package by.epam.coursira.model;

import by.epam.coursira.entity.Lecture;
import java.util.List;

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
}
