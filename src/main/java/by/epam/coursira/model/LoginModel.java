package by.epam.coursira.model;

public class LoginModel extends JspModelAbstract {
  private String errorMessage;

  public LoginModel() {}

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
