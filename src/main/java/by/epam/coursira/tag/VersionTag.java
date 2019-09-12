package by.epam.coursira.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;


public class VersionTag extends TagSupport {
  @Override
  public int doStartTag() throws JspException {
    ResourceBundle bundle = ResourceBundle.getBundle("application", Locale.ENGLISH);
    String version = bundle.getString("version");
    JspWriter out = pageContext.getOut();
    try {
      out.write(version);
    } catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }
}

