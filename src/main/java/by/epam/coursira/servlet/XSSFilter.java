package by.epam.coursira.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebFilter(urlPatterns = {"/*"})
public class XSSFilter implements Filter {
  private static final Logger logger = LogManager.getLogger();

  @Override
  public void init(FilterConfig filterConfig) {
    logger.info("filter XSS init");
  }

  @Override
  public void destroy() {
    logger.info("filter XSS destroy");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    chain.doFilter(new XSSRequestWrapper((HttpServletRequest) request), response);
  }

  static class XSSRequestWrapper extends HttpServletRequestWrapper {
    XSSRequestWrapper(HttpServletRequest servletRequest) {
      super(servletRequest);
    }

    @Override
    public String[] getParameterValues(String parameter) {
      String[] values = super.getParameterValues(parameter);
      if (values == null) {
        return null;
      }
      int count = values.length;
      String[] encodedValues = new String[count];
      for (int i = 0; i < count; i++) {
        encodedValues[i] = stripXSS(values[i]);
      }
      return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
      String value = super.getParameter(parameter);
      return stripXSS(value);
    }

    @Override
    public String getHeader(String name) {
      String value = super.getHeader(name);
      return stripXSS(value);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
      Map<String, String[]> map = super.getParameterMap();
      Map<String, String[]> cleanMap = new HashMap<>(map.size());
      for (Map.Entry<String, String[]> entry : map.entrySet()) {
        cleanMap.put(
            stripXSS(entry.getKey()),
            Arrays.stream(entry.getValue()).map(this::stripXSS).toArray(String[]::new));
      }
      return cleanMap;
    }

    private String stripXSS(String value) {
      if (value == null) {
        return null;
      }
      value = value.replaceAll("<\\s*/?\\s*script\\s*>", "");
      return value;
    }
  }
}
