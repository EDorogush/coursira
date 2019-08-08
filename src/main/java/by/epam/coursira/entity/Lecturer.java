package by.epam.coursira.entity;

import java.util.Objects;
import java.util.StringJoiner;

public class Lecturer {
  private int id;
  private String firstName;
  private String lastName;

  public Lecturer(int id, String firstName, String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Lecturer)) return false;
    Lecturer lecturer = (Lecturer) o;
    return id == lecturer.id &&
      Objects.equals(firstName, lecturer.firstName) &&
      Objects.equals(lastName, lecturer.lastName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName);
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", Lecturer.class.getSimpleName() + "[", "]")
      .add("id=" + id)
      .add("firstName='" + firstName + "'")
      .add("lastName='" + lastName + "'")
      .toString();
  }
}
