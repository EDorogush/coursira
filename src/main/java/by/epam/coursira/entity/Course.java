package by.epam.coursira.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Course extends AbstractEntity {
  private int id;
  private String title;
  private String description;
  private int capacity;
  private int studentsAmount;
  private boolean ready;
  private List<Lecturer> lecturers;
  private List<Lecture> lectures;

  public Course() {
    lecturers = new ArrayList<>();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  public int getStudentsAmount() {
    return studentsAmount;
  }

  public void setStudentsAmount(int studentsAmount) {
    this.studentsAmount = studentsAmount;
  }

  public List<Lecturer> getLecturers() {
    return lecturers;
  }

  public void setLecturers(List<Lecturer> lecturers) {
    this.lecturers = lecturers;
  }

  public void addLecturer(Lecturer lecturer) {
    lecturers.add(lecturer);
  }

  public List<Lecture> getLectures() {
    return lectures;
  }

  public void setLectures(List<Lecture> lectures) {
    this.lectures = lectures;
  }

  public boolean isReady() {
    return ready;
  }

  public void setReady(boolean ready) {
    this.ready = ready;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Course.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("title='" + title + "'")
        .add("description='" + description + "'")
        .add("capacity=" + capacity)
        .add("studentsAmount=" + studentsAmount)
        .add("lecturers=" + lecturers)
        .toString();
  }

  public static class Builder {
    private Course course;

    public Builder() {
      course = new Course();
    }

    public Course build() {
      return course;
    }

    public Builder withId(int id) {
      course.id = id;
      return this;
    }

    public Builder withTitle(String title) {
      course.title = title;
      return this;
    }

    public Builder withDescription(String description) {
      course.description = description;
      return this;
    }

    public Builder withCapacity(int capacity) {
      course.capacity = capacity;
      return this;
    }

    public Builder withStudentsAmount(int studentsAmount) {
      course.studentsAmount = studentsAmount;
      return this;
    }

    public Builder withReady(boolean ready) {
      course.ready = ready;
      return this;
    }

    public Builder withLecturers(List<Lecturer> lecturers) {
      course.lecturers = lecturers;
      return this;
    }

    public Builder withLectures(List<Lecture> lectures) {
      course.lectures = lectures;
      return this;
    }
  }
}
