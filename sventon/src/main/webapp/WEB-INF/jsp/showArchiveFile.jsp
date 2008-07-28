<%
/*
 * ====================================================================
 * Copyright (c) 2005-2008 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
%>
<%@ include file="/WEB-INF/jspf/pageInclude.jspf" %>

<html>
<head>
  <%@ include file="/WEB-INF/jspf/pageHead.jspf" %>
  <title>Show Archive File - ${command.target}</title>
</head>

<body>
  <%@ include file="/WEB-INF/jspf/pageTop.jspf"%>
  <sventon:currentTargetHeader title="Show Archive File" target="${command.target}" properties="${properties}"/>

  <form name="searchForm" action="#" method="get" onsubmit="return doSearch(this, '${command.name}', '${command.path}');">
  <table class="sventonFunctionLinksTable">
    <tr>
      <td style="white-space: nowrap;">
        <sventon:fileFunctionButtons command="${command}" isArchivedEntry="${archivedEntry ne null}"/>
      </td>
      <td style="text-align: right;">
        <c:if test="${useCache}">
          <sventon:searchField command="${command}" isUpdating="${isUpdating}" isHead="${isHead}" searchMode="${userRepositoryContext.searchMode}"/>
        </c:if>
      </td>
    </tr>
  </table>
    <!-- Needed by ASVNTC -->
    <input type="hidden" name="revision" value="${command.revision}">
  </form>

  <table class="sventonEntriesTable">
    <c:set var="rowCount" value="0"/>
    <c:set var="totalOrigSize" value="0"/>
    <c:set var="totalCompSize" value="0"/>
    <tr>
      <th/>
      <th>Name</th>
      <th>Original size</th>
      <th>Compressed size</th>
      <th>Date</th>
      <th>CRC</th>
    </tr>
    <c:forEach items="${entries}" var="zipEntry">

      <c:url value="/repos/${command.name}/view${command.path}" var="showFileUrl">
        <c:param name="revision" value="${command.revision}" />
        <c:param name="archivedEntry" value="${zipEntry.name}" />
      </c:url>

      <jsp:useBean id="zipEntry" type="java.util.zip.ZipEntry" />
      <jsp:useBean id="entryDate" class="java.util.Date" />

      <tr class="${rowCount mod 2 == 0 ? 'sventonEntryEven' : 'sventonEntryOdd'}">
        <c:choose>
          <c:when test="${zipEntry.directory}">
            <td><img src="images/icon_folder.png" alt="dir"></td>
            <td>${zipEntry.name}</td>
          </c:when>
          <c:otherwise>
            <td><sventon-ui:fileTypeIcon filename="${entry.name}"/></td>
            <td><a href="${showFileUrl}">${zipEntry.name}</a></td>
          </c:otherwise>
        </c:choose>
        <td class="sventonColRightAlign">${zipEntry.size}</td>
        <td class="sventonColRightAlign">${zipEntry.compressedSize}</td>
        <td class="sventonColNoWrap"><fmt:formatDate type="both" value="<%=new java.util.Date(zipEntry.getTime())%>"
            dateStyle="short" timeStyle="short"/>
        </td>
        <td>${zipEntry.crc}</td>
      </tr>
      <c:set var="totalOrigSize" value="${totalOrigSize + zipEntry.size}"/>
      <c:set var="totalCompSize" value="${totalCompSize + zipEntry.compressedSize}"/>

      <c:set var="rowCount" value="${rowCount + 1}"/>
    </c:forEach>

    <tr class="${rowCount mod 2 == 0 ? 'sventonEntryEven' : 'sventonEntryOdd'}">
      <td align="right"><b>Total:</b></td>
      <td><b>${rowCount} entries</b></td>
      <td align="right" title="${totalOrigSize} bytes">
        <b><sventon-ui:formatBytes size="${totalOrigSize}" locale="${pageContext.request.locale}"/></b>
      </td>
      <td align="right" title="${totalCompSize} bytes">
        <b><sventon-ui:formatBytes size="${totalCompSize}" locale="${pageContext.request.locale}"/></b>
      </td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
  </table>

<%@ include file="/WEB-INF/jspf/pageFoot.jspf" %>
</body>
</html>
