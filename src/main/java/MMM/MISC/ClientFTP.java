package MMM.MISC;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPClient;

public class ClientFTP {

    private final String endFTP;
    private final String usuario;
    private final String senha;
    private final String folderserver;

    public ClientFTP() {
        this.endFTP = LeituraProperties.getInstance().leituraPropertiesString("ftp.hostname");
        this.usuario = LeituraProperties.getInstance().leituraPropertiesString("ftp.username");
        this.senha = LeituraProperties.getInstance().leituraPropertiesString("ftp.password");
        this.folderserver = LeituraProperties.getInstance().leituraPropertiesString("ftp.folderserver");
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
    public void sendFile(String fileName, String folder) throws ClienteFTPException {
        try {
            //abre um stream com o arquivo a ser enviado
            InputStream is;

            is = new FileInputStream(fileName);

            File file = new File(fileName);

            //Conecta no FTP
            FTPClient ftp = connectFTP();
            //Indica arquivo do tipo ASCII
            ftp.setFileType(FTPClient.ASCII_FILE_TYPE);
            ftp.changeWorkingDirectory(folderserver + folder);

            //Grava o arquivo no FTP
            ftp.storeFile(file.getName(), is);

            //Disconecta do FTP
            disconnectFTP(ftp);
        } catch (ClienteFTPException | IOException ex) {
            throw new ClienteFTPException("Não foi possível enviar o arquivo para o FTP");
        }
    }

    //Verifica a quantidade de arquivos na pasta do FTP
    public int checkListFile(String folder) throws ClienteFTPException {
        try {
            FTPClient ftp;
            ftp = connectFTP();
            //Indica arquivo do tipo ASCII
            ftp.setFileType(FTPClient.ASCII_FILE_TYPE);

            ftp.changeWorkingDirectory(folderserver + folder);

            //Obtém a lista de arquivos no FTP
            String[] lista = ftp.listNames();

            int qtdArquivos = lista.length;
            qtdArquivos = qtdArquivos - 2; // Retira o "." e  ".." que não contam como arquivos

            //Disconecta do FTP
            disconnectFTP(ftp);

            return qtdArquivos;
        } catch (IOException ex){
            throw new ClienteFTPException("Não foi possível obter a quantidade de arquivos que estão no FTP");
        }
    }

    //Recebe o arquivo do servidor de FTP(Nome do arquivo)
    public void receiveFile(String folder) throws ClienteFTPException {

        try {
            FTPClient ftp;
            ftp = connectFTP();
            //Indica arquivo do tipo ASCII
            ftp.setFileType(FTPClient.ASCII_FILE_TYPE);

            ftp.changeWorkingDirectory(folderserver + folder);

            //Obtém a lista de arquivos no FTP
            String[] lista = ftp.listNames();

            //Se não encontrou arquivos, aborta
            if (lista == null || lista.length == 0) {
                return;
            }

            //Varre a lista de arquivos
            for (int i = 0; i < lista.length; i++) {

                //Se for os arquivos de pasta
                if (lista[i].equals(".") || lista[i].equals("..")){
                    continue;
                }

                //Cria o arquivo que será baixado(Na pasta indicada)
                FileOutputStream fos = new FileOutputStream(folder + lista[i]);
                //Descarrega o arquivo na pasta corrente
                ftp.retrieveFile(lista[i], fos);
                //Exclui o arquivo da pasta do FTP
                ftp.deleteFile(lista[i]);
            }

            disconnectFTP(ftp);
        } catch (IOException ex) {
            throw new ClienteFTPException("Não foi possível receber o arquivo do FTP");
        }

    }
}
