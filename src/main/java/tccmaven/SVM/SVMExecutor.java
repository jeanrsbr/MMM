/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tccmaven.SVM;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import tccmaven.MISC.EditaValores;
import tccmaven.MISC.LeituraProperties;
import tccmaven.MISC.Log;

/**
 *
 * @author Jean-NoteI5
 */
public class SVMExecutor {

    private int numThreads;
    private String nomArqARFF;

    public SVMExecutor(String nomArqARFF) {
        this.nomArqARFF = nomArqARFF;
        numThreads = Integer.parseInt(LeituraProperties.getInstance().leituraProperties("thread.svm"));
    }

    public void executaAnalise() throws SVMExecutorException, WekaSVMException, ParametroSVMException {

        try {

            ManipuladorParametroSVM manipuladorParametroSVM = new ManipuladorParametroSVM();
            manipuladorParametroSVM.populaAnalise();
            ArrayList<ParametroSVM> analise = manipuladorParametroSVM.getParametroSVM();

            //Varre as opções de análise
            for (int i = 0; i < analise.size(); i++) {


                //THREAD
                new Thread(new WekaSVM(nomArqARFF, analise.get(i), i)).start();

//                //SEM THREAD
//                WekaSVM bambu = new WekaSVM(nomArqARFF, analise.get(i), i);
//                bambu.perfomanceAnalysis();
                //Se processou todas as threads
                while (true) {
                    if (Thread.activeCount() != numThreads + 1) {
                        break;
                    } else {
                        //Espera 1 segundo para conferir novamente
                        Thread.sleep(1000);
                    }
                }

            }

            //Se processou todas as threads
            while (true) {
                if (Thread.activeCount() == 1) {
                    break;
                } else {
                    //Espera 1 segundo para conferir novamente
                    Thread.sleep(1000);
                }
            }

            //Cria arquivo CSV
            criaCSV(analise);

        } catch (WekaSVMException | InterruptedException ex) {
            throw new SVMExecutorException("Não foi possível executar a predição");
        }

    }

    private void criaCSV(ArrayList<ParametroSVM> analise) throws SVMExecutorException, WekaSVMException,
            ParametroSVMException {

        try {
            //Abre o arquivo CSV de resultados
            File file = new File("teste/resultado_" + getName().split(".arff")[0] + ".csv");
            FileOutputStream arquivoGravacao = new FileOutputStream(file);
            OutputStreamWriter strWriter = new OutputStreamWriter(arquivoGravacao);
            BufferedWriter resultado = new BufferedWriter(strWriter);

            //Cabeçalho
            resultado.
                    write("ativo;cost;gamma;tam_treino;evaluation;kernel;type;valor_real;valor_predito;diffMod;perc_acerto");
            resultado.newLine();

            Log.loga("Iniciando exportação do arquivo CSV", "SVM");
            //Varre as opções de análise
            for (int i = 0; i < analise.size(); i++) {
                //Se possui resultado na ocorrência
                if (ManipuladorResultadoSVM.getInstance().getResultado(i) != null) {
                    resultado.write(montaLinha(analise.get(i), ManipuladorResultadoSVM.getInstance().getResultado(i)));
                    resultado.newLine();
                }
            }
            resultado.flush();
            resultado.close();
        } catch (IOException ex) {
            throw new SVMExecutorException("Não foi possível criar o arquivo de resultado");
        }

    }

    private String montaLinha(ParametroSVM parametroSVM, ResultadoSVM resultadoSVM) throws WekaSVMException,
            ParametroSVMException {

        StringBuilder linha = new StringBuilder();
        linha.append(getName());
        linha.append(";");
        linha.append(EditaValores.editaVirgula(parametroSVM.getCost()));
        linha.append(";");
        linha.append(EditaValores.editaVirgula(parametroSVM.getGamma()));
        linha.append(";");
        linha.append(parametroSVM.getTamanhoDoConjunto());
        linha.append(";");
        linha.append(parametroSVM.getGridSearchEvaluationAlfa());
        linha.append(";");
        linha.append(parametroSVM.getKernelAlfa());
        linha.append(";");
        linha.append(parametroSVM.getTypeAlfa());
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(resultadoSVM.getReal()));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(resultadoSVM.getPredict()));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(resultadoSVM.getDiffMod()));
        linha.append(";");
        linha.append(EditaValores.edita2DecVirgula(resultadoSVM.getPercentualAcerto()));

        //Retorna a linha montada
        return linha.toString();

    }

    //Retorna o nome do arquivo
    private String getName() {
        File file = new File(nomArqARFF);
        return file.getName();
    }
}
