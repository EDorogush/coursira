package by.epam.coursira.model;

import by.epam.coursira.entity.Role;
import java.util.Objects;
import java.util.StringJoiner;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SignUpModel)) return false;
    if (!super.equals(o)) return false;
    SignUpModel that = (SignUpModel) o;
    return Objects.equals(errorDataMessage, that.errorDataMessage)
        && Objects.equals(firstName, that.firstName)
        && Objects.equals(lastName, that.lastName)
        && role == that.role
        && Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), errorDataMessage, firstName, lastName, role, email);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SignUpModel.class.getSimpleName() + "[", "]")
        .add("errorDataMessage='" + errorDataMessage + "'")
        .add("firstName='" + firstName + "'")
        .add("lastName='" + lastName + "'")
        .add("role=" + role)
        .add("email='" + email + "'")
        .toString();
  }
}
