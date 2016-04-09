package MMM.MISC;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPClient;

public class ClientFTP {

    private String endFTP;
    private String usuario;
    private String senha;
    private String folderserver;
    private String folderlocal;

    public ClientFTP() {
        this.endFTP = LeituraProperties.getInstance().leituraProperties("ftp.hostname");
        this.usuario = LeituraProperties.getInstance().leituraProperties("ftp.username");
        this.senha = LeituraProperties.getInstance().leituraProperties("ftp.password");
        this.folderserver = LeituraProperties.getInstance().leituraProperties("ftp.folderserver");
        this.folderlocal = LeituraProperties.getInstance().leituraProperties("ftp.folderlocal");
    }

    //Conecta ao servidor FTP
    private FTPClient connectFTP() throws ClienteFTPException {

        try {
            FTPClient ftp = null;
            //Tenta conectar por até 5 vezes
            for (int i = 0; i < 5; i++) {
                //Entidade FTP
                ftp = new FTPClient();
                //Tenta conectar ao endereço passado
                ftp.connect(endFTP);
                //verifica se conectou com sucesso!
                if (FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    ftp.login(usuario, senha);
                    break;
                } else {
                    ftp.disconnect();
                }
                //Se chegou ao final e não conseguiu conectar
                if (i == 4) {
                    throw new ClienteFTPException("Não foi possível conectar no servidor de FTP");
                }
            }

            return ftp;

        } catch (IOException | ClienteFTPException ex) {
            throw new ClienteFTPException("Não foi possível conectar no servidor de FTP");
        }

    }

    //Desconecta do servidor FTP
    private void disconnectFTP(FTPClient ftp) throws IOException {
        //Executa comando de Logoff
        ftp.logout();
        //Desconecta
        ftp.disconnect();

    }

    //Envia o arquivo para o servidor de FTP
    public void sendFile(String fileName) throws ClienteFTPException {
        try {
            //abre um stream com o arquivo a ser enviado
            InputStream is;

            is = new FileInputStream(fileName);

            File file = new File(fileName);

            //Conecta no FTP
            FTPClient ftp = connectFTP();
            //Indica arquivo do tipo ASCII
            ftp.setFileType(FTPClient.ASCII_FILE_TYPE);

            //Grava o arquivo no FTP
            ftp.storeFile(folderserver + file.getName(), is);

            //Disconecta do FTP
            disconnectFTP(ftp);
        } catch (ClienteFTPException | IOException ex) {
            throw new ClienteFTPException("Não foi possível enviar o arquivo para o FTP");
        }
    }

    //Recebe o arquivo do servidor de FTP(Nome do arquivo)
    public void receiveFile() throws ClienteFTPException {

        try {
            FTPClient ftp;
            ftp = connectFTP();
            //Indica arquivo do tipo ASCII
            ftp.setFileType(FTPClient.ASCII_FILE_TYPE);
            ftp.changeWorkingDirectory(folderserver);

            //Obtém a lista de arquivos no FTP
            String[] lista = ftp.listNames();

            //Se não encontrou arquivos, aborta
            if (lista == null || lista.length == 0) {
                return;
            }

            //Varre a lista de arquivos
            for (int i = 0; i < lista.length; i++) {

                //Se for arquivo CSV
                if (lista[i].endsWith(".csv")) {
                    FileOutputStream fos;
                    //Cria o arquivo que será baixado(Na pasta indicada)
                    fos = new FileOutputStream(folderlocal + lista[i]);
                    //Descarrega o arquivo na pasta corrente
                    ftp.retrieveFile(lista[i], fos);
                }
            }

            disconnectFTP(ftp);
        } catch (ClienteFTPException | IOException ex) {
            throw new ClienteFTPException("Não foi possível receber o arquivo do FTP");
        }

    }
}
