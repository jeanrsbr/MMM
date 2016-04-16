/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.ARFF.IMPORT;

import MMM.MISC.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import MMM.MISC.LeituraProperties;

/**
 *
 * @author Jean-NoteI5
 */
public class BaixaArquivo {

    String ativo;

    public BaixaArquivo(String ativo) {
        this.ativo = ativo;
    }

    public BufferedReader downloadArquivo() throws BaixaArquivoException {

        try {
            //Instância objeto para acesso ao URL onde encontra-se o CSV
            URL url;
            //Monta URL para baixar a planilha CSV
            String url_yahoo = montaLink();
            Log.loga(url_yahoo, "CSV");
            //Instância a URL que contém o arquivo CSV
            url = new URL(url_yahoo);

            //Declara a instância do arquivo para leitura
            BufferedReader br;
            //Se possui configurações de proxy
            if (!LeituraProperties.getInstance().leituraPropertiesString("conn.proxyHost").equals("")) {
                // INFORMAÇÕES DE PROXY
                System.
                        setProperty("http.proxyHost", LeituraProperties.getInstance().
                                leituraPropertiesString("conn.proxyHost"));
                System.
                        setProperty("http.proxyPort", LeituraProperties.getInstance().
                                leituraPropertiesString("conn.proxyPort"));

                // AUTENTICAÇÃO DE PROXY
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(LeituraProperties.getInstance().
                                leituraPropertiesString("conn.proxyUser"), LeituraProperties.
                                getInstance().leituraPropertiesString("conn.proxyPassword").toCharArray());
                    }
                });

                HttpURLConnection con;
                con = (HttpURLConnection) url.openConnection();
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            } else {
                br = new BufferedReader(new InputStreamReader(url.openStream()));
            }

            return br;

        } catch (MalformedURLException ex) {
            throw new BaixaArquivoException("O Link para baixar o CSV de cotações não foi montado corretamente", ex);
        } catch (IOException ex) {
            throw new BaixaArquivoException("Não foi possível baixar o arquivo CSV do link montado", ex);
        }
    }

    //Monta o link para efetuar a requisição
    private String montaLink() throws BaixaArquivoException {
        //Pega a instância atual
        Calendar calendar = LeituraProperties.getInstance().leituraPropertiesDataCalendar("prop.DataFim");

        if (calendar == null){
            throw new BaixaArquivoException("Houve erro no momento de obter a data final");
        }
        //Inicializa a data final
        String anoFim = String.valueOf(calendar.get(Calendar.YEAR));
        String mesFim = String.valueOf(calendar.get(Calendar.MONTH));
        String diaFim = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        //Pega a instância atual
        calendar = LeituraProperties.getInstance().leituraPropertiesDataCalendar("prop.DataIni");

        if (calendar == null){
            throw new BaixaArquivoException("Houve erro no momento de obter a data inicial");
        }

        //Inicializa a data inicial
        String anoIni = String.valueOf(calendar.get(Calendar.YEAR));
        String mesIni = String.valueOf(calendar.get(Calendar.MONTH));
        String diaIni = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        //Monta URL para baixar a planilha CSV
        return LeituraProperties.getInstance().leituraPropertiesString("prop.CSV").
                replaceAll("#ATIVO#", ativo).
                replaceAll("#ANO_FIM#", anoFim).
                replaceAll("#MES_FIM#", mesFim).
                replaceAll("#DIA_FIM#", diaFim).
                replaceAll("#ANO_INI#", anoIni).
                replaceAll("#MES_INI#", mesIni).
                replaceAll("#DIA_INI#", diaIni);

    }
}
