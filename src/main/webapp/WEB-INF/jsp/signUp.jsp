<%@include file="parts/meta.jsp" %>


<jsp:useBean id="model" class="by.epam.coursira.model.SignUpModel" scope="request"/>

<fmt:setLocale value="${model.currentLocale}"/>
<fmt:bundle basename="pagecontent" prefix="sign_up_page.">
  <html>
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <%@include file="parts/navigation.jsp" %>

  <fmt:bundle basename="pagecontent" prefix="user.">
  <div class="container">
    <form name="UpdatePersonalForm" method="post" action="${pageContext.request.contextPath}/sign">
      <div class="form-group">
        <label><fmt:message key="email"/>&#42 :</label>
        <input type="text" name="email" value="${model.email}" minlength="1" maxlength="128" class="form-control"
               placeholder="Enter email">
      </div>

      <div class="form-group">
        <label> <fmt:message key="password_first"/>&#42 :</label>
        <input type="password" name="passwordFirst" required minlength="3" maxlength="16" class="form-control"
               placeholder="Enter password">
      </div>
      <div class="form-group">
        <label> <fmt:message key="password_second"/>&#42 :</label>
        <input type="password" name="passwordSecond" required minlength="3" maxlength="16" class="form-control"
               placeholder="Enter password">
      </div>
      <div class="form-group">
        <label><fmt:message key="first_name"/>&#42 :</label>
        <input type="text" name="firstName" required value="${model.firstName}" minlength="1" maxlength="128"/><br/>
      </div>
      <div class="form-group">
        <label><fmt:message key="last_name"/>&#42 :</label>
        <input type="text" name="lastName" required minlength="1" value="${model.lastName}" maxlength="128"/><br/>
      </div>
      <div class="form-group">
        <select class="form-control" name="role">
          <option>STUDENT</option>
          <option>LECTURER</option>
        </select>
      </div>

      <div class="container">
        <p class="text-danger">${model.errorDataMessage}</p>
      </div>
      </fmt:bundle>

      <input type="submit" class="btn btn-success" value="<fmt:message key="submit"/>"/>
    </form>

  </div>


  </body>
  </html>


  <%--  </body>--%>
  <%--  </html>--%>
</fmt:bundle>

