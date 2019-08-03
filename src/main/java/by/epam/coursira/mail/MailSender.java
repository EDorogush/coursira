package by.epam.coursira.mail;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailSender {
  private static final Logger logger = LogManager.getLogger();
  private final String APP_USER_NAME;
  private final String APP_PASSWORD;
  private final Properties propSmtp;

  public MailSender(String address, String password, Properties prop) {
    this.APP_USER_NAME = address;
    this.APP_PASSWORD = password;
    this.propSmtp = prop;
  }


  public boolean sendMail(String sendTo, String subject, String messageText)
      throws MessagingException {


    Session session =
        Session.getInstance(
            propSmtp,
            new javax.mail.Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(APP_USER_NAME, APP_PASSWORD);
              }
            });

    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress("lena.panfilenok@gmail.com"));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sendTo));
    message.setSubject(subject);
    message.setText(messageText);
    Transport.send(message);

    logger.info("message to {} was sent ", sendTo);
    return true;
  }
}
