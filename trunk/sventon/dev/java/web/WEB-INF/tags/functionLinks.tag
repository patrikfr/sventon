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
<%@ tag body-content="empty" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="/WEB-INF/spring.tld" %>
<%@ attribute name="pageName" required="true" type="java.lang.String" %>

<!-- Prepare link URLs -->

<c:url var="downloadLinkUrl" value="get.svn">
  <c:param name="path" value="${command.path}${entry.name}" />
  <c:param name="revision" value="${command.revision}" />
  <c:param name="name" value="${command.name}" />
</c:url>
<c:url var="showLogLinkUrl" value="showlog.svn">
  <c:param name="path" value="${command.path}${entry.name}" />
  <c:param name="revision" value="${command.revision}" />
  <c:param name="name" value="${command.name}" />
</c:url>
<c:url var="showLockLinkUrl" value="showlocks.svn">
  <c:param name="path" value="${command.path}${entry.name}" />
  <c:param name="revision" value="${command.revision}" />
  <c:param name="name" value="${command.name}" />
</c:url>
<c:url var="showDirLinkUrl" value="repobrowser.svn">
  <c:param name="path" value="${command.path}" />
  <c:param name="revision" value="${command.revision}" />
  <c:param name="name" value="${command.name}" />
</c:url>
<c:url var="showFileLinkUrl" value="showfile.svn">
  <c:param name="path" value="${command.path}${entry.name}" />
  <c:param name="revision" value="${command.revision}" />
  <c:param name="name" value="${command.name}" />
</c:url>
<c:url var="blameLinkUrl" value="blame.svn">
  <c:param name="path" value="${command.path}${entry.name}" />
  <c:param name="revision" value="${command.revision}" />
  <c:param name="name" value="${command.name}" />
</c:url>
<c:url var="showArchivedFileLinkUrl" value="showfile.svn">
  <c:param name="path" value="${command.path}${entry.name}" />
  <c:param name="revision" value="${command.revision}" />
  <c:param name="name" value="${command.name}" />
  <c:param name="archivedEntry" value="${archivedEntry}" />
  <c:param name="force" value="true" />
</c:url>

<form name="searchForm" action="#" method="post" onsubmit="return doSearch(searchForm);">
<table class="sventonFunctionLinksTable" border="0">
  <tr><td style="white-space: nowrap;">

<c:choose>

  <c:when test="${pageName == 'showTextFile'}">
    <c:choose>
      <c:when test="${archivedEntry ne null}">
        <input type="button" class="btn" value="<spring:message code="showarchivefile.button.text"/>" onclick="javascript:parent.location='${showFileLinkUrl}';"/>        
      </c:when>
      <c:otherwise>
        <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target}"/>')" onclick="javascript:parent.location='${showLogLinkUrl}';"/>
        <input type="button" class="btn" value="<spring:message code="download.button.text"/>" onclick="javascript:parent.location='${downloadLinkUrl}';"/>
        <input type="button" class="btn" value="<spring:message code="blame.button.text"/>" onclick="javascript:parent.location='${blameLinkUrl}';"/>
        <input type="button" class="btn" value="<spring:message code="showrawfile.button.text"/>" onmouseover="Tip('<spring:message code="showrawfile.button.tooltip"/>')" onclick="javascript:parent.location='${showFileLinkUrl}&format=raw';"/>

        <c:if test="${!empty committedRevision}">
          <c:url value="diffprev.svn" var="diffPreviousUrl">
            <c:param name="path" value="${command.path}${entry.name}" />
            <c:param name="revision" value="${command.revision}" />
            <c:param name="name" value="${command.name}" />
            <c:param name="commitrev" value="${committedRevision}" />
          </c:url>
          <input type="button" class="btn" value="<spring:message code="diffprev.button.text"/>" onmouseover="Tip('<spring:message code="diffprev.button.tooltip" arguments="${committedRevision}"/>')" onclick="javascript:parent.location='${diffPreviousUrl}';"/>
          <%@ include file="/WEB-INF/jspf/charsetSelectList.jspf"%>
        </c:if>
      </c:otherwise>
    </c:choose>
  </c:when>

  <c:when test="${pageName == 'showImageFile' || pageName == 'showBinaryFile' || pageName == 'showArchiveFile'}">
    <c:choose>
      <c:when test="${archivedEntry eq null}">
        <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target}"/>')" onclick="javascript:parent.location='${showLogLinkUrl}';"/>
        <input type="button" class="btn" value="<spring:message code="download.button.text"/>" onclick="javascript:parent.location='${downloadLinkUrl}';"/>
      </c:when>
      <c:otherwise>
        <input type="button" class="btn" value="<spring:message code="showarchivefile.button.text"/>" onclick="javascript:parent.location='${showFileLinkUrl}';"/>
        <input type="button" class="btn" value="<spring:message code="force-display.button.text"/>" onclick="javascript:parent.location='${showArchivedFileLinkUrl}';"/>
      </c:otherwise>
    </c:choose>
  </c:when>

  <c:when test="${pageName == 'repobrowser'}">
    <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target eq '' ? '/' : command.target}"/>')" onclick="javascript:parent.location='${showLogLinkUrl}';"/>
    <input type="button" class="btn" value="<spring:message code="showlocks.button.text"/>" onclick="javascript:parent.location='${showLockLinkUrl}';"/>
    <c:choose>
      <c:when test="${useCache}">
        <c:choose>
          <c:when test="${isUpdating}">
            <input type="button" class="btn" value="<spring:message code="flatten.button.text"/>" onclick="javascript:return doFlatten('${command.path}', '${command.name}');" disabled onmouseover="Tip('<spring:message code="flatten.button.isupdating.tooltip"/>')" />
          </c:when>
          <c:when test="${!isHead}">
            <input type="button" class="btn" value="<spring:message code="flatten.button.text"/>" onclick="javascript:return doFlatten('${command.path}', '${command.name}');" disabled onmouseover="Tip('<spring:message code="flatten.button.disabled.tooltip"/>')" />
          </c:when>
          <c:when test="${isFlatten}">
            <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="javascript:parent.location='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')" />
          </c:when>
          <c:otherwise>
            <input type="button" class="btn" value="<spring:message code="flatten.button.text"/>" onclick="javascript:return doFlatten('${command.path}', '${command.name}');" onmouseover="Tip('<spring:message code="flatten.button.tooltip"/>')" />
          </c:otherwise>
        </c:choose>
      </c:when>
    </c:choose>
    <c:if test="${!isFlatten && !isEntrySearch && !isLogSearch}">
      </td>
      <td align="right" style="white-space: nowrap;"><spring:message code="filter.text"/></td>
      <td style="white-space: nowrap;">
        <select name="filterExtension" class="sventonSelect">
          <option value="all">&lt;show all&gt;</option>
          <c:forEach items="${existingExtensions}" var="extension">
            <option value="${extension}" ${extension eq filterExtension ? "selected" : ""}>${extension}</option>
          </c:forEach>
        </select>
        <input type="button" class="btn" value="<spring:message code="filter.button.text"/>" onclick="javascript:parent.location='${showDirLinkUrl}&filterExtension=' + this.form.filterExtension.options[this.form.filterExtension.selectedIndex].value;"/>
    </c:if>
  </c:when>

  <c:when test="${pageName == 'showLog'}">
    <c:choose>
      <c:when test="${isFile}">
        <input type="button" class="btn" value="<spring:message code="showfile.button.text"/>" onclick="javascript:parent.location='${showFileLinkUrl}';"/>
        <input type="button" class="btn" value="<spring:message code="download.button.text"/>" onclick="javascript:parent.location='${downloadLinkUrl}';"/>
      </c:when>
      <c:otherwise>
        <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="javascript:parent.location='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')" />
      </c:otherwise>
    </c:choose>
  </c:when>

  <c:when test="${pageName == 'showRevInfo'}">
    <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="javascript:parent.location='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')" />
  </c:when>

  <c:when test="${pageName == 'showDiff'}">
    <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target}"/>')" onclick="javascript:parent.location='${showLogLinkUrl}';"/>
    <input type="button" class="btn" value="<spring:message code="showfile.button.text"/>" onclick="javascript:parent.location='${showFileLinkUrl}';"/>

    <c:url value="unifieddiff.svn" var="unifiedDiffUrl">
      <c:param name="path" value="${command.path}${entry.name}" />
      <c:param name="revision" value="${command.revision}" />
      <c:param name="name" value="${command.name}" />
      <c:param name="entry" value="${diffCommand.toPath};;${diffCommand.toRevision}" />
      <c:param name="entry" value="${diffCommand.fromPath};;${diffCommand.fromRevision}" />
    </c:url>
    <input type="button" class="btn" value="<spring:message code="unifieddiff.button.text"/>" onmouseover="Tip('<spring:message code="unifieddiff.button.tooltip"/>')" onclick="javascript:parent.location='${unifiedDiffUrl}';"/>
    <input type="button" class="btn" value="<spring:message code="wrap-nowrap.button.text"/>" onclick="javascript:toggleWrap();"/>
    <%@ include file="/WEB-INF/jspf/charsetSelectList.jspf"%>
  </c:when>

  <c:when test="${pageName == 'showUnifiedDiff'}">
    <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target}"/>')" onclick="javascript:parent.location='${showLogLinkUrl}';"/>
    <input type="button" class="btn" value="<spring:message code="showfile.button.text"/>" onclick="javascript:parent.location='${showFileLinkUrl}';"/>

    <c:url value="diff.svn" var="diffUrl">
      <c:param name="path" value="${command.path}${entry.name}" />
      <c:param name="revision" value="${command.revision}" />
      <c:param name="name" value="${command.name}" />
      <c:param name="entry" value="${diffCommand.toPath};;${diffCommand.toRevision}" />
      <c:param name="entry" value="${diffCommand.fromPath};;${diffCommand.fromRevision}" />
    </c:url>
    <input type="button" class="btn" value="<spring:message code="diff.button.text"/>" onmouseover="Tip('<spring:message code="diff.button.tooltip"/>')" onclick="javascript:parent.location='${diffUrl}';"/>
    <%@ include file="/WEB-INF/jspf/charsetSelectList.jspf"%>
  </c:when>

  <c:when test="${pageName == 'showBlame'}">
    <input type="button" class="btn" value="<spring:message code="showlog.button.text"/>" onmouseover="Tip('<spring:message code="showlog.button.tooltip" arguments="${command.target}"/>')" onclick="javascript:parent.location='${showLogLinkUrl}';"/>
    <input type="button" class="btn" value="<spring:message code="showfile.button.text"/>" onclick="javascript:parent.location='${showFileLinkUrl}';"/>
    <input type="button" class="btn" value="<spring:message code="download.button.text"/>" onclick="javascript:parent.location='${downloadLinkUrl}';"/>
    <%@ include file="/WEB-INF/jspf/charsetSelectList.jspf"%>
  </c:when>
        
  <c:when test="${pageName == 'showThumbs'}">
    <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="javascript:parent.location='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')" />
  </c:when>

  <c:when test="${pageName == 'showLock'}">
    <input type="button" class="btn" value="<spring:message code="showdir.button.text"/>" onclick="javascript:parent.location='${showDirLinkUrl}';" onmouseover="Tip('<spring:message code="showdir.button.tooltip" arguments="${command.path}"/>')" />
  </c:when>

  <c:otherwise>
    <spring:message code="functionlinks.error.message" arguments="${pagename}"/>
  </c:otherwise>
</c:choose>

    </td>
    <c:if test="${useCache}">
      <td align="right" style="white-space: nowrap;">
        <spring:message code="search.text"/>
        <input type="radio" id="entrySearch" name="searchMode" class="rdo" value="entries" ${userContext.searchMode eq 'entries' ? 'checked' : ''}/>
        <label for="entrySearch">entries</label>
        <input type="radio" id="logSearch" name="searchMode" class="rdo" value="logMessages" ${userContext.searchMode eq 'logMessages' ? 'checked' : ''}>
        <label for="logSearch">logs</label>
        <input type="text" name="searchString" class="sventonSearchField" value="" ${isUpdating || !isHead ? 'disabled' : ''} />
        <input type="hidden" name="startDir" value="${command.pathPart}"/>
        <c:choose>
          <c:when test="${isUpdating}">
            <input type="submit" value="go!" disabled onmouseover="Tip('<spring:message code="search.button.isupdating.tooltip"/>')" class="btn"/>
          </c:when>
          <c:when test="${!isHead}">
            <input type="submit" value="go!" disabled onmouseover="Tip('<spring:message code="search.button.disabled.tooltip"/>')" class="btn"/>
          </c:when>
          <c:otherwise>
            <input type="submit" value="go!" onmouseover="Tip('<spring:message code="search.button.tooltip"/>')" class="btn"/>
          </c:otherwise>
        </c:choose>
      </td>
    </c:if>
  </tr>
</table>
  <!-- Needed by ASVNTC -->
  <input type="hidden" name="path" value="${command.path}${entry.name}"/>
  <input type="hidden" name="revision" value="${command.revision}"/>
  <input type="hidden" name="name" value="${command.name}"/>
</form>
