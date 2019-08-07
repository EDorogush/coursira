package by.epam.coursira.model;

import by.epam.coursira.entity.Lecture;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class PersonalModel extends JspModelAbstract {
  private int courseAmount;
  private List<Lecture> schedule;
  private boolean hasNextPage;
  private int currentPageIndex;

  public int getCourseAmount() {
    return courseAmount;
  }

  public void setCourseAmount(int courseAmount) {
    this.courseAmount = courseAmount;
  }

  public List<Lecture> getSchedule() {
    return schedule;
  }

  public void setSchedule(List<Lecture> schedule) {
    this.schedule = schedule;
  }

  public boolean isHasNextPage() {
    return hasNextPage;
  }

  public void setHasNextPage(boolean hasNextPage) {
    this.hasNextPage = hasNextPage;
  }

  public int getCurrentPageIndex() {
    return currentPageIndex;
  }

  public void setCurrentPageIndex(int currentPageIndex) {
    this.currentPageIndex = currentPageIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    PersonalModel that = (PersonalModel) o;
    return courseAmount == that.courseAmount
        && hasNextPage == that.hasNextPage
        && currentPageIndex == that.currentPageIndex
        && Objects.equals(schedule, that.schedule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), courseAmount, schedule, hasNextPage, currentPageIndex);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", PersonalModel.class.getSimpleName() + "[", "]")
        .add("courseAmount=" + courseAmount)
        .add("schedule=" + schedule)
        .add("hasNextPage=" + hasNextPage)
        .add("currentPageIndex=" + currentPageIndex)
        .toString();
  }
}
