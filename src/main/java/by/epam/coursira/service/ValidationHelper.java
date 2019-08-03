package by.epam.coursira.service;

import by.epam.coursira.entity.Lecture;
import by.epam.coursira.exception.ClientServiceException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javax.servlet.http.Part;


public final class ValidationHelper {

  private static final int MAX_AGE = 100;
  private static final int MIN_AGE = 15;
  private static final int MAX_IMAGE_SIZE = 1 * 1024 * 1024; // 1 Mb in bytes
  private static final int MAX_TEXT_SIZE = 128;
  private static final int MIN_COURSE_CAPACITY = 2;
  private static final int MAX_COURSE_CAPACITY = 50;
  private static final int MAX_LECTURE_DURATION_IN_HOURS = 4;
  private static Set<String> imageTypes = new HashSet<>();

  static {
    imageTypes.add("image/png");
    imageTypes.add("image/jpg");
    imageTypes.add("image/jpeg");
  }

  private ValidationHelper() {

  }

  private static String filterTextFromJunk(String str) {
    return str.trim().replaceAll("\\s\\s+", " ");
  }

  static String validateText(String text, Locale messageLocale, String fieldName)
      throws ClientServiceException {

    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
    if (text.isBlank()) {
      throw new ClientServiceException(bundle.getString("BLANK_TEXT") + fieldName);
    }
    if (text.length() > MAX_TEXT_SIZE) {
      StringBuffer buffer =
          new StringBuffer()
              .append(bundle.getString("TOO_LARGE_TEXT"))
              .append(fieldName)
              .append(bundle.getString("MUST_BE_LESS"))
              .append(MAX_TEXT_SIZE);
      throw new ClientServiceException(buffer.toString());
    }
    return filterTextFromJunk(text);
  }

  static String validateTextNullable(String text, Locale messageLocale, String fieldName)
      throws ClientServiceException {
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
    if (text.isBlank()) {
      return null;
    }
    if (text.length() > MAX_TEXT_SIZE) {
      StringBuffer buffer =
          new StringBuffer()
              .append(bundle.getString("TOO_LARGE_TEXT"))
              .append(fieldName)
              .append(bundle.getString("MUST_BE_LESS"))
              .append(MAX_TEXT_SIZE);
      throw new ClientServiceException(buffer.toString());
    }
    return filterTextFromJunk(text);
  }

  static boolean checkAge(int age, Locale messageLocale) throws ClientServiceException {
    if ((age < MIN_AGE) || (age > MAX_AGE)) {
      ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
      throw new ClientServiceException(
          String.format(bundle.getString("WRONG_AGE") + "[%d, %d]", MIN_AGE, MAX_AGE));
    }
    return true;
  }

  static boolean checkImage(Part image, Locale messageLocale) throws ClientServiceException {
    if (!imageTypes.contains(image.getContentType())) {
      ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
      throw new ClientServiceException(bundle.getString("WRONG_IMAGE_TYPE"));
    }
    if (image.getSize() > MAX_IMAGE_SIZE) {
      ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
      throw new ClientServiceException(
          String.format(
              bundle.getString("TOO_LARGE_FILE") + " %d  Mb", MAX_IMAGE_SIZE / 1024 / 1024));
    }
    return true;
  }

  static void checkLimit(int limit, Locale messageLocale) throws ClientServiceException {
    if (limit <= 0) {
      ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
      throw new ClientServiceException(
          String.format(bundle.getString("LIMIT_VALUE_MUST_BE_POSITIVE_BUT_FOUND") + " %d", limit));
    }
  }

  static void checkOffSet(int offset, Locale messageLocale) throws ClientServiceException {
    if (offset < 0) {
      ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
      throw new ClientServiceException(
          String.format(
              bundle.getString("OFFSET_VALUE_MUST_BE_POSITIVE_BUT_FOUND") + " %d", offset));
    }
  }

  static boolean checkCourseCapacity(int capacity, Locale messageLocale)
      throws ClientServiceException {
    if (capacity < MIN_COURSE_CAPACITY || capacity > MAX_COURSE_CAPACITY) {
      ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
      throw new ClientServiceException(
          String.format(
              bundle.getString("COURSE_CAPACITY_MUST_BE_WITHIN ") + "[ %d, %d]",
              MIN_COURSE_CAPACITY,
              MAX_COURSE_CAPACITY));
    }
    return true;
  }

  static void checkLectureDatesTimes(Instant begin, Instant end, Locale messageLocale)
      throws ClientServiceException {
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
    if (begin.isAfter(end)
        || begin.isBefore(Instant.now())
        || Duration.between(begin, end).toHours() > MAX_LECTURE_DURATION_IN_HOURS
        || begin.equals(end)) {
      throw new ClientServiceException(bundle.getString("LECTURE_TIME_WRONG"));
    }
  }

  static boolean checkScheduleHaveCrossing(List<Lecture> schedule, Locale messageLocale)
      throws ClientServiceException {
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", messageLocale);
    schedule.sort(Comparator.comparing(Lecture::getStartTime));
    Iterator<Lecture> iter = schedule.iterator();
    Lecture previous = iter.next(); // 1 lecture is always be here
    while (iter.hasNext()) {
      Lecture current = iter.next();
      if (current.getStartTime().isBefore(previous.getEndTime())) {
        throw new ClientServiceException(bundle.getString("START_BEFORE_END"));
      }
      if (current.getStartTime().equals(previous.getEndTime())) {
        throw new ClientServiceException(bundle.getString("START_EQUALS_END"));
      }
      previous = current;
    }
    return false;
  }
}
