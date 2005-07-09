<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
  <head>
    <title>Show file</title>
    <%@ include file="/WEB-INF/jsp/head.jsp"%>
    <link href="syntax.css" rel="stylesheet" type="text/css"/>
  </head>

  <body>
    <%@ include file="/WEB-INF/jsp/top.jsp"%>

    <c:url value="blame.svn" var="blameUrl">
      <c:param name="path" value="${command.path}${entry.name}" />
    </c:url>

    <table cellspacing="3">
      <tr>
        <td><a href="#">[Download]</a></td>
        <td><a href="#">[Diff with previous]</a></td>
        <td><a href="<c:out value="${blameUrl}&revision=${command.revision}"/>">[Blame]</a></td>
      </tr>
    </table>

    <c:choose>
      <c:when test="${empty fileContents}">
This is a binary file.
      </c:when>
     	<c:otherwise>
<c:out value="${fileContents}" escapeXml="false"/>
      </c:otherwise>
    </c:choose>
  </body>
</html>
