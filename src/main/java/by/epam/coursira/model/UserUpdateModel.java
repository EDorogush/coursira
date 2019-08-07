package by.epam.coursira.model;

import java.util.Objects;
import java.util.StringJoiner;

public class UserUpdateModel extends JspModelAbstract {
  private String errorImageMessage;
  private String errorDataMessage;

  public String getErrorImageMessage() {
    return errorImageMessage;
  }

  public void setErrorImageMessage(String errorImageMessage) {
    this.errorImageMessage = errorImageMessage;
  }

  public String getErrorDataMessage() {
    return errorDataMessage;
  }

  public void setErrorDataMessage(String errorDataMessage) {
    this.errorDataMessage = errorDataMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    UserUpdateModel that = (UserUpdateModel) o;
    return Objects.equals(errorImageMessage, that.errorImageMessage) &&
      Objects.equals(errorDataMessage, that.errorDataMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), errorImageMessage, errorDataMessage);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", UserUpdateModel.class.getSimpleName() + "[", "]")
      .add("errorImageMessage='" + errorImageMessage + "'")
      .add("errorDataMessage='" + errorDataMessage + "'")
      .toString();
  }
}
