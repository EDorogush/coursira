package by.epam.coursira.spring;

//import org.springframework.boot.SpringApplication;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CoursiraApplication {

  public static void main(String[] args) {
    //SpringApplication.run(CoursiraApplication.class, args);
    ApplicationContext context = new ClassPathXmlApplicationContext("/spring.xml");


  }
}

