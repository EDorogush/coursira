package by.epam.coursira.model;

import java.util.Objects;
import java.util.StringJoiner;

public class LoginModel extends JspModelAbstract {
  private String errorMessage;

  public LoginModel() {}

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    LoginModel that = (LoginModel) o;
    return Objects.equals(errorMessage, that.errorMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), errorMessage);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", LoginModel.class.getSimpleName() + "[", "]")
        .add("errorMessage='" + errorMessage + "'")
        .toString();
  }
}
