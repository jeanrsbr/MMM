/*
 * TCC
 * CopyRight Rech Informática Ltda. Todos os direitos reservados.
 */
package MMM.MISC;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Descrição da classe.
 */
public class LeituraProperties {

    private static LeituraProperties instance;
    private Properties props;

    private LeituraProperties(){

        props = new Properties();
        FileInputStream file;
        try {
            file = new FileInputStream("./properties/dados.properties");
            props.load(file);
        } catch (IOException ex) {
            System.out.println("Não foi possível abrir o arquivo de propriedades");
        }

    }

    //Lê o arquivo properties
    public String leituraProperties(String chave){
        return props.getProperty(chave, "");
    }

    public static LeituraProperties getInstance(){
        if (instance == null) {
            instance = new LeituraProperties();
        }
        return instance;
    }

}
