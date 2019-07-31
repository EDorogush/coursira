<%@include file="parts/meta.jsp" %>

<jsp:useBean id="model" class="by.epam.coursira.model.CourseDetailModel" scope="request"/>

<fmt:setLocale value="${model.currentLocale}"/>
<fmt:bundle basename="pagecontent" prefix="course_detail_page.">
  <html>
  <head>
    <title>${model.course.title}</title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>

  <core:choose>
    <%--    student interface--%>
    <core:when test="${(model.principal.user.role == 'STUDENT')}">
      <core:choose>
        <core:when test="${model.inUserList}">
          <div class="alert alert-success">
            <strong><fmt:message key="YOU_ARE_SUBSCRIBED"/></strong>
          </div>
          <div>
            <form name="unsubscribeForm" method="post"
                  action="${pageContext.request.contextPath}/courses/${model.course.id}/subscriptions?subscribe=false">
              <input type="submit" class="btn btn-danger" value="<fmt:message key="unsubscribe"/>"/>
            </form>
          </div>
        </core:when>
        <core:when test="${!model.hasFreeSpot}">
          <div class="alert alert-warning">
            <strong><fmt:message key="COURSE_IS_FILLED"/></strong>
          </div>
        </core:when>
        <core:when test="${model.ableToJoin}">
          <form name="subscribeForm" method="post"
                action="${pageContext.request.contextPath}/courses/${model.course.id}/subscriptions?subscribe=true">
            <input type="submit" class="btn btn-success" value="<fmt:message key="JOIN_TO_COURSE"/>"/>
          </form>
        </core:when>
        <core:otherwise>
          <div class="alert alert-danger">
            <strong><fmt:message key="JOIN_DENIED_SCHEDULE_CONFLICT"/></strong>
          </div>
        </core:otherwise>
      </core:choose>
    </core:when>

    <%--    lecturer interface--%>
    <core:when test="${(model.principal.user.role == 'LECTURER')}">
      <core:if test="${model.inUserList}">
        <a href="${pageContext.request.contextPath}/courses/${model.course.id}/update" class="btn btn-primary"><fmt:message key="EDIT"/></a>
      </core:if>
    </core:when>

    <%--    anonyumous interface--%>
    <core:otherwise>

      <core:choose>
        <core:when test="${model.hasFreeSpot}">
          <div class="alert alert-danger">
            <strong><fmt:message key="JOINED_DENIED_MUST_LOGIN"/></strong>
          </div>
          <fmt:bundle basename="pagecontent" prefix="navigation.">
            <div class="btn-group">
              <a href="${pageContext.request.contextPath}/login" class="btn btn-success"><fmt:message key="login"/></a>
              <a href="${pageContext.request.contextPath}/sign" class="btn btn-success"><fmt:message key="sign_up"/></a>
            </div>


          </fmt:bundle>
        </core:when>
        <core:otherwise>
          <div class="alert alert-warning">
            <strong><fmt:message key="COURSE_IS_FILLED"/></strong>
          </div>
        </core:otherwise>
      </core:choose>


    </core:otherwise>
  </core:choose>


    <%--  course description--%>
  <div class="container">
    <fmt:bundle basename="pagecontent" prefix="course.">
      <h2>${model.course.title}</h2>
      <div class="well"><b><fmt:message key="description"/>: </b>${model.course.description}</div>
      <div class="well"><b><fmt:message key="capacity"/>: </b>${model.course.capacity}</div>
      <div class="well"><b><fmt:message key="student_amount"/>: </b>${model.course.studentsAmount}</div>
      <div class="well"><b><fmt:message key="lecturers"/>: </b></br>
        <core:forEach items="${model.course.lecturers}" var="lecturer">
          <%--          <p><a href="${pageContext.request.contextPath}/lecturers/${lecturer.id}">${lecturer.name}</a></p>--%>
          <p>${lecturer.firstName} ${lecturer.lastName}</p>
        </core:forEach>
      </div>
    </fmt:bundle>
  </div>
  <%@ page import="java.time.format.TextStyle" %>
  <div class="container">
    <fmt:bundle basename="pagecontent" prefix="schedule.">
      <table class="table table-bordered table-striped">
        <tr>
          <td><fmt:message key="description"/></td>
          <td><fmt:message key="date"/></td>
          <td><fmt:message key="start_time"/>, UTC
            (${model.principal.session.zoneOffset.getDisplayName(TextStyle.FULL,model.principal.session.language.locale)})
          </td>
          <td><fmt:message key="end_time"/>, UTC
            (${model.principal.session.zoneOffset.getDisplayName(TextStyle.FULL,model.principal.session.language.locale)})
          </td>
          <td><fmt:message key="lecturer"/></td>
        </tr>
        <core:forEach items="${model.course.lectures}" var="lecture">

          <tr>
            <td>${lecture.description}</td>
            <td><ctg:localDateTag zoneOffset="${model.principal.session.zoneOffset}" time="${lecture.startTime}"/></td>
            <td><ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}" time="${lecture.startTime}"/></td>
            <td><ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}" time="${lecture.endTime}"/></td>
            <td>${lecture.lecturer.firstName} ${lecture.lecturer.lastName}</td>
          </tr>
        </core:forEach>
      </table>
    </fmt:bundle>
  </div>

    <%--paging--%>
  <ul class="pager">
    <fmt:bundle basename="pagecontent" prefix="paging.">
      <core:if test="${model.currentPageIndex>1}">
        <li>
          <a href="${pageContext.request.contextPath}/courses/${model.course.id}?page=${model.currentPageIndex-1}"><fmt:message key="previous"/></a>
        </li>
      </core:if>
      <core:if test="${model.hasNextPage}">
        <li>
          <a href="${pageContext.request.contextPath}/courses/${model.course.id}?page=${model.currentPageIndex+1}"><fmt:message key="next"/></a>
        </li>
      </core:if>
    </fmt:bundle>
  </ul>


  </body>
  </html>
</fmt:bundle>
