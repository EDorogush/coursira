<%@include file="parts/meta.jsp" %>
<jsp:useBean id="model" class="by.epam.coursira.model.IndexModel" scope="request"/>
<fmt:setLocale value="${model.currentLocale}"/>

<fmt:bundle basename="pagecontent" prefix="index_page.">
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>
  <div class="container">
    <h2><fmt:message key="WELCOME_TO"/> CoursIra <fmt:message key="COURSIRA_USEFUL_ONLINE_COURSES"/></h2>
    <p><fmt:message key="WE_SUGGEST"/> ${model.coursesAmount} <fmt:message
      key="COURSES_FROM"/> ${model.lecturerAmount}
      <fmt:message key="LECTURERS"/>
    </p>
    <p>${model.studentsAmount} <fmt:message key="STUDENT_HAVE_ALREADY_JOIN_US"/></p>

  </div>
  </body>
</fmt:bundle>
</html>
