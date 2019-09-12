package by.epam.coursira.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class AssemblyTimeTag extends TagSupport {
  @Override
  public int doStartTag() throws JspException {
    ResourceBundle bundle = ResourceBundle.getBundle("application", Locale.ENGLISH);
    String version = bundle.getString("buildDate");
    Instant time = Instant.ofEpochMilli(Long.parseLong(version));
    String localDateTime = time.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    JspWriter out = pageContext.getOut();
    try {
      out.write(localDateTime);
    } catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }
}


