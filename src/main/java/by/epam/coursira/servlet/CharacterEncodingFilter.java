package by.epam.coursira.servlet;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebFilter(urlPatterns = {"/*"})
public class CharacterEncodingFilter implements Filter {
  private static final Logger logger = LogManager.getLogger();

  @Override
  public void init(FilterConfig filterConfig) {
    logger.info("encoding filter init");
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    logger.debug("in encoding filter");
    servletRequest.setCharacterEncoding("UTF-8");
    servletResponse.setContentType("text/html; charset=UTF-8");
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {
    logger.info("encoding filter destroy");
  }
}
