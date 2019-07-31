<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" isErrorPage="true" %>
<%@include file="parts/meta.jsp" %>
<jsp:useBean id="model" class="by.epam.coursira.model.ErrorModel" scope="request"/>
<fmt:setLocale value="${model.currentLocale}"/>

<fmt:bundle basename="pagecontent" prefix="not_found_page.">
  <head>
    <title><fmt:message key="title"/></title>
    <%@include file="parts/header.jsp" %>
  </head>
  <body>
  <div class="alert alert-warning" role="alert">
    <fmt:message key="message"/>
  </div>
  <a href="${pageContext.request.contextPath}/" class="btn btn-success"><fmt:message key="home_page"/></a>
  </body>

  </html>
</fmt:bundle>
