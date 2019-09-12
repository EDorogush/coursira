<%@include file="parts/meta.jsp" %>

<jsp:useBean id="model" class="by.epam.coursira.model.CourseCreateModel" scope="request"/>
<fmt:setLocale value="${model.currentLocale}"/>
<fmt:bundle basename="pagecontent" prefix="course_create_page.">
  <html>
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>


  <div class="container">
    <form name="CreateCourseForm" method="post" action="${pageContext.request.contextPath}/courses/newCourse">
      <fmt:bundle basename="pagecontent" prefix="course.">
        <div class="form-group">
          <label><fmt:message key="title"/>&#42 :</label>
          <input type="text" name="title" value="${model.title}" minlength="1" maxlength="128" class="form-control">
        </div>

        <div class="form-group">
          <label><fmt:message key="description"/>&#42 :</label>
          <input type="text" name="description" value="${model.description}" minlength="1" maxlength="128"
                 class="form-control">
        </div>

        <div class="form-group">
          <label><fmt:message key="capacity"/></label>
          <select name="capacity">
            <core:forEach begin="2" end="50" varStatus="loop">
              <option value="${loop.index}"
                <core:if test="${model.capacity == loop.index}">
                  selected
                </core:if>>${loop.index}</option>
            </core:forEach>
          </select>
        </div>
        <div class="container">
          <p class="text-danger">${model.errorDataMessage}</p>
        </div>
      </fmt:bundle>
      <input type="submit" class="btn btn-success" value="<fmt:message key="submit"/>"/>

    </form>

  </div>

 <%@include file="parts/footer.jsp" %>
  </body>
  </html>

</fmt:bundle>

