<%@include file="parts/meta.jsp" %>
<%@ page import="java.time.format.TextStyle" %>
<jsp:useBean id="model" class="by.epam.coursira.model.PersonalModel" scope="request"/>

<fmt:setLocale value="${model.currentLocale}"/>
<fmt:bundle basename="pagecontent" prefix="personal_page.">
  <html>
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>


  <%--  personal data--%>
  <div class="row">
    <div class="col-md-2">
        <%--  photo--%>
      <fmt:bundle basename="pagecontent" prefix="photo.">
        <core:choose>
          <core:when test="${model.principal.user.base64Image == null}">
            <img src="${pageContext.request.contextPath}/static/avatar.png"
                 class="img-thumbnail" width="200" height="100"/>
          </core:when>
          <core:otherwise>
            <img src="data:image/jpg;base64,${model.principal.user.base64Image}" alt="<fmt:message key="ADD_PHOTO"/>"
                 class="img-thumbnail" width="200" height="100"/>
          </core:otherwise>
        </core:choose>
      </fmt:bundle>

    </div>

    <div class="col-md-4">
      <fmt:bundle basename="pagecontent" prefix="user.">
        <div class="well well-sm"><b><fmt:message key="email"/> </b>${model.principal.user.email}</div>
      </fmt:bundle>
      <fmt:bundle basename="pagecontent" prefix="person.">
        <div class="well well-sm"><b><fmt:message key="first_name"/> </b>${model.principal.user.firstName}</div>
        <div class="well well-sm"><b><fmt:message key="last_name"/> </b>${model.principal.user.lastName}</div>
      </fmt:bundle>
      <div class="well well-sm"><a href="${pageContext.request.contextPath}/personal/update" class="btn btn-success"><fmt:message key="update"/></a></div>

    </div>
    <div class="col-md-6">
      <fmt:bundle basename="pagecontent" prefix="person.">
      <div class="well well-sm"><b><fmt:message key="age"/> </b>${model.principal.user.age}</div>
      <div class="well well-sm"><b><fmt:message key="interests"/> </b>${model.principal.user.interests}</div>
      <div class="well well-sm"><b><fmt:message key="organization"/> </b>${model.principal.user.organization}</div>
      <div class="well well-sm"><b><fmt:message key="time_zone"/>
        </fmt:bundle>
      </b>${model.principal.session.zoneOffset.getDisplayName(TextStyle.FULL,model.principal.session.language.locale)}
      </div>
      <div class="well well-sm"><b><fmt:message key="courses_amount"/> </b>${model.courseAmount}</div>
    </div>
  </div>

</fmt:bundle>
<%--  Schedule here--%>

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
          <%--        <td><fmt:message key="start_time"/>, UTC--%>
          <%--          (${model.principal.session.zoneOffset.getDisplayName(TextStyle.FULL,model.principal.session.language.locale)})--%>
          <%--        </td>--%>
          <%--        <td><fmt:message key="end_time"/>, UTC--%>
          <%--          (${model.principal.session.zoneOffset.getDisplayName(TextStyle.FULL,model.principal.session.language.locale)})--%>
          <%--        </td>--%>
        <core:if test="${model.principal.user.role == 'STUDENT'}">
          <td><fmt:message key="lecturer"/></td>
        </core:if>

      </tr>
      <core:forEach items="${model.schedule}" var="lecture">
        <tr>
          <td>${lecture.description}</td>
          <td><ctg:localDateTag zoneOffset="${model.principal.session.zoneOffset}"
                                time="${lecture.startTime}"/></td>
          <td><ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}"
                                time="${lecture.startTime}"/></td>
          <td><ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}"
                                time="${lecture.endTime}"/></td>
            <%--          <td><ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}" time="${lecture.startTime}"/></td>--%>
            <%--          <td><ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}" time="${lecture.endTime}"/></td>--%>
          <core:if test="${model.principal.user.role == 'STUDENT'}">
            <td>${lecture.lecturer.firstName} ${lecture.lecturer.lastName}</td>
          </core:if>

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
        <a href="${pageContext.request.contextPath}/personal?page=${model.currentPageIndex-1}"><fmt:message key="previous"/></a>
      </li>
    </core:if>
    <core:if test="${model.hasNextPage}">
      <li>
        <a href="${pageContext.request.contextPath}/personal?page=${model.currentPageIndex+1}"><fmt:message key="next"/></a>
      </li>
    </core:if>
  </fmt:bundle>
</ul>

 <%@include file="parts/footer.jsp" %>
</body>
</html>

