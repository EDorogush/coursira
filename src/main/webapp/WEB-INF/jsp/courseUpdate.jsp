<%@include file="parts/meta.jsp" %>

<jsp:useBean id="model" class="by.epam.coursira.model.CourseUpdateModel" scope="request"/>
<fmt:setLocale value="${model.currentLocale}"/>
<fmt:bundle basename="pagecontent" prefix="course_update_page.">
  <html>
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>
    <%--course details--%>
  <div class="container">
    <div class="row">

      <div class="col-md-6">
        <form name="CreateCourseForm" accept-charset="UTF-8" method="post"
              action="${pageContext.request.contextPath}/courses/${model.course.id}/update">
          <input hidden name="updateCourseData" value="true">
          <fmt:bundle basename="pagecontent" prefix="course.">
            <div class="form-group">
              <label><fmt:message key="title"/>&#42 :</label>
              <input type="text" name="title" required value="${model.course.title}" minlength="1" maxlength="128"
                     class="form-control">
            </div>
            <div class="form-group">
              <label><fmt:message key="capacity"/></label>
              <select name="capacity">
                <core:forEach begin="2" end="50" varStatus="loop">
                  <option value="${loop.index}"
                    <core:if test="${model.course.capacity == loop.index}">
                      selected
                    </core:if>>${loop.index}</option>
                </core:forEach>
              </select>
            </div>
            <div class="form-group">
              <label><fmt:message key="description"/>&#42 :</label>
              <input type="text" name="description" value="${model.course.description}" minlength="1" maxlength="128"
                     class="form-control">
            </div>
          </fmt:bundle>
          <input type="submit" class="btn btn-success" value="<fmt:message key="submitCourseData"/>"/>
        </form>
      </div>
      <div class="col-md-6">

        <label><fmt:message key="other_lecturers"/></label>
        <core:forEach items="${model.course.lecturers}" var="lecturer">
          <div class="row">
            <div class="col-md-3"><p>${lecturer.firstName} ${lecturer.lastName}</p></div>
            <div class="col-md-3">
              <form name="DeleteLectureForm" method="post"
                    action="${pageContext.request.contextPath}/courses/${model.course.id}/update">
                <input hidden name="deleteLecturer" value="true">
                <input hidden name="lecturerId" value="${lecturer.id}">
                <input type="submit" class="btn btn-warning" value="<fmt:message key="DeleteLecturer"/>"/>
              </form>
            </div>
          </div>
        </core:forEach>
      </div>

      <div class="form-group">
        <label><fmt:message key="addLecturer"/></label>
        <form name="InviteLectureForm" method="post"
              action="${pageContext.request.contextPath}/courses/${model.course.id}/update">
          <input hidden name="inviteLecturer" value="true">
        <select name="lecturerId">
          <option value="0"></option>
          <core:forEach items="${model.lecturers}" var="lecturer">
            <option value="${lecturer.id}">${lecturer.firstName} ${lecturer.lastName}</option>
          </core:forEach>
        </select>
          <input type="submit" class="btn btn-success" value="<fmt:message key="submit"/>"/>
        </form>
      </div>
    </div>

    <div class="text-danger"><strong> ${model.errorCourseDataMessage}</strong></div>
  </div>

  <%@ page import="java.time.format.TextStyle" %>
  <div class="container">

    <table class="table table-bordered table-striped">
      <fmt:bundle basename="pagecontent" prefix="schedule.">
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
          <td></td>
        </tr>
      </fmt:bundle>

      <core:forEach items="${model.course.lectures}" var="lecture">
        <core:choose>
          <core:when test="${lecture.lecturer.id == model.principal.user.id}">
            <form name="UpdateLectureForm" method="post"
                  action="${pageContext.request.contextPath}/courses/${model.course.id}/update">
              <input hidden name="updateLecture" value="true">
              <input hidden name="lectureId" value="${lecture.id}">
              <tr>
                <td>
                  <div class="form-group">
                    <input type="text" name="lectureDescription" required value="${lecture.description}" minlength="1"
                           maxlength="128"
                           class="form-control">
                  </div>
                </td>
                <td>
                  <div class="form-group">
                    <input type="date" name="lectureDay" required
                           value=
                             <ctg:localDateTag zoneOffset="${model.principal.session.zoneOffset}"
                                               time="${lecture.startTime}"/>
                             class="form-control">
                  </div>
                </td>
                <td>
                  <div class="form-group">
                    <input type="time" name="lectureStartTime" required
                           value=
                             <ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}"
                                               time="${lecture.startTime}"/>
                             class="form-control">
                  </div>
                </td>
                <td>
                  <div class="form-group">
                    <input type="time" name="lectureEndTime" required
                           value=
                             <ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}"
                                               time="${lecture.endTime}"/>
                             class="form-control">
                  </div>
                </td>
                <td>${lecture.lecturer.firstName} ${lecture.lecturer.lastName}</td>

                <td><input type="submit" class="btn btn-success" value="<fmt:message key="submitLectureUpdate"/>"/>
                </td>
            </form>
            <td>
              <form name="DeleteLectureForm" method="post"
                    action="${pageContext.request.contextPath}/courses/${model.course.id}/update">
                <input hidden name="deleteLecture" value="true">
                <input hidden name="lectureId" value="${lecture.id}">
                <input type="submit" class="btn btn-warning" value="<fmt:message key="DeleteLecture"/>"/>
              </form>
            </td>
            </tr>


          </core:when>
          <core:otherwise>
            <%--              not my lecture--%>
            <tr>
              <td>${lecture.description}</td>
              <td><ctg:localDateTag zoneOffset="${model.principal.session.zoneOffset}"
                                    time="${lecture.startTime}"/></td>
              <td><ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}"
                                    time="${lecture.startTime}"/></td>
              <td><ctg:localTimeTag zoneOffset="${model.principal.session.zoneOffset}"
                                    time="${lecture.endTime}"/></td>
              <td>${lecture.lecturer.firstName} ${lecture.lecturer.lastName}</td>
              <td></td>
            </tr>
          </core:otherwise>
        </core:choose>
      </core:forEach>
        <%--              new lecture--%>
      <form name="NewLectureForm" method="post"
            action="${pageContext.request.contextPath}/courses/${model.course.id}/update">
        <input hidden name="newLecture" value="true">
        <tr>
          <td>
            <div class="form-group">
              <input type="text" name="lectureDescription" required minlength="1"
                     maxlength="128"
                     class="form-control">
            </div>
          </td>
          <td>
            <div class="form-group">
              <input type="date" name="lectureDay" required class="form-control">
            </div>
          </td>
          <td>
            <div class="form-group">
              <input type="time" name="lectureStartTime" required class="form-control">
            </div>
          </td>
          <td>
            <div class="form-group">
              <input type="time" name="lectureEndTime" required class="form-control">
            </div>
          </td>
          <td>${model.principal.user.firstName}<br>${model.principal.user.lastName}<br>
          </td>
          <td><input type="submit" class="btn btn-success" value="<fmt:message key="submitLectureNew"/>"/></td>
        </tr>
      </form>
    </table>

    <form name="ActivateForm" method="post"
          action="${pageContext.request.contextPath}/courses/${model.course.id}/update">
      <input hidden name="activateCourse" value="true">
      <input type="submit" class="btn btn-success" value="<fmt:message key="activateCourse"/>"/>
    </form>

  </div>


  </div>
  </body>
  </html>


</fmt:bundle>
