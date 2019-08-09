package by.epam.coursira.command;

import by.epam.coursira.model.JspModelAbstract;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * This Class presents result of processing request from the client by {@link Command}. Two options
 * of the result is possible: when request should be forwarded to JSP page and when request should
 * be redirected to another URL. That's why two constructors are possible.
 */
public class CommandResult {
  private final boolean isForward;
  private final String jsp;
  private final JspModelAbstract jspModel;
  private final String pageToRedirect;

  /**
   * This constructor should be used when result of request processing should be forward to JSP
   * page. {@link JspModelAbstract} then also needed.
   *
   * @param jsp {@link String} location address of file represents jsp page.
   * @param jspModel {@link JspModelAbstract} model for filling the JSP page.
   */
  public CommandResult(String jsp, JspModelAbstract jspModel) {
    this.isForward = true;
    this.jsp = jsp;
    this.jspModel = jspModel;
    this.pageToRedirect = null;
  }

  /**
   * This constructor method should be used when result of request processing should be redirected
   * to another page.
   *
   * @param page {@link String} URL address of the target page
   */
  public CommandResult(String page) {
    this.isForward = false;
    this.jsp = null;
    this.jspModel = null;
    this.pageToRedirect = page;
  }

  public JspModelAbstract getJspModel() {
    return jspModel;
  }

  public boolean isForward() {
    return isForward;
  }

  public String getJsp() {
    return jsp;
  }

  public String getPageToRedirect() {
    return pageToRedirect;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CommandResult.class.getSimpleName() + "[", "]")
        .add("isForward=" + isForward)
        .add("jsp='" + jsp + "'")
        .add("jspModel=" + jspModel)
        .add("pageToRedirect='" + pageToRedirect + "'")
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommandResult)) return false;
    CommandResult that = (CommandResult) o;
    return isForward == that.isForward
        && Objects.equals(jsp, that.jsp)
        && Objects.equals(jspModel, that.jspModel)
        && Objects.equals(pageToRedirect, that.pageToRedirect);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isForward, jsp, jspModel, pageToRedirect);
  }
}
