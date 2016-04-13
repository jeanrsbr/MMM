/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.SVM;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import MMM.MISC.LeituraProperties;
import MMM.MISC.Log;

/**
 *
 * @author Jean-NoteI5
 */
public class SVMExecutor {

    private final int numThreads;
    private final String nomArqARFF;

    public SVMExecutor(String nomArqARFF) {
        this.nomArqARFF = nomArqARFF;
        numThreads = Integer.parseInt(LeituraProperties.getInstance().leituraProperties("thread.svm"));
    }

    public void executaAnalise() throws SVMExecutorException, WekaSVMException, ParametroSVMException {

        try {

            ManipuladorParametroSVM manipuladorParametroSVM = new ManipuladorParametroSVM(getName());
            manipuladorParametroSVM.populaAnalise();
            ArrayList<ParametroSVM> analise = manipuladorParametroSVM.getParametroSVM();

            //Varre as opções de análise
            for (int i = 0; i < analise.size(); i++) {

                //THREAD
                new Thread(new WekaSVM(nomArqARFF, analise.get(i))).start();

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
    
    //Executa a SVM para uma única configuração
    public ParametroSVM executaAnalise(ParametroSVM in) throws WekaSVMException, ParametroSVMException{
        WekaSVM analise = new WekaSVM(nomArqARFF, in);
        analise.perfomanceAnalysis();
        //Retorna o objeto com o resultado da análise
        return in;
               
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
            resultado.write(analise.get(0).montaCabecalho());
            resultado.newLine();

            Log.loga("Iniciando exportação do arquivo CSV", "SVM");
            //Varre as opções de análise
            for (int i = 0; i < analise.size(); i++) {
                resultado.write(analise.get(i).montaLinha());
                resultado.newLine();
            }
            resultado.flush();
            resultado.close();
        } catch (IOException ex) {
            throw new SVMExecutorException("Não foi possível criar o arquivo de resultado");
        }

    }


    //Retorna o nome do arquivo
    private String getName() {
        File file = new File(nomArqARFF);
        return file.getName();
    }
}
