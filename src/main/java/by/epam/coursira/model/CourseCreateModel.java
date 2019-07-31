package by.epam.coursira.model;

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
}
