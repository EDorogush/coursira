package by.epam.coursira.model;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecturer;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class CourseUpdateModel extends JspModelAbstract {
  private Course course;
  private String errorCourseDataMessage;
  private String errorScheduleMessage;
  private List<Lecturer> lecturers;

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

  public List<Lecturer> getLecturers() {
    return lecturers;
  }

  public void setAllLecturers(List<Lecturer> alllecturers) {
    lecturers = alllecturers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CourseUpdateModel)) return false;
    if (!super.equals(o)) return false;
    CourseUpdateModel that = (CourseUpdateModel) o;
    return Objects.equals(course, that.course)
        && Objects.equals(errorCourseDataMessage, that.errorCourseDataMessage)
        && Objects.equals(errorScheduleMessage, that.errorScheduleMessage)
        && Objects.equals(lecturers, that.lecturers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(), course, errorCourseDataMessage, errorScheduleMessage, lecturers);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CourseUpdateModel.class.getSimpleName() + "[", "]")
        .add("course=" + course)
        .add("errorCourseDataMessage='" + errorCourseDataMessage + "'")
        .add("errorScheduleMessage='" + errorScheduleMessage + "'")
        .add("lecturers=" + lecturers)
        .toString();
  }
}
