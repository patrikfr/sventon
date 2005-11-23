<%
/*
 * ====================================================================
 * Copyright (c) 2005 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<%@ page import="de.berlios.sventon.util.ByteFormatter, java.util.Locale"%>

<html>
<head>
<title>sventon repository browser - <c:out value="${url}" /></title>
<%@ include file="/WEB-INF/jsp/head.jsp"%>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/top.jsp"%>

  <p>
    <table class="sventonHeader"><tr><td>

  <c:choose>
    <c:when test="${isSearch}">
      Search result for - <b><c:out value="${searchString}"/></b> (directory '<c:out value="${startDir}"/>' and below)
    </c:when>
    <c:otherwise>
      <c:choose>
        <c:when test="${isFlatten}">
          Flattened structure - <b><c:out value="${command.target}"/></b>
        </c:when>
        <c:otherwise>
          Repository Browser - <b><c:out value="${command.target}"/></b>
        </c:otherwise>
      </c:choose>
    </c:otherwise>
  </c:choose>

  <c:if test="${!empty properties}">
    <a href="javascript:toggleElementVisibility('propertiesDiv'); changeHideShowDisplay('propertiesLink');">[<span id="propertiesLink">show</span> properties]</a>
  </c:if>
    </td></tr></table>
    <%@ include file="/WEB-INF/jsp/sventonheader.jsp"%>
  </p>

<br/>

<div id="entriesDiv" class="sventonEntriesDiv">
<form method="get" action="#" name="entriesForm" onsubmit="return doAction(entriesForm);">
  <!-- Needed by ASVNTC -->
  <input type="hidden" name="path" value="${command.path}"/>
  <input type="hidden" name="revision" value="${command.revision}"/>

  <table class="sventonEntriesTable">
    <tr>
      <th></th>
      <th></th>
      <th>File</th>
      <th>Size (bytes)</th>
      <th>Revision</th>
      <th>Author</th>
      <th>Date</th>
      <th colspan="2">Options</th>
    </tr>
    <%
      int rowCount = 0;
      long totalSize = 0;
    %>

    <c:forEach items="${svndir}" var="entry">
    <jsp:useBean id="entry" type="de.berlios.sventon.ctrl.RepositoryEntry" />
      <c:url value="repobrowser.svn" var="viewUrl">
        <c:param name="path" value="${entry.fullEntryNameStripMountPoint}" />
      </c:url>
      <c:url value="showlog.svn" var="showLogUrl">
        <c:param name="path" value="${entry.fullEntryNameStripMountPoint}" />
      </c:url>
      <c:url value="showfile.svn" var="showFileUrl">
        <c:param name="path" value="${entry.fullEntryNameStripMountPoint}" />
      </c:url>

      <tr class="<%if (rowCount % 2 == 0) out.print("sventonEntry1"); else out.print("sventonEntry2");%>">
      <%
        totalSize += entry.getSize();
      %>
        <td class="sventonCol1"><input type="checkbox" name="entry" value="<c:out value="${entry.fullEntryName}" />"/></td>
        <% if ("dir".equals(entry.getKind())) { %>
        <td class="sventonCol2"><img src="images/icon_dir.gif" alt="dir" /></td>
        <td class="sventonCol3">
          <c:choose>
            <c:when test="${isSearch || isFlatten}">
              <a href="<c:out value="${viewUrl}/&revision=${command.revision}"/>" onmouseover="this.T_WIDTH=1;return escape('<c:out value="${entry.fullEntryName}" />')">
                <c:out value="${entry.friendlyFullEntryName}" />
            </c:when>
            <c:otherwise>
              <a href="<c:out value="${viewUrl}/&revision=${command.revision}"/>"><c:out value="${entry.name}" /></c:otherwise>
          </c:choose>
          </a></td>
        <% } else { %>
        <td class="sventonCol2"><img src="images/icon_file.gif" alt="file" /></td>
        <td class="sventonCol3">
          <c:choose>
            <c:when test="${isSearch || isFlatten}">
              <a href="<c:out value="${showFileUrl}&revision=${command.revision}"/>" onmouseover="this.T_WIDTH=1;return escape('<c:out value="${entry.fullEntryName}" />')">
                <c:out value="${entry.friendlyFullEntryName}"/>
            </c:when>
            <c:otherwise>
              <a href="<c:out value="${showFileUrl}&revision=${command.revision}"/>"><c:out value="${entry.name}"/></c:otherwise>
          </c:choose>
          </a></td>
        <% } %>
        <td class="sventonCol4"><% if ("file".equals(entry.getKind())) { %><c:out value="${entry.size}" /><% } %></td>
        <td class="sventonCol5"><c:out value="${entry.revision}" /></td>
        <td class="sventonCol6"><c:out value="${entry.author}" /></td>
        <td class="sventonCol7"><fmt:formatDate type="both" value="${entry.date}" dateStyle="short" timeStyle="short"/></td>
        <td class="sventonCol8"><a href="<c:out value="${showLogUrl}&revision=${command.revision}"/>">[Show log]</a></td>
      </tr>
      <% rowCount++; %>
    </c:forEach>

    <tr class="<%if (rowCount % 2 == 0) out.print("sventonEntry1"); else out.print("sventonEntry2");%>">
      <td colspan="2" align="right"><b>Total:</b></td>
      <td><b><%=rowCount%> entries</b></td>
      <td align="right" title="<%=totalSize%>&nbsp;bytes"><b><%if (totalSize != 0) out.print(ByteFormatter.format(totalSize, request.getLocale()));%></b></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>

    <tr>
      <td><input type="button" name="toggleButton" value="toggle" onClick="javascript:toggleEntryFields(this.form)"/></td>
      <td colspan="2">
        <select class="sventonSelect" name="actionSelect">
          <option class="sventonSelectOption">Actions...</option>
          <!--<option value="zip">&nbsp;&nbsp;Download as zip</option>-->
          <option value="thumb">&nbsp;&nbsp;Show as thumbnails</option>
        </select>
      </td>
      <td colspan="6"><input type="submit" value="go!"/></td>
    </tr>
  </table>
</form>
</div>
<br>
<script language="JavaScript" type="text/javascript" src="wz_tooltip.js"></script>
<%@ include file="/WEB-INF/jsp/foot.jsp"%>
</body>
</html>
