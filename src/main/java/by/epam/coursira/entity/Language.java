package by.epam.coursira.entity;

import java.util.Locale;

public enum Language {
  RU(new Locale("ru", "RU")),
  EN(Locale.ENGLISH);

  private final Locale locale;

  Language(Locale name) {
    this.locale = name;
  }

  public Locale getLocale() {
    return locale;
  }
}
