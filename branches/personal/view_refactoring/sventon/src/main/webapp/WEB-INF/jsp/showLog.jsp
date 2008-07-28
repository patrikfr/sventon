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
<%@ include file="/WEB-INF/jspf/pageInclude.jspf"%>
<%@ page import="org.sventon.util.HTMLCreator" %>

<html>
<head>
  <%@ include file="/WEB-INF/jspf/pageHead.jspf"%>
  <title>Logs View - ${command.target}</title>
</head>

<body>
  <%@ include file="/WEB-INF/jspf/pageTop.jspf"%>
  <sventon:currentTargetHeader title="Log Messages" target="${command.target}" properties="${properties}"/>

  <form name="searchForm" action="#" method="get" onsubmit="return doSearch(this, '${command.name}', '${command.path}');">
  <table class="sventonFunctionLinksTable">
    <tr>
      <td style="white-space: nowrap;">
        <sventon:logsFunctionButtons command="${command}" isFile="${isFile}"/>
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

  <form action="${pageContext.request.contextPath}/repos/${command.name}/diff${command.path}${entry.name}" method="get" name="logForm" onsubmit="return doDiff(this);">

    <!-- Needed by ASVNTC -->
    <input type="hidden" name="revision" value="${command.revision}">

    <c:set var="command" value="${command}"/>
    <jsp:useBean id="command" type="org.sventon.web.command.SVNBaseCommand" />

    <table class="sventonLogEntriesTable">
      <c:set var="rowCount" value="0"/>
      <tr>
        <c:if test="${isFile}">
          <th style="width: 55px">&nbsp;</th>
        </c:if>
        <th>Revision</th>
        <th>Message</th>
        <th>&nbsp;</th>
        <th>Author</th>
        <th>Date</th>
      </tr>

      <c:set var="nextPath" value=""/>
      <c:set var="nextRev" value=""/>

      <% pageContext.setAttribute("br", "\n"); %>
      <c:forEach items="${logEntriesPage}" var="entry">
        <c:set var="nextPath" value="${entry.pathAtRevision}"/>
        <c:set var="nextRev" value="${entry.revision}"/>

        <jsp:useBean id="entry" type="org.sventon.model.LogEntryWrapper" />

        <tr class="${rowCount mod 2 == 0 ? 'sventonEntryEven' : 'sventonEntryOdd'}">
          <c:choose>
            <c:when test="${isFile}">
              <c:url value="/repos/${command.name}/view${entry.pathAtRevision}" var="showUrl">
                <c:param name="revision" value="${entry.revision}" />
              </c:url>
              <td><input type="checkbox" name="entry" value="${entry.pathAtRevision};;${entry.revision}" onClick="verifyCheckBox(this)"></td>
              <td><a href="${showUrl}">${entry.revision}</a></td>
            </c:when>
            <c:otherwise>
              <c:url value="/repos/${command.name}/revinfo" var="showRevInfoUrl">
                <c:param name="revision" value="${entry.revision}" />
              </c:url>
              <td><a href="${showRevInfoUrl}">${entry.revision}</a></td>
            </c:otherwise>
          </c:choose>
          <td><a href="#" onclick="Element.toggle('logInfoEntry${rowCount}'); toggleInnerHTML('hdr${rowCount}', '<spring:message code="less.link"/>', '<spring:message code="more.link"/>'); return false;">${fn:replace(fn:escapeXml(entry.message), br, '<br>')}</a></td>
          <td><a href="#" onclick="Element.toggle('logInfoEntry${rowCount}'); toggleInnerHTML('hdr${rowCount}', '<spring:message code="less.link"/>', '<spring:message code="more.link"/>'); return false;"><span id="hdr${rowCount}"><spring:message code="more.link"/></span></a></td>
          <td>${entry.author}</td>
          <td nowrap><fmt:formatDate type="both" value="${entry.date}" dateStyle="short" timeStyle="short"/></td>
        </tr>
        <tr id="logInfoEntry${rowCount}" style="display:none" class="sventonEntryLogInfo">
          <c:if test="${isFile}">
            <td>&nbsp;</td>
          </c:if>
          <td valign="top"><b>Changed<br>paths</b></td><td colspan="4">
            <%=HTMLCreator.createChangedPathsTable(entry.getChangedPaths(), entry.getRevision(),
                entry.getPathAtRevision(), "", command.getName(), false, false, response)%>
          </td>
        </tr>
        <c:set var="rowCount" value="${rowCount + 1}"/>
      </c:forEach>
      <c:url value="/repos/${command.name}/showlog${command.path}" var="showNextLogUrl">
        <c:param name="nextPath" value="${nextPath}" />
        <c:param name="nextRevision" value="${nextRev}" />
        <c:param name="revision" value="${command.revision}"/>
      </c:url>

      <c:choose>
        <c:when test="${morePages}">
          <tr>
            <td colspan="5" align="center">
              <a href="${showNextLogUrl}">Next ${pageSize}</a>&nbsp;
            </td>
          </tr>
        </c:when>
      </c:choose>
      <tr>
        <td colspan="2">
          <c:if test="${isFile}"><input type="submit" class="btn" value="diff"></c:if>
        </td>
        <td colspan="3">&nbsp;</td>
      </tr>
    </table>
  </form>

<%@ include file="/WEB-INF/jspf/pageFoot.jspf"%>
</body>
</html>
