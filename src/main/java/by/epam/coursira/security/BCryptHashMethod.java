package by.epam.coursira.security;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component("bcrypt")
public class BCryptHashMethod implements HashMethod {
  @Override
  public String toHash(String value) {
    return BCrypt.hashpw(value, BCrypt.gensalt());
  }

  @Override
  public boolean verify(String actual, String hashedValue) {
    return BCrypt.checkpw(actual, hashedValue);
  }
}
