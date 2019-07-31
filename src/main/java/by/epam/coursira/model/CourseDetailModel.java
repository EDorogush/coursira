package by.epam.coursira.model;

import by.epam.coursira.entity.Course;

public class CourseDetailModel extends JspModelAbstract {
  private Course course;
  private int currentPageIndex;
  private boolean hasNextPage;
  private boolean hasFreeSpot;
  private boolean isInUserList;
  private boolean ableToJoin; // available for student;

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
}
