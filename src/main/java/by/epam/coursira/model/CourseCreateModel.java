package by.epam.coursira.model;

import java.util.Objects;
import java.util.StringJoiner;

public class CourseCreateModel extends JspModelAbstract {
  private String title;
  private String description;
  private int capacity;
  private String errorDataMessage;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
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
    if (!(o instanceof CourseCreateModel)) return false;
    if (!super.equals(o)) return false;
    CourseCreateModel that = (CourseCreateModel) o;
    return capacity == that.capacity &&
      Objects.equals(title, that.title) &&
      Objects.equals(description, that.description) &&
      Objects.equals(errorDataMessage, that.errorDataMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), title, description, capacity, errorDataMessage);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CourseCreateModel.class.getSimpleName() + "[", "]")
        .add("title='" + title + "'")
        .add("description='" + description + "'")
        .add("capacity=" + capacity)
        .add("errorDataMessage='" + errorDataMessage + "'")
        .toString();
  }
}
