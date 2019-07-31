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
  private static final Properties prop = new Properties();

  public MailSender(String address, String password) {
    this.APP_USER_NAME = address;
    this.APP_PASSWORD = password;
  }

  static {
    prop.put("mail.smtp.host", "smtp.gmail.com");
    prop.put("mail.smtp.port", "587");
    prop.put("mail.smtp.auth", "true");
    prop.put("mail.smtp.starttls.enable", "true"); // TLS
  }

  public boolean sendMail(String sendTo, String subject, String messageText)
      throws MessagingException {

    Session session =
        Session.getInstance(
            prop,
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
