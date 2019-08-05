package by.epam.coursira.servlet;

public final class CoursiraUrlPatterns {

  public static final String INDEX = "/";
  public static final String COURSES = "/courses";
  public static final String NEW_COURSE = "/courses/newCourse";
  public static final String COURSE_DETAILS = "/courses/([^/?[A-Z]]+)(\\?.*)?";
  public static final String COURSE_SUBSCRIBE = "/courses/([^/?]+)/subscriptions(\\?.*)?";
  public static final String COURSE_UPDATE = "/courses/([^/?[A-Z]]+)/update";
  public static final String LANGUAGE = "/language";
  public static final String LOGIN = "/login";
  public static final String LOGOUT = "/logout";
  public static final String PAGE_NOT_FOUND = "/page not found";
  public static final String PERSONAL = "/personal";
  public static final String UPDATE_PERSONAL = "/personal/update";
  public static final String SIGN_IN = "/sign";
  public static final String REGISTRATION_CONFIRM = "/registration";

  private CoursiraUrlPatterns() {}
}
