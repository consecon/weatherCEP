package SendEmail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class SendEmail {
    private String username="chuthuavu1996@gmail.com";
    private String password="ruoi1996";
    public SendEmail(){

    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void send(String msg){
        Properties props= new Properties();
        props.put("mail.smtp.host", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        Session session= Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUsername(),getPassword());
            }
        });
        try {
            Message message= new MimeMessage(session);
            String to="chuthuavu1996@gmail.com";
            InternetAddress[] address= InternetAddress.parse(to,true);
            message.setRecipients(Message.RecipientType.TO,address);
            message.setSubject("weather warning");
            message.setSentDate(new Date());
            message.setText(msg);
            message.setHeader("XPriority", "1");
            Transport.send(message);
        } catch (MessagingException e){
            throw new  RuntimeException(e);
        }
    }
}
