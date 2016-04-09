/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tccmaven.ARFF.IMPORT;

import tccmaven.MISC.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import tccmaven.MISC.LeituraProperties;

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
            //Se esta em modo teste
            if (LeituraProperties.getInstance().leituraProperties("prop.teste").equals("1")) {
                File file = new File("Teste//teste.csv");
                FileInputStream arquivo = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(arquivo);
                return new BufferedReader(reader);
            } else {
                return arquivoLink();
            }
        } catch (FileNotFoundException ex) {
            throw new BaixaArquivoException("Não foi possível abrir o arquivo de teste", ex);
        }

    }

    //Baixa o arquivo do Link
    public BufferedReader arquivoLink() throws BaixaArquivoException {

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
            if (!LeituraProperties.getInstance().leituraProperties("conn.proxyHost").equals("")) {
                // INFORMAÇÕES DE PROXY
                System.setProperty("http.proxyHost", LeituraProperties.getInstance().leituraProperties("conn.proxyHost"));
                System.setProperty("http.proxyPort", LeituraProperties.getInstance().leituraProperties("conn.proxyPort"));

                // AUTENTICAÇÃO DE PROXY
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(LeituraProperties.getInstance().leituraProperties("conn.proxyUser"), LeituraProperties.
                                getInstance().leituraProperties("conn.proxyPassword").toCharArray());
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
        try {

            //Data final para leitura
            String dataFim = LeituraProperties.getInstance().leituraProperties("prop.DataFim");
            //Pega a instância atual
            Calendar calendar = Calendar.getInstance();

            //Se possui informado data final
            if (!dataFim.equals("")) {
                //Converte a data final para formato simples
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date;
                date = formatter.parse(dataFim);
                calendar.setTime(date);
            }

            //Inicializa a data final
            String anoFim = String.valueOf(calendar.get(Calendar.YEAR));
            String mesFim = String.valueOf(calendar.get(Calendar.MONTH));
            String diaFim = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

            //Pega a instância atual
            calendar = Calendar.getInstance();

            //Data inicial para leitura
            String dataIni = LeituraProperties.getInstance().leituraProperties("prop.DataIni");
            //Se possui data inicial
            if (!dataIni.equals("")) {
                //Converte a data final para formato simples
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date;
                date = formatter.parse(dataIni);
                calendar.setTime(date);
            } else {
                //Força a data inicial como o ano de 2010
                calendar.set(2010, 01, 01);
            }

            //Inicializa a data inicial
            String anoIni = String.valueOf(calendar.get(Calendar.YEAR));
            String mesIni = String.valueOf(calendar.get(Calendar.MONTH));
            String diaIni = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

            //Monta URL para baixar a planilha CSV
            return LeituraProperties.getInstance().leituraProperties("prop.CSV").
                    replaceAll("#ATIVO#", ativo).
                    replaceAll("#ANO_FIM#", anoFim).
                    replaceAll("#MES_FIM#", mesFim).
                    replaceAll("#DIA_FIM#", diaFim).
                    replaceAll("#ANO_INI#", anoIni).
                    replaceAll("#MES_INI#", mesIni).
                    replaceAll("#DIA_INI#", diaIni);
        } catch (ParseException ex) {
            throw new BaixaArquivoException("Não foi possível realizar a conversão de datas corretamente", ex);
        }

    }
}
