/*
 * TCC
 * CopyRight Rech Informática Ltda. Todos os direitos reservados.
 */
package MMM.MISC;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Descrição da classe.
 */
public class LeituraProperties {

    private static LeituraProperties instance;
    private Properties props;

    private LeituraProperties() {

        props = new Properties();
        FileInputStream file;
        try {
            file = new FileInputStream("./properties/dados.properties");
            props.load(file);
        } catch (IOException ex) {
            System.out.println("Não foi possível abrir o arquivo de propriedades");
        }

    }

    public String leituraPropertiesString(String chave){
        return props.getProperty(chave, "");
    }

    public int leituraPropertiesInteiro(String chave) {
        return Integer.parseInt(props.getProperty(chave, ""));
    }

    public double leituraPropertiesDouble(String chave) {
        return Double.parseDouble(props.getProperty(chave, ""));
    }


    public String leituraPropertiesDataAlpha(String chave){

        //Data
        String data = props.getProperty(chave, "");
        //Pega a instância atual
        Calendar calendar = Calendar.getInstance();

        //Se possui informado data
        if (!data.equals("")) {
            //Converte a data para formato simples
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date;
            try {
                date = formatter.parse(data);
            } catch (ParseException ex) {
                return "";
            }
            calendar.setTime(date);
        } else {
            return "";
        }



        String teste = String.valueOf(calendar.get(Calendar.MONTH));


        return String.valueOf(calendar.get(Calendar.YEAR)) + String.valueOf(calendar.get(Calendar.MONTH) + 1) + String.
                valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    }

    public Calendar leituraPropertiesDataCalendar(String chave){

        //Data
        String data = props.getProperty(chave, "");
        //Pega a instância atual
        Calendar calendar = Calendar.getInstance();

        //Se possui informado data
        if (!data.equals("")) {
            //Converte a data para formato simples
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date;
            try {
                date = formatter.parse(data);
            } catch (ParseException ex) {
                return null;
            }
            calendar.setTime(date);
        } else {
            return null;
        }

        return calendar;
    }


    public static LeituraProperties getInstance() {
        if (instance == null) {
            instance = new LeituraProperties();
        }
        return instance;
    }

}
