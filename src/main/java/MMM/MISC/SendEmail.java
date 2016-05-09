/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MMM.MISC;

import java.util.ArrayList;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.mail.*;

public class SendEmail {

    private final SimpleEmail email;

    public SendEmail() {
        this.email = new SimpleEmail();
        email.setSSL(true);
        email.setAuthentication("moneymakermachinetcc@gmail.com", "j7f7h821");
        email.setHostName("smtp.gmail.com"); // o servidor SMTP para envio do e-mail
    }

    //Se passou um e-mail como String cria uma collection com o e-mail e o envia
    public void send(String to, String from, String subject, String textMsg) throws EmailException, AddressException {
        //Coleção de e-mail
        ArrayList<InternetAddress> mailTo = new ArrayList<InternetAddress>();
        InternetAddress mailToIntAd = new InternetAddress(to);
        mailTo.add(mailToIntAd);

        send(mailTo, from, subject, textMsg);

    }

    //Se passou um array de e-mail 
    public void send(String[] to, String from, String subject, String textMsg) throws EmailException, AddressException {
        //Coleção de e-mail
        ArrayList<InternetAddress> mailTo = new ArrayList<InternetAddress>();
        //Cria lista de e-mails
        for (int i = 0; i < to.length; i++) {
            //Cria nova instância de EmailAdress
            InternetAddress mailToIntAd = new InternetAddress(to[i]);
            //Adiciona a lista de e-mails
            mailTo.add(mailToIntAd);
        }
        send(mailTo, from, subject, textMsg);
    }

    private void send(ArrayList<InternetAddress> mailTo, String from, String subject, String textMsg) throws EmailException, AddressException {

        //Lista de e-mails a serem enviados(Será sempre apenas 1)
        email.setTo(mailTo);

        email.setFrom(from); // remetente

        email.setSubject(subject); // assunto do e-mail

        email.setMsg(textMsg); //conteudo do e-mail

        email.send(); //envia o e-mail

    }

}
