/*
 * MMM
 * CopyRight Rech Informática Ltda. Todos os direitos reservados.
 */
package MMM.ANALISADOR;

import MMM.MISC.ClientFTP;
import MMM.MISC.Log;
import java.io.File;

/**
 * Descrição da classe.
 */
public class Analisador {

    public void analisador() {

        try {

            //Instância o arquivo de propriedades
            File properties = new File("properties/dados.properties");
            //Verifica se existe o arquivo de propriedades
            if (!properties.exists()) {
                System.out.
                        println("Não existe o arquivo de propriedades no diretório \n" + properties.getAbsolutePath());
                return;
            }



            //Espera os arquivos estarem prontos no FTP
            while (true) {

                ClientFTP ftp = new ClientFTP();
                ftp.receiveFile();

                //Aguarda por um segundo para fazer a nova checagem
                Thread.sleep(1000);
            }

        } catch (Exception ex) {
            Log.loga(ex.getMessage());
            ex.printStackTrace();

        }
    }
}
