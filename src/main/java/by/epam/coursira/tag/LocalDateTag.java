package by.epam.coursira.tag;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class LocalDateTag extends TagSupport {
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
    String localDate = time.atOffset(zoneOffset).format(DateTimeFormatter.ISO_LOCAL_DATE);
    JspWriter out = pageContext.getOut();
    try {
      out.write(localDate);
    } catch (IOException e) {
      throw new JspException(e);
    }

    return SKIP_BODY;
  }
}
