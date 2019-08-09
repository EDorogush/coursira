package by.epam.coursira.model;

import java.util.Objects;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IndexModel)) return false;
    if (!super.equals(o)) return false;
    IndexModel that = (IndexModel) o;
    return coursesAmount == that.coursesAmount
        && lecturerAmount == that.lecturerAmount
        && studentsAmount == that.studentsAmount;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), coursesAmount, lecturerAmount, studentsAmount);
  }
}
