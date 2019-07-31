package by.epam.coursira.model;

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
}
