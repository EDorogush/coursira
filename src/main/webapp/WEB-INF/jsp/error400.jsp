<%@include file="parts/meta.jsp" %>
<jsp:useBean id="model" class="by.epam.coursira.model.ErrorModel" scope="request"/>
<fmt:setLocale value="${model.currentLocale}"/>
<fmt:bundle basename="pagecontent" prefix="client_error_page.">
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>
  <div class="container">
    <div class="alert alert-danger" role="alert">
        ${model.errorMessage}
    </div>

  </div>
   <%@include file="parts/footer.jsp" %>
  </body>
  </html>
</fmt:bundle>
