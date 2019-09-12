<%@include file="parts/meta.jsp" %>

<fmt:bundle basename="pagecontent" prefix="error_page.">
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <div class="alert alert-warning" role="alert">
    <a href="http://www.nooooooooooooooo.com"><fmt:message key="message"/></a>
  </div>
 <%@include file="parts/footer.jsp" %>
  </body>

  </html>
</fmt:bundle>
