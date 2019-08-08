package by.epam.coursira.model;

import by.epam.coursira.entity.Course;
import java.util.Objects;
import java.util.StringJoiner;

public class CourseDetailModel extends JspModelAbstract {
  private Course course;
  private int currentPageIndex;
  private boolean hasNextPage;
  private boolean hasFreeSpot;
  private boolean isInUserList;
  private boolean ableToJoin;

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  public int getCurrentPageIndex() {
    return currentPageIndex;
  }

  public void setCurrentPageIndex(int currentPageIndex) {
    this.currentPageIndex = currentPageIndex;
  }

  public boolean isHasFreeSpot() {
    return hasFreeSpot;
  }

  public void setHasFreeSpot(boolean hasFreeSpot) {
    this.hasFreeSpot = hasFreeSpot;
  }

  public boolean isInUserList() {
    return isInUserList;
  }

  public void setInUserList(boolean inUserList) {
    isInUserList = inUserList;
  }

  public boolean isAbleToJoin() {
    return ableToJoin;
  }

  public void setAbleToJoin(boolean ableToJoin) {
    this.ableToJoin = ableToJoin;
  }

  public boolean isHasNextPage() {
    return hasNextPage;
  }

  public void setHasNextPage(boolean hasNextPage) {
    this.hasNextPage = hasNextPage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CourseDetailModel)) return false;
    if (!super.equals(o)) return false;
    CourseDetailModel that = (CourseDetailModel) o;
    return currentPageIndex == that.currentPageIndex &&
      hasNextPage == that.hasNextPage &&
      hasFreeSpot == that.hasFreeSpot &&
      isInUserList == that.isInUserList &&
      ableToJoin == that.ableToJoin &&
      Objects.equals(course, that.course);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), course, currentPageIndex, hasNextPage, hasFreeSpot, isInUserList, ableToJoin);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CourseDetailModel.class.getSimpleName() + "[", "]")
      .add("course=" + course)
      .add("currentPageIndex=" + currentPageIndex)
      .add("hasNextPage=" + hasNextPage)
      .add("hasFreeSpot=" + hasFreeSpot)
      .add("isInUserList=" + isInUserList)
      .add("ableToJoin=" + ableToJoin)
      .toString();
  }
}
