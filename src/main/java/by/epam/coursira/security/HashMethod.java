package by.epam.coursira.security;

public interface HashMethod {
  String toHash(String s);

  boolean verify(String actual, String hashedValue);
}
