<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ctg" uri="/WEB-INF/tld/custom.tld" %>
<jsp:useBean id="model" class="by.epam.coursira.model.LoginModel" scope="request"/>
<fmt:setLocale value="${model.currentLocale}"/>

<head>
<fmt:bundle basename="pagecontent" prefix="login_page.">
  <title><fmt:message key="title"/></title>
  <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <fmt:bundle basename="pagecontent" prefix="navigation.">
    <nav class="navbar navbar-default">
      <div class="container-fluid">
        <div class="navbar-header">
          <a class="navbar-brand">CoursIra</a>
        </div>
        <ul class="nav navbar-nav">
          <li class="active"><a href="${pageContext.request.contextPath}/"><fmt:message key="home"/></a></li>
        </ul>
        <ul class="nav navbar-nav navbar-right">
          <li>
            <p><fmt:message key="chose_language"/>
            <form name="choseLanguage" method="post" action="${pageContext.request.contextPath}/language">
              <select name="language">
                <core:forEach items="${model.languages}" var="language">
                  <option value="${language}"
                    <core:if test="${language.locale == model.currentLocale}">
                      selected
                    </core:if>
                  >
                    <fmt:message key="${language}"/></option>
                </core:forEach>
              </select>
              <input type="submit" value=<fmt:message key="lang_submit"/>>
            </form>
            </p>

          </li>
        </ul>
      </div>
    </nav>
  </fmt:bundle>

  <fmt:bundle basename="pagecontent" prefix="user.">
    <div class="container">
      <form name="loginForm" method="post" action="${pageContext.request.contextPath}/login">
        <div class="form-group">
          <label><fmt:message key="email"/>&#42 :</label>
          <input type="text" name="login" minlength="1" maxlength="128" class="form-control"
                 placeholder="Enter email">
        </div>

        <div class="form-group">
          <label> <fmt:message key="password"/>&#42 :</label>
          <input type="password" name="password" required minlength="3" maxlength="16" class="form-control"
                 placeholder="Enter password">
        </div>
        <input type="submit" class="btn btn-default" value="<fmt:message key="login"/>"/>
      </form>
    </div>
    <div class="container">
      <p class="text-danger">${model.errorMessage}</p>
    </div>
  </fmt:bundle>

 <%@include file="parts/footer.jsp" %>
  </body>

  </html>
</fmt:bundle>
