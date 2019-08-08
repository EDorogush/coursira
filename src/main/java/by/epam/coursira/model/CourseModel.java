package by.epam.coursira.model;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecturer;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class CourseModel extends JspModelAbstract {
  private List<Course> courses;
  private int currentPageIndex;
  private boolean hasNextPage;
  private boolean isLecturerCourses;
  private boolean isPersonal;
  private Lecturer lecturer;

  public List<Course> getCourses() {
    return courses;
  }

  public void setCourses(List<Course> courses) {
    this.courses = courses;
  }

  public int getCurrentPageIndex() {
    return currentPageIndex;
  }

  public void setCurrentPageIndex(int currentPageIndex) {
    this.currentPageIndex = currentPageIndex;
  }

  public boolean isHasNextPage() {
    return hasNextPage;
  }

  public void setHasNextPage(boolean hasNextPage) {
    this.hasNextPage = hasNextPage;
  }

  public boolean isLecturerCourses() {
    return isLecturerCourses;
  }

  public void setLecturerCourses(boolean lecturerCourses) {
    isLecturerCourses = lecturerCourses;
  }

  public Lecturer getLecturer() {
    return lecturer;
  }

  public void setLecturer(Lecturer lecturer) {
    this.lecturer = lecturer;
  }

  public boolean isPersonal() {
    return isPersonal;
  }

  public void setPersonal(boolean personal) {
    isPersonal = personal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CourseModel)) return false;
    if (!super.equals(o)) return false;
    CourseModel that = (CourseModel) o;
    return currentPageIndex == that.currentPageIndex &&
      hasNextPage == that.hasNextPage &&
      isLecturerCourses == that.isLecturerCourses &&
      isPersonal == that.isPersonal &&
      Objects.equals(courses, that.courses) &&
      Objects.equals(lecturer, that.lecturer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), courses, currentPageIndex, hasNextPage, isLecturerCourses, isPersonal,
      lecturer);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CourseModel.class.getSimpleName() + "[", "]")
      .add("courses=" + courses)
      .add("currentPageIndex=" + currentPageIndex)
      .add("hasNextPage=" + hasNextPage)
      .add("isLecturerCourses=" + isLecturerCourses)
      .add("isPersonal=" + isPersonal)
      .add("lecturer=" + lecturer)
      .toString();
  }
}
