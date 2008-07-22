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
<%@ tag body-content="empty" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sventon-ui" uri="/WEB-INF/sventon.tld" %>
<%@ attribute name="pageName" required="true" type="java.lang.String" %>

<!-- Prepare link URLs -->

<c:url var="downloadLinkUrl" value="/repos/${command.name}/get${command.path}${entry.name}">
  <c:param name="name" value="${command.name}" />
</c:url>
<c:url var="showLogLinkUrl" value="/repos/${command.name}/showlog${command.path}${entry.name}">
  <c:param name="revision" value="${command.revision}" />
</c:url>
<c:url var="showLockLinkUrl" value="/repos/${command.name}/showlocks${command.path}${entry.name}">
  <c:param name="revision" value="${command.revision}" />
</c:url>
<c:url var="showDirLinkUrl" value="/repos/${command.name}/browse${command.path}">
  <c:param name="revision" value="${command.revision}" />
</c:url>
<c:url var="showFileUrl" value="/repos/${command.name}/view${command.path}${entry.name}">
  <c:param name="revision" value="${command.revision}" />
</c:url>
<c:url var="blameLinkUrl" value="/repos/${command.name}/blame${command.path}${entry.name}">
  <c:param name="revision" value="${command.revision}" />
</c:url>
<c:url var="showArchivedFileLinkUrl" value="/repos/${command.name}/view${command.path}${entry.name}">
    <c:param name="revision" value="${command.revision}" />
    <c:param name="archivedEntry" value="${archivedEntry}" />
  <c:param name="forceDisplay" value="true" />
</c:url>

<form name="searchForm" action="#" method="get" onsubmit="return doSearch(this, '${command.name}', '${command.path}');">
<table class="sventonFunctionLinksTable">
  <tr><td style="white-space: nowrap;">

<c:choose>
  <c:when test="${pageName eq 'showTextFile'}">
    <c:choose>
      <c:when test="${archivedEntry ne null}">
        <input type="button" class="btn" value="<spring:message code="showarchivefile.button.text"/>" onclick="document.location.href='${showFileUrl}';">
      </c:when>
      <c:otherwise>
        <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target}"/>')" onclick="document.location.href='${showLogLinkUrl}';">
        <input type="button" class="btn" value="<spring:message code="download.button.text"/>" onclick="document.location.href='${downloadLinkUrl}';">
        <input type="button" class="btn" value="<spring:message code="blame.button.text"/>" onclick="document.location.href='${blameLinkUrl}';">
        <input type="button" class="btn" value="<spring:message code="showrawfile.button.text"/>" onmouseover="Tip('<spring:message code="showrawfile.button.tooltip"/>')" onclick="document.location.href='${showFileUrl}&format=raw';">

          <c:url value="/repos/${command.name}/diffprev${command.path}${entry.name}" var="diffPreviousUrl">
            <c:param name="revision" value="${command.revision}" />
          </c:url>
        <input type="button" class="btn" value="<spring:message code="diffprev.button.text"/>" onmouseover="Tip('<spring:message code="diffprev.button.tooltip" arguments="${command.path},${command.revision}"/>')" onclick="document.location.href='${diffPreviousUrl}';">
      </c:otherwise>
    </c:choose>
    <%@ include file="/WEB-INF/jspf/charsetSelectList.jspf"%>
  </c:when>

  <c:when test="${pageName eq 'showImageFile' || pageName eq 'showBinaryFile' || pageName eq 'showArchiveFile'}">
    <c:choose>
      <c:when test="${archivedEntry eq null}">
        <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target}"/>')" onclick="document.location.href='${showLogLinkUrl}';">
        <input type="button" class="btn" value="<spring:message code="download.button.text"/>" onclick="document.location.href='${downloadLinkUrl}';">
      </c:when>
      <c:otherwise>
        <input type="button" class="btn" value="<spring:message code="showarchivefile.button.text"/>" onclick="document.location.href='${showFileUrl}';">
        <input type="button" class="btn" value="<spring:message code="force-display.button.text"/>" onclick="document.location.href='${showArchivedFileLinkUrl}';">
      </c:otherwise>
    </c:choose>
  </c:when>

  <c:when test="${pageName eq 'repobrowser'}">
    <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target eq '' ? '/' : command.target}"/>')" onclick="document.location.href='${showLogLinkUrl}';">
    <input type="button" class="btn" value="<spring:message code="showlocks.button.text"/>" onclick="document.location.href='${showLockLinkUrl}';">
    <c:choose>
      <c:when test="${useCache}">
        <c:choose>
          <c:when test="${isUpdating}">
            <input type="button" class="btn" value="<spring:message code="flatten.button.text"/>" disabled="disabled">
            <img class="helpIcon" src="images/icon_help.png" alt="Help" onmouseover="Tip('<spring:message code="flatten.button.isupdating.tooltip"/>')">
          </c:when>
          <c:when test="${!isHead}">
            <input type="button" class="btn" value="<spring:message code="flatten.button.text"/>" disabled="disabled">
            <img class="helpIcon" src="images/icon_help.png" alt="Help" onmouseover="Tip('<spring:message code="flatten.button.disabled.tooltip"/>')">
          </c:when>
          <c:when test="${isFlatten}">
            <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="document.location.href='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')">
          </c:when>
          <c:otherwise>
            <input type="button" class="btn" value="<spring:message code="flatten.button.text"/>" onclick="return doFlatten('${command.path}', '${command.name}');" onmouseover="Tip('<spring:message code="flatten.button.tooltip"/>')">
          </c:otherwise>
        </c:choose>
      </c:when>
    </c:choose>
    <c:if test="${!isFlatten && !isEntrySearch && !isLogSearch}">
      </td>
      <td style="white-space: nowrap; text-align: right;"><spring:message code="filter.text"/></td>
      <td style="white-space: nowrap;">
        <select name="filterExtension" class="sventonSelect" onchange="document.location.href='${showDirLinkUrl}&filterExtension=' + this.form.filterExtension.options[this.form.filterExtension.selectedIndex].value;">
          <option value="all">&lt;show all&gt;</option>
          <c:forEach items="${existingExtensions}" var="extension">
            <option value="${extension}" ${extension eq filterExtension ? "selected" : ""}>${extension}</option>
          </c:forEach>
        </select>
    </c:if>
  </c:when>

  <c:when test="${pageName eq 'showLog'}">
    <c:choose>
      <c:when test="${isFile}">
        <input type="button" class="btn" value="<spring:message code="showfile.button.text"/>" onclick="document.location.href='${showFileUrl}';">
        <input type="button" class="btn" value="<spring:message code="download.button.text"/>" onclick="document.location.href='${downloadLinkUrl}';">
      </c:when>
      <c:otherwise>
        <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="document.location.href='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')">
      </c:otherwise>
    </c:choose>
  </c:when>

  <c:when test="${pageName eq 'showRevInfo'}">
    <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="document.location.href='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')">
  </c:when>

  <c:when test="${pageName eq 'showPathDiff'}">
    <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="document.location.href='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')">
  </c:when>

  <c:when test="${pageName eq 'showDiff' || pageName eq 'showUnifiedDiff' || pageName eq 'showInlineDiff'}">
    <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target}"/>')" onclick="document.location.href='${showLogLinkUrl}';">
    <input type="button" class="btn" value="<spring:message code="showfile.button.text"/>" onclick="document.location.href='${showFileUrl}';">

    <c:url value="/repos/${command.name}/diff${command.path}${entry.name}" var="diffUrl">
      <c:param name="revision" value="${command.revision}" />
      <c:param name="entry" value="${diffCommand.toPath};;${diffCommand.toRevision}" />
      <c:param name="entry" value="${diffCommand.fromPath};;${diffCommand.fromRevision}" />
      <c:if test="${!empty pegrev}">
        <c:param name="pegrev" value="${pegrev}" />
      </c:if>
      <c:if test="${param.showlatestrevinfo}">
        <c:param name="showlatestrevinfo" value="true" />
      </c:if>
    </c:url>
    <input type="button" class="btn" value="<spring:message code="wrap-nowrap.button.text"/>" onclick="toggleWrap();">

    <c:url value="/repos/${command.name}/diffprev${diffCommand.fromPath}" var="diffPreviousUrl">
      <c:param name="revision" value="${diffCommand.fromRevision}" />
    </c:url>
    <input type="button" class="btn" value="<spring:message code="diffprev.button.text"/>" onmouseover="Tip('<spring:message code="diffprev.button.tooltip" arguments="${diffCommand.fromPath},${diffCommand.fromRevision}"/>')" onclick="document.location.href='${diffPreviousUrl}';">

    <select name="diffStyle" class="sventonSelect" onchange="document.location.href=this.options[this.selectedIndex].value;">
      <option value="${diffUrl}&style=inline"
          ${pageName eq 'showInlineDiff' ? 'selected' : ''}>Inline</option>
      <option value="${diffUrl}&style=sidebyside"
          ${pageName eq 'showDiff' ? 'selected' : ''}>Side By Side</option>
      <option value="${diffUrl}&style=unified"
          ${pageName eq 'showUnifiedDiff' ? 'selected' : ''}>Unified</option>
    </select>
    <%@ include file="/WEB-INF/jspf/charsetSelectList.jspf"%>
  </c:when>

  <c:when test="${pageName eq 'showBlame'}">
    <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target}"/>')" onclick="document.location.href='${showLogLinkUrl}';">
    <input type="button" class="btn" value="<spring:message code="showfile.button.text"/>" onclick="document.location.href='${showFileUrl}';">
    <input type="button" class="btn" value="<spring:message code="download.button.text"/>" onclick="document.location.href='${downloadLinkUrl}';">
    <%@ include file="/WEB-INF/jspf/charsetSelectList.jspf"%>
  </c:when>
        
  <c:when test="${pageName eq 'showThumbs'}">
    <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="document.location.href='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')">
  </c:when>

  <c:when test="${pageName eq 'showLock'}">
    <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="document.location.href='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')">
  </c:when>

  <c:otherwise>
    <spring:message code="functionlinks.error.message" arguments="${pagename}"/>
  </c:otherwise>
</c:choose>

    </td>
    <c:if test="${useCache}">
      <td style="text-align: right;">
        <span style="white-space: nowrap;">
          <spring:message code="search.text"/>
          <input type="radio" id="entrySearch" name="searchMode" class="rdo" value="entries" ${userRepositoryContext.searchMode eq 'entries' ? 'checked' : ''}>
          <label for="entrySearch">entries</label>
          <input type="radio" id="logSearch" name="searchMode" class="rdo" value="logMessages" ${userRepositoryContext.searchMode eq 'logMessages' ? 'checked' : ''}>
          <label for="logSearch">logs</label>
        </span>
        <input type="hidden" name="startDir" value="${command.pathPart}">

        <c:choose>
          <c:when test="${isUpdating}">
            <img class="helpIcon" src="images/icon_help.png" alt="Help" onmouseover="Tip('<spring:message code="search.button.isupdating.tooltip"/>')">
            <input type="text" name="searchString" class="sventonSearchField" value="" disabled="disabled">
            <input type="submit" value="go!" disabled="disabled" class="btn">
          </c:when>
          <c:when test="${!isHead}">
            <img class="helpIcon" src="images/icon_help.png" alt="Help" onmouseover="Tip('<spring:message code="search.button.disabled.tooltip"/>')">
            <input type="text" name="searchString" class="sventonSearchField" value="" disabled="disabled">
            <input type="submit" value="go!" disabled="disabled" class="btn">
          </c:when>
          <c:otherwise>
            <img class="helpIcon" src="images/icon_help.png" alt="Help" onmouseover="return getHelpText('search_help');">
            <input type="text" name="searchString" class="sventonSearchField" value="">
            <input type="submit" value="go!" class="btn">
          </c:otherwise>
        </c:choose>
      </td>
    </c:if>
  </tr>
</table>
  <!-- Needed by ASVNTC -->
  <input type="hidden" name="path" value="${command.path}">
  <input type="hidden" name="revision" value="${command.revision}">
  <input type="hidden" name="name" value="${command.name}">
</form>
