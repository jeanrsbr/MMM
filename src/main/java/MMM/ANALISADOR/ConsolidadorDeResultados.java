/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.ANALISADOR;

import MMM.ARFF.ARFFConstants;
import MMM.MISC.ClientFTP;
import MMM.MISC.ClienteFTPException;
import MMM.MISC.LeituraProperties;
import MMM.MISC.Log;
import MMM.SVM.SVMConstants;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Jean-NoteI5
 */
public class ConsolidadorDeResultados {

    private boolean usaFTP;

    public ConsolidadorDeResultados() {
        //Indica que usa FTP
        usaFTP = true;
        //Se não usa FTP
        if (Integer.parseInt(LeituraProperties.getInstance().leituraProperties("ftp.ftp")) == 0) {
            usaFTP = false;
        }

    }

    //Realiza a consolidação de resultados e sugere qual ativo deve ser comprado ou vendido
    public void sugereCompra() throws ConsolidadorDeResultadosException {

        try {



            //Valida os arquivos existentes no diretório
            validaArquivos();

            ArrayList<Resultado> resultados = new ArrayList<>();

            //Obtém a lista de arquivos do diretório ARFF
            String[] listaArquivosARFF = listaArquivos(ARFFConstants.ARFF_FOLDER);
            String[] listaArquivosResultado = listaArquivos(SVMConstants.RESULTADO_FOLDER);

            //Varre os arquivos para análise
            for (int i = 0; i < listaArquivosARFF.length; i++) {

                //Inicia analise de determinado ativo
                Analisador analisador = new Analisador(SVMConstants.RESULTADO_FOLDER + listaArquivosResultado[i], ARFFConstants.ARFF_FOLDER + listaArquivosARFF[i]);
                //Inclui o resultado analisado deste ativo
                resultados.add(analisador.analisa());

            }



            double percentualDiffValoresAux = 0;
            int indAux = 0;

            //Varre os resultados obtidos
            for (int i = 0; i < resultados.size(); i++) {

                //Se preveu que o ativo vai baixar de preço
                if (resultados.get(i).getPercentualDiffValores() <= 100){
                    continue;
                }

                //Se houve um aumento exagerado, presume que o algoritmo loquetiou
                if (resultados.get(i).getPercentualDiffValores() > 150){
                    continue;
                }

                //Se o registro atual processado, possui uma diferença maior que o registro anterior
                if (resultados.get(i).getPercentualDiffValores() > percentualDiffValoresAux){
                    percentualDiffValoresAux = resultados.get(i).getPercentualDiffValores();
                    indAux = i;

                }

            }

            //Indica ao usuário quais dos ativos devem ser investidos
            Log.loga("Invista no ativo: " + resultados.get(indAux).getAtivo(), "ANALISE");

        } catch (Exception ex) {
            throw new ConsolidadorDeResultadosException("Houve um erro no momento de consolidar os resultados");
        }
    }

    //Retorna a lista de arquivos existentes no diretório
    private String[] listaArquivos(String name){
        File aRFF = new File(name);
        return aRFF.list();

    }

    private void validaArquivos() throws ClienteFTPException, InterruptedException, ConsolidadorDeResultadosException {

        //Obtém a lista de ativos que devem ser importados
        String[] ativos = LeituraProperties.getInstance().leituraProperties("prop.ativos").split("#");

        //Se deve obter os arquivos do FTP
        if (usaFTP) {
            baixaArquivosFTP(ativos.length);
        }

        //Verifica se possui todos os arquivos ARFF necessários
        if (listaArquivos(ARFFConstants.ARFF_FOLDER).length != ativos.length){
            throw new ConsolidadorDeResultadosException("A quantidade de arquivos ARFF não é igual a quantidade de ativos indicados para importação");
        }

        //Verifica se possui todos os arquivos de resultado necessários
        if (listaArquivos(SVMConstants.RESULTADO_FOLDER).length != ativos.length){
            throw new ConsolidadorDeResultadosException("A quantidade de arquivos de resultado não é igual a quantidade de ativos indicados para importação");
        }
    }

    //Baixa arquivos do FTP
    private void baixaArquivosFTP(int qtdArquivos) throws ClienteFTPException, InterruptedException {

        //Baixa o arquivo ARFF
        ClientFTP arff = new ClientFTP();
        //Verifica se já foram processados todos os arquivos ARFF necessários
        while (true) {
            if (arff.checkListFile(ARFFConstants.ARFF_FOLDER) == qtdArquivos) {
                break;
            }
            //
            Thread.sleep(10000);
        }
        //Baixa os arquivos ARFF
        arff.receiveFile(ARFFConstants.ARFF_FOLDER);

        //Baixa o arquivo CSV
        ClientFTP resultado = new ClientFTP();

        //Verifica se já foram processados todos os arquivos de resultado necessários
        while (true) {
            if (resultado.checkListFile(SVMConstants.RESULTADO_FOLDER) == qtdArquivos) {
                break;
            }
            //
            Thread.sleep(10000);
        }
        resultado.receiveFile(SVMConstants.RESULTADO_FOLDER);
    }

}
