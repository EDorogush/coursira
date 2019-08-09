package by.epam.coursira.model;

import by.epam.coursira.entity.Language;
import by.epam.coursira.entity.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public abstract class JspModelAbstract {
  private final List<Language> languages = Arrays.asList(Language.values());
  private Principal principal;

  public JspModelAbstract() {}

  public Principal getPrincipal() {
    return principal;
  }

  public void setPrincipal(Principal principal) {
    this.principal = principal;
  }

  public List<Language> getLanguages() {
    return languages;
  }

  public Locale getCurrentLocale() {
    return principal.getSession().getLanguage().getLocale();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JspModelAbstract)) return false;
    JspModelAbstract that = (JspModelAbstract) o;
    return Objects.equals(languages, that.languages) && Objects.equals(principal, that.principal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(languages, principal);
  }
}
