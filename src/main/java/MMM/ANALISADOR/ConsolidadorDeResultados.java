/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.ANALISADOR;

import MMM.MISC.ClientFTP;
import MMM.MISC.ClienteFTPException;
import MMM.MISC.LeituraProperties;
import com.sun.security.ntlm.Client;

/**
 *
 * @author Jean-NoteI5
 */
public class ConsolidadorDeResultados {

    private final static String ARFF_DIR = "ARFF";
    private final static String RESULTADO_DIR = "resultado";
    private boolean usaFTP;

    //Verificar quantos ativos serão analisados (PROPERTIES)
    //Baixar os arquivos CSV do FTP ou pegar os arquivos da base local
    //Baixar os arquivos ARFF do FTP ou pegar os arquivos da base local
    //Sugerir compra de quais ativos
    //Para o TCC precisa comparar a sugestão com o valor efetivo para verificar se houve lucro ou prejuízo (Talvez isso deve ser em outra CLASSE)
    //Sugere a compra e venda em arquivo CSV?
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

                //Se deve obter os arquivos do FTP
                if (usaFTP) {
                    baixaArquivosFTP(ativos.length);
                }

            }

        } catch (Exception ex) {
            throw new ConsolidadorDeResultadosException("Houve um erro no momento de consolidar os resultados");
        }
    }

    //Baixa arquivos do FTP
    private void baixaArquivosFTP(int qtdArquivos) throws ClienteFTPException, InterruptedException {

        //Baixa o arquivo ARFF
        ClientFTP arff = new ClientFTP();
        //Verifica se já foram processados todos os arquivos ARFF necessários
        while (true) {
            if (arff.checkListFile(ARFF_DIR) == qtdArquivos) {
                break;
            }
            //
            Thread.sleep(10000);
        }
        //Baixa os arquivos ARFF
        arff.receiveFile(ARFF_DIR);

        //Baixa o arquivo CSV
        ClientFTP resultado = new ClientFTP();

        //Verifica se já foram processados todos os arquivos de resultado necessários
        while (true) {
            if (resultado.checkListFile(RESULTADO_DIR) == qtdArquivos) {
                break;
            }
            //
            Thread.sleep(10000);
        }
        resultado.receiveFile(RESULTADO_DIR);
    }

}
