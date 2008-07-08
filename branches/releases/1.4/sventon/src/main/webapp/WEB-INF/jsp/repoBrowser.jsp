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
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<html>
<head>
  <%@ include file="/WEB-INF/jspf/pageHead.jspf"%>
  <title>sventon repository browser - ${repositoryURL}</title>
</head>
<body>
  <%@ include file="/WEB-INF/jspf/pageTop.jspf"%>

  <sventon:currentTargetHeader title="Repository Browser" target="${command.target}" hasProperties="true"/>
  <sventon:functionLinks pageName="repobrowser"/>

  <form method="post" action="#" name="entriesForm" onsubmit="return doAction(this);">
    <input type="hidden" name="path" value="${command.path}">
    <input type="hidden" name="revision" value="${command.revision}">
    <input type="hidden" name="name" value="${command.name}">
    <input type="hidden" name="pegrev" value="${!empty numrevision ? numrevision : command.revision}">
    
    <c:url value="repobrowser.svn" var="sortUrl">
      <c:param name="path" value="${command.path}" />
      <c:param name="revision" value="${command.revision}" />
      <c:param name="name" value="${command.name}" />
    </c:url>

    <table class="sventonEntriesTable">
      <%@ include file="/WEB-INF/jspf/sortableEntriesTableHeaderRow.jspf"%>
      <c:set var="rowCount" value="0"/>
      <c:set var="totalSize" value="0"/>
      <c:set var="backLinkVisible" value="false"/>

      <c:if test="${!empty command.pathNoLeaf}">
        <c:set var="backLinkVisible" value="true"/>
        <c:url value="repobrowser.svn" var="backUrl">
          <c:param name="path" value="${command.pathNoLeaf}"/>
          <c:param name="revision" value="${command.revision}"/>
          <c:param name="name" value="${command.name}"/>
        </c:url>

        <tr class="sventonEntryEven">
          <td class="sventonCol1"/>
          <td class="sventonCol2"><img src="images/icon_folder.png" alt="dir"></td>
          <td class="sventonCol3">
            <a href="<sventon-ui:formatUrl url='${backUrl}'/> ">..&nbsp;&nbsp;&nbsp;</a>
          </td>
          <td/>
          <td/>
          <td/>
          <td/>
          <td/>
        </tr>
        <c:set var="rowCount" value="${rowCount + 1}"/>
      </c:if>

      <c:forEach items="${svndir}" var="entry">
        <c:url value="repobrowser.svn" var="viewUrl">
          <c:param name="path" value="${entry.fullEntryName}" />
          <c:param name="revision" value="${command.revision}" />
          <c:param name="name" value="${command.name}" />
          <c:param name="bypassEmpty" value="true" />
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
        <c:url value="entrytray.ajax" var="entryTrayAddUrl">
          <c:param name="path" value="${entry.fullEntryName}" />
          <c:param name="revision" value="${entry.revision}" />
          <c:param name="name" value="${command.name}" />
          <c:param name="action" value="add" />
        </c:url>

        <c:set var="totalSize" value="${totalSize + entry.size}"/>

        <tr class="${rowCount mod 2 == 0 ? 'sventonEntryEven' : 'sventonEntryOdd'}">
          <td class="sventonCol1">
            <input type="checkbox" name="entry" value="${entry.fullEntryName};;${entry.revision}">
          </td>
          <c:choose>
            <c:when test="${'dir' eq entry.kind}">
              <td class="sventonCol2">
                <div id="<sventon-ui:formatUrl url='${entryTrayAddUrl}'/>" class="entry">
                  <img src="images/icon_folder.png" alt="dir">
                </div>
              </td>
              <td class="sventonCol3"><a href="<sventon-ui:formatUrl url='${viewUrl}'/>">${entry.name}</a></td>
            </c:when>
            <c:otherwise>
              <td class="sventonCol2">
                <div id="${entryTrayAddUrl}" class="entry">
                  <sventon-ui:fileTypeIcon filename="${entry.name}"/>
                </div>
              </td>
              <td class="sventonCol3"><a href="<sventon-ui:formatUrl url="${showFileUrl}" />">${entry.name}</a></td>
            </c:otherwise>
          </c:choose>
          <td class="sventonCol4" align="center">
            <c:set var="lock" value="${locks[entry.fullEntryName]}" scope="page"/>
            <c:if test="${!empty lock}">
              <jsp:useBean id="lock" type="org.tmatesoft.svn.core.SVNLock" />
              <span onmouseover="Tip('<table><tr><td><b>Owner</b></td><td><%=StringEscapeUtils.escapeJavaScript(lock.getOwner())%></td></tr><tr><td><b>Comment</b></td><td style=\'white-space: nowrap\'>${lock.comment}</td></tr><tr><td><b>Created</b></td><td style=\'white-space: nowrap\'><fmt:formatDate type="both" value="${lock.creationDate}" dateStyle="short" timeStyle="short"/></td></tr><tr><td><b>Expires</b></td><td style=\'white-space: nowrap\'><fmt:formatDate type="both" value="${lock.expirationDate}" dateStyle="short" timeStyle="short"/></td></tr></table>')"><img src="images/icon_lock.png"></span>
            </c:if>
          </td>
          <td class="sventonCol5"><c:if test="${'file' eq entry.kind}">${entry.size}</c:if></td>
          <td class="sventonCol6">
            <a href="${showRevInfoUrl}" onmouseover="getLogMessage(${entry.revision}, '${command.name}', '<fmt:formatDate type="both" value="${entry.date}" dateStyle="short" timeStyle="short"/>');">
              ${entry.revision}
            </a>
          </td>
          <td class="sventonCol7">${entry.author}</td>
          <td class="sventonCol8">
            <fmt:formatDate type="both" value="${entry.date}" dateStyle="short" timeStyle="short"/>
          </td>
        </tr>
        <c:set var="rowCount" value="${rowCount + 1}"/>
      </c:forEach>

      <tr class="${rowCount mod 2 == 0 ? 'sventonEntryEven' : 'sventonEntryOdd'}">
        <td colspan="2" class="sventonCol1" align="right"><b>Total:</b></td>
        <td><b>${backLinkVisible ? rowCount - 1 : rowCount} entries</b></td>
        <td/>
        <td align="right" title="${totalSize} bytes"><b><sventon-ui:formatBytes size="${totalSize}" locale="${pageContext.request.locale}"/></b></td>
        <td/>
        <td/>
        <td/>
      </tr>
      <tr>
        <%@ include file="/WEB-INF/jspf/actionSelectList.jspf"%>
        <td colspan="5"/>
      </tr>
    </table>
  </form>

  <c:if test="${bypassed}">
    <script type="text/javascript">
      new Effect.Highlight($('clickableUrl'), {startcolor: '#ffff00', restorecolor: 'true', duration: 3});
    </script>
  </c:if>

  <div align="right" class="entryTrayContainer" id="entryTrayContainerDiv">
    <table class="entryTrayHeaderTable">
      <tr>
        <td>
          <spring:message code="entrytray.dragdrop.header"/>&nbsp;
          <a class="sventonHeaderLink" href="#" onclick="toggleInnerHTML('hideShowTrayLink', '[hide]', '[show]'); showHideEntryTray(); return false;"><span id="hideShowTrayLink">[show]</span></a>
        </td>
      </tr>
    </table>
    <div id="entryTrayWrapper" style="display: none">
      <div id="entryTray">
        <%@ include file="/WEB-INF/jsp/ajax/entryTray.jsp"%>
      </div>
    </div>
  </div>

  <script type="text/javascript">
    var entries = document.getElementsByClassName('entry');
    for (var i = 0; i < entries.length; i++) {
      new Draggable(entries[i].id, {revert:true})
    }
    Droppables.add('entryTrayContainerDiv', {onDrop:
        function(element, dropon, event) {
          var ajax = new Ajax.Updater({success: $('entryTray')}, element.id + '&pegrev=${!empty numrevision ? numrevision : command.revision}', {
            method: 'post', onFailure: reportAjaxError, onComplete: function(request) {
            Element.hide('spinner');
          }
          });
          Element.show('spinner');
        }
    })
  </script>

<%@ include file="/WEB-INF/jspf/pageFoot.jspf"%>
</body>
</html>
