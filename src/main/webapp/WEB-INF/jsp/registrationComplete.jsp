<%@include file="parts/meta.jsp" %>
<jsp:useBean id="model" class="by.epam.coursira.model.RegistrationCompletedModel" scope="request"/>
<fmt:setLocale value="${model.currentLocale}"/>

<fmt:bundle basename="pagecontent" prefix="registration_complete_page.">
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>
  <div class="container">
    <core:if test="${model.activate}">
      <div class="alert alert-success" role="alert">
          ${model.textMessage}
      </div>
    </core:if>
    <core:if test="${!model.activate}">
      <div class="alert aler®t-danger" role="alert">
          ${model.textMessage}
      </div>
    </core:if>
  </div>

 <%@include file="parts/footer.jsp" %>
  </body>
</fmt:bundle>
</html>
