<%@include file="parts/meta.jsp" %>
<jsp:useBean id="model" class="by.epam.coursira.model.CourseModel" scope="request"/>
<fmt:setLocale value="${model.currentLocale}"/>

<fmt:bundle basename="pagecontent" prefix="courses_page.">
  <html>
  <head>
    <title>Title</title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>


  <div class="container">
    <table class="table table-bordered table-striped">
      <tr>
        <fmt:bundle basename="pagecontent" prefix="course.">
          <td><fmt:message key="title"/></td>
          <td><fmt:message key="description"/></td>
          <td><fmt:message key="capacity"/></td>
          <td><fmt:message key="student_amount"/></td>
          <td><fmt:message key="lecturers"/></td>
          <core:if test="${model.principal.user.role == 'LECTURER'}">
            <td><fmt:message key="state"/></td>
          </core:if>
        </fmt:bundle>
        <td><fmt:message key="link_details"/></td>

      </tr>
      <core:forEach items="${model.courses}" var="course">
        <tr>
          <td>${course.title}</td>
          <td>${course.description}</td>
          <td>${course.capacity}</td>
          <td>${course.studentsAmount}</td>
          <td><core:forEach items="${course.lecturers}" var="lecturer">
            ${lecturer.firstName}  ${lecturer.lastName} </br>
          </core:forEach></td>
          <core:if test="${model.principal.user.role == 'LECTURER'}">
            <td>${course.ready}</td>
          </core:if>
          <td><a href="${pageContext.request.contextPath}/courses/${course.id}" class="glyphicon glyphicon-hand-right"></a></td>
        </tr>
      </core:forEach>
    </table>

      <%--paging--%>
    <ul class="pager">
      <fmt:bundle basename="pagecontent" prefix="paging.">
        <core:if test="${model.currentPageIndex>1}">
          <li>
            <core:choose>
              <core:when test="${param.containsKey('personal')}">
                <a
                  href="${pageContext.request.contextPath}/courses?personal=${param.get("personal")}&page=${model.currentPageIndex-1}"><fmt:message
                  key="previous"/></a>
              </core:when>
              <core:otherwise>
                <a
                  href="${pageContext.request.contextPath}/courses?personal=false&page=${model.currentPageIndex-1}"><fmt:message
                  key="previous"/></a>
              </core:otherwise>
            </core:choose>

          </li>
        </core:if>
        <core:if test="${model.hasNextPage}">
          <li>
            <core:choose>
              <core:when test="${param.containsKey('personal')}">
                <a
                  href="${pageContext.request.contextPath}/courses?personal=${param.get("personal")}&page=${model.currentPageIndex+1}"><fmt:message
                  key="next"/></a>
              </core:when>
              <core:otherwise>
                <a
                  href="${pageContext.request.contextPath}/courses?personal=false&page=${model.currentPageIndex+1}"><fmt:message
                  key="next"/></a>
              </core:otherwise>
            </core:choose>

          </li>
        </core:if>
      </fmt:bundle>
    </ul>


  </div>
   <%@include file="parts/footer.jsp" %>
  </body>
  </html>
</fmt:bundle>
