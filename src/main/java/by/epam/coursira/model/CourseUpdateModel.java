package by.epam.coursira.model;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecturer;
import java.util.List;

public class CourseUpdateModel extends JspModelAbstract {
  private Course course;
  private String errorCourseDataMessage;
  private String errorScheduleMessage;
  private List<Lecturer> Alllecturers;

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  public String getErrorCourseDataMessage() {
    return errorCourseDataMessage;
  }

  public void setErrorCourseDataMessage(String errorCourseDataMessage) {
    this.errorCourseDataMessage = errorCourseDataMessage;
  }

  public String getErrorScheduleMessage() {
    return errorScheduleMessage;
  }

  public void setErrorScheduleMessage(String errorScheduleMessage) {
    this.errorScheduleMessage = errorScheduleMessage;
  }

  public List<Lecturer> getAlllecturers() {
    return Alllecturers;
  }

  public void setAlllecturers(List<Lecturer> alllecturers) {
    Alllecturers = alllecturers;
  }
}
