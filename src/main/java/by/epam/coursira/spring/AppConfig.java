package by.epam.coursira.spring;

import by.epam.coursira.command.Command;
import by.epam.coursira.mail.MailSender;
import by.epam.coursira.security.BCryptHashMethod;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@Configuration
public class AppConfig {

  @Bean
  public HikariConfig hikariConfig(ServletContext servletContext) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(servletContext.getInitParameter("jdbcDriver"));
    config.setMaximumPoolSize(Integer.parseInt(servletContext.getInitParameter("dbPoolSize")));
    return config;
  }

  @Bean
  public DataSource dataSource(HikariConfig config) {
    return new HikariDataSource(config);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public Properties mailProperties(ServletContext servletContext) {
    Properties props = new Properties();
    props.setProperty("mail.smtp.host", servletContext.getInitParameter("mail.smtp.host"));
    props.setProperty("mail.smtp.port", servletContext.getInitParameter("mail.smtp.port"));
    props.setProperty("mail.smtp.auth", servletContext.getInitParameter("mail.smtp.auth"));
    props.setProperty("mail.smtp.starttls.enable", servletContext.getInitParameter("mail.smtp.starttls.enable"));
    return props;
  }

  @Bean
  public MailSender mailSender(ServletContext servletContext, @Qualifier("mailProperties") Properties properties) {
    String gmailAddress = servletContext.getInitParameter("gmailAddress");
    String password = servletContext.getInitParameter("gmailPassword");
    return new MailSender(gmailAddress, password, properties);
  }

  @Bean
  public Integer paginationLimit(ServletContext servletContext) {
    return Integer.parseInt(servletContext.getInitParameter("paginationLimit"));
  }

  @Bean
  public List<Command> commandList(Command courseCommand, Command courseCreateCommand, Command courseUpdateCommand,
                                   Command indexCommand, Command languageCommand, Command loginCommand, Command logoutCommand,
                                   Command courseIdCommand, Command courseIdSubscriptionCommand, Command personalPageCommand,
                                   Command personalUpdateCommand, Command registrationCommand, Command registrationConfirmCommand) {
    return List.of(courseCommand, courseCreateCommand, courseUpdateCommand, indexCommand, languageCommand, loginCommand, logoutCommand,
      courseIdCommand, courseIdSubscriptionCommand, personalPageCommand, personalUpdateCommand, registrationCommand, registrationConfirmCommand);
  }


}
