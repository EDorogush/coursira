package by.epam.coursira.model;

import java.util.Objects;
import java.util.StringJoiner;

public class RegistrationCompletedModel extends JspModelAbstract {
  private boolean activate;
  private String textMessage;

  public boolean isActivate() {
    return activate;
  }

  public void setActivate(boolean activate) {
    this.activate = activate;
  }

  public String getTextMessage() {
    return textMessage;
  }

  public void setTextMessage(String textMessage) {
    this.textMessage = textMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RegistrationCompletedModel)) return false;
    if (!super.equals(o)) return false;
    RegistrationCompletedModel that = (RegistrationCompletedModel) o;
    return activate == that.activate &&
      Objects.equals(textMessage, that.textMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), activate, textMessage);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RegistrationCompletedModel.class.getSimpleName() + "[", "]")
      .add("activate=" + activate)
      .add("textMessage='" + textMessage + "'")
      .toString();
  }
}
