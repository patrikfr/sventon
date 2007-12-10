<%
/*
 * ====================================================================
 * Copyright (c) 2005-2007 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
%>
<%@ include file="/WEB-INF/jspf/pageInclude.jspf"%>

<html>
  <head>
    <%@ include file="/WEB-INF/jspf/pageHeadWithoutRssLink.jspf"%>
    <title>sventon repository browser</title>
  </head>

  <body>
    <%@ include file="/WEB-INF/jspf/topHeaderTable.jspf"%>
    <h2>
      Authentication required for
      <c:choose>
        <c:when test="${paramValues['path'] eq null}">
          repository
        </c:when>
        <c:otherwise>
          directory: <i>${paramValues['path'][0]}</i>
        </c:otherwise>
      </c:choose>
    </h2>

    <form name="loginForm" action="${action}" method="post">
      <table>
        <tr>
          <td>Username</td>
          <td><input name="uid" type="text" nocache value="${paramValues['uid'][0]}"/></td>
        </tr>
        <tr>
          <td>Password</td>
          <td><input name="pwd" type="password" nocache/></td>
        </tr>
        <tr>
          <td colspan="2" align="right">
            <input type="submit" value="log in">
          </td>
        </tr>
        <c:if test="${paramValues['uid'] ne null}" >
        <tr>
          <td colspan="2">
            <span class="exclamationText">Authentication failed!</span>
          </td>
        </tr>
        </c:if>
      </table>

      <c:forEach items="${parameters}" var="paramEntry">
        <c:forEach items="${paramEntry.value}" var="parameter">
          <c:if test="${paramEntry.key ne 'pwd' && paramEntry.key ne 'uid'}">
            <input type="hidden" name="${paramEntry.key}" value="${parameter}"/>
          </c:if>
        </c:forEach>
      </c:forEach>

    </form>

    <script language="JavaScript" type="text/javascript">document.loginForm.uid.focus();</script>    

<%@ include file="/WEB-INF/jspf/pageFoot.jspf"%>
  </body>
</html>
