<%@ include file="/WEB-INF/jsp/include.jsp"%>
<%@ page import="org.tmatesoft.svn.core.io.*" %>

<html>
<head>
<title>Repository browser view</title>
<%@ include file="/WEB-INF/jsp/head.jsp"%>
</head>
<body>
<%@ include file="/WEB-INF/jsp/top.jsp"%>
<p>
  <span class="sventonLocation">
    <c:out value="${url}/${path}" /> Rev: <c:out value="${revision}" />
  </span>
</p>
<p>
  <input class="sventonGoTo" type="text" name="goto_url" value="" />[GoTo]
</p>

<table class="sventonEntriesTable">
	<tr>
    <th></th>
		<th>File</th>
		<th>Last changed rev</th>
		<th>File type</th>
		<th colspan="2">Options</th>
	</tr>
  <% int rowCount = 0; %>
  <c:forEach items="${svndir}" var="entry">
    <c:url value="repobrowser.svn" var="viewUrl">
      <c:param name="path" value="${path}${entry.name}" />
    </c:url>
    <c:url value="showlog.svn" var="showLogUrl">
      <c:param name="path" value="${path}${entry.name}" />
    </c:url>
    <c:url value="showfile.svn" var="showFileUrl">
      <c:param name="path" value="${path}${entry.name}" />
    </c:url>
    <tr class="<%if (rowCount % 2 == 0) out.print("sventonEntry1"); else out.print("sventonEntry2");%>">

      <%
    SVNDirEntry type = (SVNDirEntry) pageContext.getAttribute("entry");
    SVNNodeKind nodeKind = type.getKind();

    %>
      <td align="right"><input type="checkbox" name="entries" value="<c:out value="${entry.name}" />"/></td>
      <% if (nodeKind == SVNNodeKind.DIR) { %>
      <td><a href="<c:out value="${viewUrl}/"/>"><c:out
        value="${entry.name}" /></a></td>
      <% } else { %>
      <td><c:out value="${entry.name}" /></td>
      <% } %>
      <td><c:out value="${entry.revision}" /></td>
      <td><c:out value="${entry.kind}" /></td>
      <td><a href="<c:out value="${showLogUrl}"/>">[Show log]</a></td>
      <td><% if (nodeKind == SVNNodeKind.FILE) { %><a href="<c:out value="${showFileUrl}"/>">[View]</a> <% } %></td>
    </tr>
    <% rowCount++; %>
	</c:forEach>

  <tr>
    <td>[Toggle]</td>
  </tr>
</table>
<br>
</body>
</html>
