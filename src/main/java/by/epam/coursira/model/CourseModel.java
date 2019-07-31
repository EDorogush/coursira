package by.epam.coursira.model;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecturer;
import java.util.List;

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
}
