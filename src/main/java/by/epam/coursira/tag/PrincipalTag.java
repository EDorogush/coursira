package by.epam.coursira.tag;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

@SuppressWarnings("serial")
public class PrincipalTag extends TagSupport {
  private Principal principal;
  private Locale currentLocale;

  public void setPrincipal(Principal principal) {
    this.principal = principal;
  }

  public void setCurrentLocale(Locale currentLocale) {
    this.currentLocale = currentLocale;
  }

  @Override
  public int doStartTag() throws JspException {
    Locale.setDefault(currentLocale);
    ResourceBundle bundle = ResourceBundle.getBundle("pagecontent", Locale.getDefault());
    JspWriter out = pageContext.getOut();
    try {
      String path = pageContext.getServletContext().getContextPath();
      if (principal.getUser().getRole() == Role.ANONYMOUS) {
        out.write(
            "  <li><a href=\""
                + path
                + "/login\"><span class=\"glyphicon glyphicon-user\"></span>"
                + bundle.getString("navigation.login")
                + "</a></li>\n"
                + "      <li><a href=\""
                + path
                + "/sign\"><span class=\"glyphicon glyphicon-log-in\"></span>"
                + bundle.getString("navigation.sign_up")
                + "</a></li>");
      } else {
        out.write(
            "<li><p class=\"navbar-brand\">"
                + bundle.getString("navigation.welcomeUser")
                + "</p>\n"
                + "      <p class=\"navbar-brand\">"
                + principal.getUser().getFirstName()
                + " "
                + principal.getUser().getLastName()
                + "</p></li>\n"
                + "      <li><form name=\"logoutForm\" method=\"post\" action=\""
                + path
                + "/logout\">\n"
                + "       \t\t\t <button type=\"submit\" class=\"btn btn-outline-light\">logout</button>\n"
                + "      \t</form>\n"
                + "      </li>\n"
                + "     ");
      }
    } catch (IOException e) {
      throw new JspException(e);
    }

    return SKIP_BODY;
  }
}
