<fmt:bundle basename="pagecontent" prefix="navigation.">
  <nav class="navbar navbar-default">
    <div class="container-fluid">
      <div class="navbar-header">
        <a class="navbar-brand">CoursIra</a>
      </div>
      <ul class="nav navbar-nav">

        <li class="active"><a href="${pageContext.request.contextPath}/"> <fmt:message key="home"/></a></li>
        <li class="active"><a href="${pageContext.request.contextPath}/courses"><fmt:message key="viewCourses"/></a></li>
        <core:if test="${(model.principal.user.role == 'STUDENT' )|| (model.principal.user.role == 'LECTURER')}">
          <li class="active"><a href="${pageContext.request.contextPath}/personal"><fmt:message key="personalPage"/></a>
          </li>
          <li class="active"><a href="${pageContext.request.contextPath}/courses?personal=true"><fmt:message key="viewMyCourses"/></a></li>
        </core:if>
        <core:if test="${(model.principal.user.role == 'LECTURER')}">
          <li class="active"><a href="${pageContext.request.contextPath}/courses/newCourse"><fmt:message key="createNewCourse"/></a></li>
        </core:if>
      </ul>
      <ul class="nav navbar-nav navbar-right">
        <ctg:principalTag principal="${model.principal}" currentLocale="${model.currentLocale}"/>
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
