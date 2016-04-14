/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM;

import java.io.File;
import java.io.FileNotFoundException;
import MMM.MISC.LeituraProperties;
import MMM.ARFF.PARAMETROS.InsereParametrosException;
import MMM.ARFF.IMPORT.BaixaArquivoException;
import MMM.ARFF.IMPORT.ImportadorException;
import MMM.MISC.Log;
import MMM.ARFF.GeraArquivoARFFException;
import MMM.ARFF.GeraArquivoARFF;
import MMM.ARFF.PARAMETROS.IndicadoresException;
import MMM.ARFF.PARAMETROS.NomeParametrosException;
import MMM.MISC.ClienteFTPException;
import MMM.SVM.ParametroSVMException;
import MMM.SVM.SVMExecutor;
import MMM.SVM.SVMExecutorException;
import MMM.SVM.WekaSVMException;

/**
 *
 * @author Jean-NoteI5
 */
public class Main {

    public static void main(String[] args) {

        try {

            //Instância o arquivo de propriedades
            File properties = new File("properties/dados.properties");
            //Verifica se existe o arquivo de propriedades
            if (!properties.exists()) {
                System.out.
                        println("Não existe o arquivo de propriedades no diretório \n" + properties.getAbsolutePath());
                return;
            }
            //Se não foi informada a data inicial
            if (LeituraProperties.getInstance().leituraProperties("prop.DataIni").equals("")) {
                System.out.println("É obrigatório informar a data inicial para importação");
                return;
            }

            //Se não foi informada a data final
            if (LeituraProperties.getInstance().leituraProperties("prop.DataFim").equals("")) {
                System.out.println("É obrigatório informar a data inicial para importação");
                return;
            }

            //Deleta os arquivos de resultado
            File resultado = new File("resultado/");
            //Verifica se existe o diretório
            if (!resultado.isDirectory()) {
                System.out.println("Não existe o diretório RESULTADO na pasta de execução");
                return;
            }

            String[] listaResultado = resultado.list();
            for (int i = 0; i < listaResultado.length; i++) {

                //Se for arquivo de resultado
                if (listaResultado[i].endsWith(".csv")) {
                    new File("resultado/" + listaResultado[i]).delete();
                }
            }

            //Deleta os arquivos de LOG
            File log = new File("c:/temp/");

            if (!log.isDirectory()) {
                System.out.println("Não existe o diretório temporário na pasta de execução! C:/temp");
                return;
            }
            String[] listaLog = log.list();

            //Se possui LOGs para serem deletados
            if (listaLog != null) {
                for (int i = 0; i < listaLog.length; i++) {
                    //Se for arquivo de resultado
                    if (listaLog[i].endsWith(".txt") && listaLog[i].startsWith("output_")) {
                        new File("c:/temp/" + listaLog[i]).delete();
                    }
                }
            }

            //Inicializa o buffer
            Log.iniBuf();

            //Avalia o tipo de execução
            int tipoExe = Integer.parseInt(LeituraProperties.getInstance().leituraProperties("prop.tipoExe"));

            if (tipoExe == 1) {
                executaSVM();
            } else {
                executaAnalisador();
            }

        } catch (InsereParametrosException | BaixaArquivoException | ImportadorException | GeraArquivoARFFException | IndicadoresException | NomeParametrosException | SVMExecutorException | WekaSVMException | ParametroSVMException | FileNotFoundException | ClienteFTPException ex) {
            Log.loga(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void executaSVM() throws GeraArquivoARFFException, ImportadorException, InsereParametrosException,
            BaixaArquivoException, IndicadoresException, NomeParametrosException, SVMExecutorException, WekaSVMException,
            ParametroSVMException, FileNotFoundException {
        //Obtém a lista de ativos que devem ser importados
        String[] ativos = LeituraProperties.getInstance().leituraProperties("prop.ativos").split("#");

        //Varre a lista de ativos a serem importados
        for (int i = 0; i < ativos.length; i++) {

            String[] atiPaises = new String[2];

            if (ativos[i].contains(";")) {
                //Obtém os ativos de cada país
                atiPaises = ativos[i].split(";");
            } else {
                atiPaises[0] = ativos[i];
            }

            //Direciona arquivo de LOG com o nome do ativo
            Log.buffAtivo(atiPaises[0]);

            //Criar arquivo ARFF
            Log.loga("Será gerado o arquivo ARFF", "ARFF");
            //Instância a geração de arquivos ARFF
            GeraArquivoARFF geraArquivoARFF = new GeraArquivoARFF(atiPaises[0], atiPaises[1]);
            //Gera o arquivo ARFF com a quantidade de ativos indicada no properties
            String arquivoARFF = geraArquivoARFF.geraArquivo();

            //Executar algoritmo SVM
            SVMExecutor sVMAnalisador = new SVMExecutor(arquivoARFF);
            sVMAnalisador.executaAnalise();

        }

    }

    private static void executaAnalisador() throws ClienteFTPException {

        try {

        } catch (Exception ex) {
            Log.loga(ex.getMessage());
            ex.printStackTrace();
        }

    }

}
