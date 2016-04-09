/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tccmaven;

import java.io.File;
import java.io.FileInputStream;
import tccmaven.MISC.LeituraProperties;
import tccmaven.ARFF.PARAMETROS.InsereParametrosException;
import tccmaven.ARFF.IMPORT.BaixaArquivoException;
import tccmaven.ARFF.IMPORT.ImportadorException;
import tccmaven.MISC.Log;
import tccmaven.ARFF.GeraArquivoARFFException;
import tccmaven.ARFF.GeraArquivoARFF;
import java.io.IOException;
import tccmaven.ARFF.PARAMETROS.IndicadoresException;
import tccmaven.ARFF.PARAMETROS.NomeParametrosException;
import tccmaven.SVM.ParametroSVMException;
import tccmaven.SVM.SVMExecutor;
import tccmaven.SVM.SVMExecutorException;
import tccmaven.SVM.WekaSVMException;

/**
 *
 * @author Jean-NoteI5
 */
public class Main {

    public static void main(String[] args) throws IOException {

        try {

            //Instância o arquivo de propriedades
            File properties = new File("properties/dados.properties");
            //Verifica se existe o arquivo de propriedades
            if (!properties.exists()) {
                System.out.println("Não existe o arquivo de propriedades no diretório \n" + properties.getAbsolutePath());
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
            File resultado = new File("teste/");
            String[] listaResultado = resultado.list();
            for (int i = 0; i < listaResultado.length; i++) {

                //Se for arquivo de resultado
                if (listaResultado[i].endsWith(".csv") && listaResultado[i].startsWith("resultado_")) {
                    new File("teste/" + listaResultado[i]).delete();
                }
            }
            
            //Deleta os arquivos de LOG
            File log = new File("logs/");
            String[] listaLog = log.list();
            for (int i = 0; i < listaLog.length; i++) {
                //Se for arquivo de resultado
                if (listaLog[i].endsWith(".txt") && listaLog[i].startsWith("output_")) {
                    new File("logs/" + listaLog[i]).delete();
                }
            }

            //Inicializa o buffer
            Log.iniBuf();

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
        } catch (InsereParametrosException | BaixaArquivoException | ImportadorException | GeraArquivoARFFException | IndicadoresException | NomeParametrosException | SVMExecutorException | WekaSVMException | ParametroSVMException ex) {
            Log.loga(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
