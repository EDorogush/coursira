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

@WebFilter(urlPatterns = {"/static/*"})
public class StaticFilter implements Filter {
  private static final Logger logger = LogManager.getLogger();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    logger.debug("in static filter");
    request.getServletContext().getNamedDispatcher("default").forward(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig){
    logger.info("static filter init");
  }

  @Override
  public void destroy() {
    logger.info("static filter destroy");
  }
}
