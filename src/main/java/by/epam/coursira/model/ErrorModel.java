package by.epam.coursira.model;

public class ErrorModel  extends  JspModelAbstract{
  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
