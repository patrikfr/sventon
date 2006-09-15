<%
/*
 * ====================================================================
 * Copyright (c) 2005-2006 Sventon Project. All rights reserved.
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
<%@ page import="de.berlios.sventon.util.ByteFormatter"%>
<%@ page import="de.berlios.sventon.repository.RepositoryEntry"%>

<html>
  <head>
    <%@ include file="/WEB-INF/jspf/pageHead.jspf"%>
    <title>sventon repository browser - ${url}</title>
  </head>
<body>
  <%@ include file="/WEB-INF/jspf/pageTop.jspf"%>

  <p>
    <c:choose>
      <c:when test="${isFlatten}">
        <ui:currentTargetHeader title="Flattened structure" target="${command.target} (and below)" hasProperties="false"/>
      </c:when>
      <c:otherwise>
        <ui:currentTargetHeader title="Repository Browser" target="${command.target}" hasProperties="true"/>
      </c:otherwise>
    </c:choose>
  </p>

  <br/>
  <ui:functionLinks pageName="repobrowse"/> 

  <div id="entriesDiv" class="sventonEntriesDiv">
    <form method="post" action="#" name="entriesForm" onsubmit="return doAction(entriesForm);">
      <!-- Needed by ASVNTC -->
      <input type="hidden" name="path" value="${command.path}"/>
      <input type="hidden" name="revision" value="${command.revision}"/>
      <input type="hidden" name="name" value="${command.name}"/>

      <table class="sventonEntriesTable">
        <tr>
          <th></th>
          <th></th>

          <c:choose>
            <c:when test="${isFlatten}">
              <c:url value="flatten.svn" var="sortUrl">
                <c:param name="path" value="${command.path}" />
                <c:param name="revision" value="${command.revision}" />
                <c:param name="name" value="${command.name}" />
              </c:url>
            </c:when>
            <c:otherwise>
              <c:url value="repobrowser.svn" var="sortUrl">
                <c:param name="path" value="${command.path}" />
                <c:param name="revision" value="${command.revision}" />
                <c:param name="name" value="${command.name}" />
              </c:url>
            </c:otherwise>
          </c:choose>

          <jsp:useBean id="userContext" scope="session" class="de.berlios.sventon.web.model.UserContext" />
          <c:set var="sortModeArrow" value="${userContext.sortMode eq 'ASC' ? 'icon_up.gif;' : 'icon_down.gif'}"/>
          <c:set var="opositeSortType" value="${userContext.sortMode eq 'ASC' ? 'DESC' : 'ASC'}"/>

          <th><a href="${sortUrl}&sortType=NAME&sortMode=${opositeSortType}" style="color: #000000;">
              <c:if test="${userContext.sortType eq 'NAME'}"><img border="0" alt="${userContext.sortType}" src="images/${sortModeArrow}"/></c:if>File</a>
          </th>
          <th></th>
          <th><a href="${sortUrl}&sortType=SIZE&sortMode=${opositeSortType}" style="color: #000000;">
            <c:if test="${userContext.sortType eq 'SIZE'}"><img border="0" alt="${userContext.sortType}" src="images/${sortModeArrow}"/></c:if>Size (bytes)</a>
          </th>
          <th><a href="${sortUrl}&sortType=REVISION&sortMode=${opositeSortType}" style="color: #000000;">
            <c:if test="${userContext.sortType eq 'REVISION'}"><img border="0" alt="${userContext.sortType}" src="images/${sortModeArrow}"/></c:if>Revision</a>
          </th>
          <th><a href="${sortUrl}&sortType=AUTHOR&sortMode=${opositeSortType}" style="color: #000000;">
            <c:if test="${userContext.sortType eq 'AUTHOR'}"><img border="0" alt="${userContext.sortType}" src="images/${sortModeArrow}"/></c:if>Author</a>
          </th>
          <th><a href="${sortUrl}&sortType=DATE&sortMode=${opositeSortType}" style="color: #000000;">
            <c:if test="${userContext.sortType eq 'DATE'}"><img border="0" alt="${userContext.sortType}" src="images/${sortModeArrow}"/></c:if>Date</a>
          </th>
        </tr>
    <%
          int rowCount = 0;
          long totalSize = 0;
    %>

        <c:if test="${!empty command.pathNoLeaf && !isFlatten}">
          <c:url value="repobrowser.svn" var="backUrl">
            <c:param name="path" value="${command.pathNoLeaf}" />
            <c:param name="revision" value="${command.revision}" />
            <c:param name="name" value="${command.name}" />
          </c:url>

          <tr class="<%if (rowCount % 2 == 0) out.print("sventonEntryEven"); else out.print("sventonEntryOdd");%>">
            <td class="sventonCol1"></td>
            <td class="sventonCol2"><img src="images/icon_dir.gif" alt="dir" /></td>
            <td class="sventonCol3">
              <a href="${backUrl}">..&nbsp;&nbsp;&nbsp;</a>
            </td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
          </tr>
      <% rowCount++; %>
        </c:if>

        <c:forEach items="${svndir}" var="entry">
          <jsp:useBean id="entry" type="RepositoryEntry" />
          <c:url value="repobrowser.svn" var="viewUrl">
            <c:param name="path" value="${entry.fullEntryName}" />
            <c:param name="revision" value="${command.revision}" />
            <c:param name="name" value="${command.name}" />
          </c:url>
          <c:url value="showfile.svn" var="showFileUrl">
            <c:param name="path" value="${entry.fullEntryName}" />
            <c:param name="revision" value="${command.revision}" />
            <c:param name="name" value="${command.name}" />
          </c:url>
          <c:url value="revinfo.svn" var="showRevInfoUrl">
            <c:param name="revision" value="${entry.revision}" />
            <c:param name="name" value="${command.name}" />
          </c:url>

          <tr class="<%if (rowCount % 2 == 0) out.print("sventonEntryEven"); else out.print("sventonEntryOdd");%>">
      <%
            totalSize += entry.getSize();
      %>
            <td class="sventonCol1"><input type="checkbox" name="entry" value="${entry.fullEntryName}"/></td>
        <% if (RepositoryEntry.Kind.dir == entry.getKind()) { %>
            <td class="sventonCol2"><img src="images/icon_dir.gif" alt="dir" /></td>
            <td class="sventonCol3">
              <c:choose>
                <c:when test="${isFlatten}">
                  <a href="${viewUrl}" onmouseover="this.T_WIDTH=1;return escape('<table><tr><td style=\'white-space: nowrap\'>${entry.fullEntryName}</td></tr></table>')">${entry.friendlyFullEntryName}</a>
                </c:when>
                <c:otherwise>
                  <a href="${viewUrl}">${entry.name}</a>
                </c:otherwise>
              </c:choose>
            </td>
        <% } else { %>
            <td class="sventonCol2"><img src="images/icon_file.gif" alt="file" /></td>
            <td class="sventonCol3">
              <c:choose>
                <c:when test="${isFlatten}">
                  <a href="${showFileUrl}" onmouseover="this.T_WIDTH=1;return escape('<table><tr><td style=\'white-space: nowrap\'>${entry.fullEntryName}</td></tr></table>')">
                ${entry.friendlyFullEntryName}
                  </a>
                </c:when>
                <c:otherwise>
                  <a href="${showFileUrl}">${entry.name}</a>
                </c:otherwise>
              </c:choose>
              </td>
        <% } %>
            <td class="sventonCol4" align="center">
              <c:if test="${!empty entry.lock}">
                <span onmouseover="this.T_WIDTH=1;return escape('<table><tr><td><b>Owner</b></td><td>${entry.lock.owner}</td></tr><tr><td><b>Comment</b></td><td style=\'white-space: nowrap\'>${entry.lock.comment}</td></tr><tr><td><b>Created</b></td><td style=\'white-space: nowrap\'><fmt:formatDate type="both" value="${entry.lock.creationDate}" dateStyle="short" timeStyle="short"/></td></tr><tr><td><b>Expires</b></td><td style=\'white-space: nowrap\'><fmt:formatDate type="both" value="${entry.lock.expirationDate}" dateStyle="short" timeStyle="short"/></td></tr></table>')"><img border="0" src="images/lock.gif"></span>
              </c:if>
            </td>
            <td class="sventonCol5"><% if (RepositoryEntry.Kind.file == entry.getKind()) { %>${entry.size}<% } %></td>
            <td class="sventonCol6"><a href="${showRevInfoUrl}" onmouseover="this.T_WIDTH=1;return escape('<spring:message code="showrevinfo.link.tooltip"/>')">${entry.revision}</a></td>
            <td class="sventonCol7">${entry.author}</td>
            <td class="sventonCol8"><fmt:formatDate type="both" value="${entry.date}" dateStyle="short" timeStyle="short"/></td>
          </tr>
      <% rowCount++; %>
        </c:forEach>

        <tr class="<%if (rowCount % 2 == 0) out.print("sventonEntryEven"); else out.print("sventonEntryOdd");%>">
          <td colspan="2" align="right"><b>Total:</b></td>
          <td><b><%=rowCount%> entries</b></td>
          <td></td>
          <td align="right" title="<%=totalSize%>&nbsp;bytes"><b><%if (totalSize != 0) out.print(ByteFormatter.format(totalSize, request.getLocale()));%></b></td>
          <td></td>
          <td></td>
          <td></td>
        </tr>

        <tr>
          <td colspan="2"><input type="button" class="btn" name="toggleButton" value="toggle" onClick="javascript:toggleEntryFields(this.form)"/></td>
          <td>
            <%@ include file="/WEB-INF/jspf/actionSelectList.jspf"%><input type="submit" class="btn" value="go!"/>
          </td>
          <td colspan="5"></td>
        </tr>
      </table>
    </form>
  </div>


<br>
<script language="JavaScript" type="text/javascript" src="wz_tooltip.js"></script>
<%@ include file="/WEB-INF/jspf/rssLink.jspf"%>
<%@ include file="/WEB-INF/jspf/pageFoot.jspf"%>
</body>
</html>
