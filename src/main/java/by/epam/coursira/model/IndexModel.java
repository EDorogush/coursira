package by.epam.coursira.model;

import java.util.StringJoiner;

public class IndexModel extends JspModelAbstract {
  private int coursesAmount;
  private int lecturerAmount;
  private int studentsAmount;

  public int getCoursesAmount() {
    return coursesAmount;
  }

  public void setCoursesAmount(int coursesAmount) {
    this.coursesAmount = coursesAmount;
  }

  public int getLecturerAmount() {
    return lecturerAmount;
  }

  public void setLecturerAmount(int lecturerAmount) {
    this.lecturerAmount = lecturerAmount;
  }

  public int getStudentsAmount() {
    return studentsAmount;
  }

  public void setStudentsAmount(int studentsAmount) {
    this.studentsAmount = studentsAmount;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", IndexModel.class.getSimpleName() + "[", "]")
        .add("principal=" + this.getPrincipal())
        .add("coursesAmount=" + coursesAmount)
        .add("lecturerAmount=" + lecturerAmount)
        .add("studentsAmount=" + studentsAmount)
        .toString();
  }
}
