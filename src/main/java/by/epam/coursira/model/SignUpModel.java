package by.epam.coursira.model;

import by.epam.coursira.entity.Role;

public class SignUpModel extends JspModelAbstract {
  private String errorDataMessage;
  private String firstName;
  private String lastName;
  private Role role;
  private String email;

  public String getErrorDataMessage() {
    return errorDataMessage;
  }

  public void setErrorDataMessage(String errorDataMessage) {
    this.errorDataMessage = errorDataMessage;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
