package by.epam.coursira.tag;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class LocalTimeTag extends TagSupport {
  //  private ZoneId zoneId;
  private Instant time;
  private ZoneOffset zoneOffset;

  public void setZoneOffset(ZoneOffset zoneOffset) {
    this.zoneOffset = zoneOffset;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  @Override
  public int doStartTag() throws JspException {
    //    String localDate = time.atZone(zoneId).format(DateTimeFormatter.ISO_LOCAL_DATE);
    //    String localTime = time.atZone(zoneId).format(DateTimeFormatter.ISO_LOCAL_TIME);
    String localTime = time.atOffset(zoneOffset).format(DateTimeFormatter.ISO_LOCAL_TIME);
    JspWriter out = pageContext.getOut();
    try {
      //      out.write(localDate + ", " + localTime);
      out.write(localTime);
    } catch (IOException e) {
      throw new JspException(e);
    }

    return SKIP_BODY;
  }
}
