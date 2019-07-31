<%@include file="parts/meta.jsp" %>


<jsp:useBean id="model" class="by.epam.coursira.model.UserUpdateModel" scope="request"/>

<fmt:setLocale value="${model.currentLocale}"/>
<fmt:bundle basename="pagecontent" prefix="personal_update_page.">
  <html>
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>


  <div class="row">
      <%--  photo--%>
    <fmt:bundle basename="pagecontent" prefix="photo.">
    <div class="col-md-2">
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
    <div class="col-md-2">
        <%--      /upload--%>
      <form action="${pageContext.request.contextPath}/personal/update" method="post" enctype="multipart/form-data">
        <div class="form-group">
          <input type="file" required name="file"/>
        </div>
        <input type="submit" class="btn btn-success" value="<fmt:message key="submit_photo"/>"/>
      </form>
      <div class="text-danger"><strong>${model.errorImageMessage}</strong></div>

    </div>
    </fmt:bundle>
      <%--  personal data--%>
    <fmt:bundle basename="pagecontent" prefix="person.">
    <div class="col-md-8">

      <form name="UpdatePersonalForm" method="post" action="${pageContext.request.contextPath}/personal/update">
        <div class="form-group">
          <label><fmt:message key="first_name"/>&#42 :</label>
          <input type="text" name="firstName" required minlength="1" maxlength="128"
                 value="${model.principal.user.firstName}"/><br/>
        </div>
        <div class="form-group">
          <label><fmt:message key="last_name"/>&#42 :</label>
          <input type="text" name="lastName" required minlength="1" maxlength="128"
                 value="${model.principal.user.lastName}"/><br/>
        </div>
        <div class="form-group">
          <label><fmt:message key="age"/></label>
          <select name="age">
            <core:forEach begin="18" end="100" varStatus="loop">
              <option value="${loop.index}"
                <core:if test="${model.principal.user.age == loop.index}">
                  selected
                </core:if>>${loop.index}</option>
            </core:forEach>
          </select>
        </div>

        <div class="form-group">
          <label><fmt:message key="interests"/></label>
          <input type="text" name="interest" maxlength="128"
                 value="${model.principal.user.interests}"/><br/>
        </div>
        <div class="form-group">
          <label><fmt:message key="organization"/></label>
          <input type="text" name="organization" maxlength="128"
                 value="${model.principal.user.organization}"/><br/>
        </div>
        </fmt:bundle>
        <input type="submit" class="btn btn-success" value="<fmt:message key="submit"/>"/>
      </form>
      <div class="text-danger"><strong> ${model.errorDataMessage}</strong></div>
    </div>


  </body>
  </html>

</fmt:bundle>

