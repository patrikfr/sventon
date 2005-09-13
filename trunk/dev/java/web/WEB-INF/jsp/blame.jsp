<%@ include file="/WEB-INF/jsp/include.jsp"%>

<html>
<head>
<title>sventon blame - <c:out value="${url}" /></title>
<%@ include file="/WEB-INF/jsp/head.jsp"%>
<link href="jhighlight.css" rel="stylesheet" type="text/css"/>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/top.jsp"%>

  <c:url value="showlog.svn" var="showLogUrl">
    <c:param name="path" value="${command.path}${entry.name}" />
  </c:url>
  
  <c:url value="get.svn" var="downloadUrl">
    <c:param name="path" value="${command.path}${entry.name}" />
  </c:url>
  
  <c:url value="showfile.svn" var="showFileUrl">
    <c:param name="path" value="${command.path}${entry.name}" />
  </c:url>
  
  <p class="sventonHeader">
  Blame - <b><c:out value="${command.target}"/></b>&nbsp;<a href="javascript:toggleElementVisibility('propertiesDiv');">[properties on/off]</a>
  </p>

    <div id="propertiesDiv" style="display:none" class="sventonPropertiesDiv">
        <table class="sventonPropertiesTable">
          <c:forEach items="${properties}" var="property">
            <tr>
              <td><b><c:out value="${property.key}"/></b></td>
              <td><c:out value="${property.value}"/></td>
            </tr>
          </c:forEach>
        </table>
    </div>

  <br/>
  
  <table class="sventonFunctionLinksTable">
    <tr>
      <td><a href="<c:out value="${showFileUrl}&revision=${command.revision}"/>">[Show file]</a></td>
      <td><a href="<c:out value="${downloadUrl}&revision=${command.revision}"/>">[Download]</a></td>
      <td><a href="#">[Diff with previous]</a></td>
      <td><a href="<c:out value="${showLogUrl}&revision=${command.revision}"/>">[Show log]</a></td>
    </tr>
  </table>

<table class="sventonBlameTable">
  <tr>
    <th>Revision</th>
    <th>Author</th>
    <th>Line</th>
  </tr>
  <tr>
    <td valign="top">
      <pre>
<c:forEach items="${handler.blameLines}" var="line"><a href="#"><c:out value="${line.revision}"/></a>
</c:forEach></pre>
    </td>
    <td valign="top">
      <pre>
<c:forEach items="${handler.blameLines}" var="line"><c:out value="${line.author}"/>
</c:forEach></pre>
    </td>
    <td valign="top">
      <pre>
<c:out value="${handler.blameContent}" escapeXml="false"/></pre>
    </td>
  </tr>
</table>
<br>
<%@ include file="/WEB-INF/jsp/foot.jsp"%>
</body>
</html>
