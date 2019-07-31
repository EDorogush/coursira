package by.epam.coursira.model;

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
}
