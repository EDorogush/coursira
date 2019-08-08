package by.epam.coursira.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.StringJoiner;

public class Lecture {
  private int id;
  private String description;
  private Instant startTime;
  private Instant endTime;
  private Lecturer lecturer;
  private int courseId;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  public Lecturer getLecturer() {
    return lecturer;
  }

  public void setLecturer(Lecturer lecturer) {
    this.lecturer = lecturer;
  }

  public int getCourseId() {
    return courseId;
  }

  public void setCourseId(int courseId) {
    this.courseId = courseId;
  }

  public static class Builder {
    private Lecture lecture;

    public Builder() {
      lecture = new Lecture();
    }

    public Lecture build() {
      return lecture;
    }

    public Builder withLectureId(int id) {
      lecture.id = id;
      return this;
    }

    public Builder withDescription(String description) {
      lecture.description = description;
      return this;
    }

    public Builder withTimeStart(Instant timeStart) {
      lecture.startTime = timeStart;
      return this;
    }

    public Builder withTimeEnd(Instant timeEnd) {
      lecture.endTime = timeEnd;
      return this;
    }

    public Builder withLecturer(Lecturer lecturer) {
      lecture.lecturer = lecturer;
      return this;
    }

    public Builder withCourseId(int courseId) {
      lecture.courseId = courseId;
      return this;
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Lecture.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("description='" + description + "'")
        .add("startTime=" + startTime)
        .add("endTime=" + endTime)
        .add("lecturer=" + lecturer)
        .add("courseId=" + courseId)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Lecture)) return false;
    Lecture lecture = (Lecture) o;
    return id == lecture.id
        && courseId == lecture.courseId
        && Objects.equals(description, lecture.description)
        && Objects.equals(startTime, lecture.startTime)
        && Objects.equals(endTime, lecture.endTime)
        && Objects.equals(lecturer, lecture.lecturer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, description, startTime, endTime, lecturer, courseId);
  }
}
