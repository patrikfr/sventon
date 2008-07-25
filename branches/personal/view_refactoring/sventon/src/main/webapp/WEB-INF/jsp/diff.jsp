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
  <%@ include file="/WEB-INF/jspf/pageHead.jspf"%>
  <title>Diff View</title>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jhighlight.css" >
</head>

<body>
  <script language="JavaScript" type="text/javascript" src="${pageContext.request.contextPath}/js/wz_tooltip.js"></script>
  <%@ include file="/WEB-INF/jspf/spinner.jspf"%>
  <sventon:topHeaderTable command="${command}" repositoryNames="${repositoryNames}"/>
  <%@ include file="/WEB-INF/jspf/pageTop.jspf"%>

  <sventon:currentTargetHeader title="Diff View" target="${command.target}" properties="${properties}"/>

  <form name="searchForm" action="#" method="get" onsubmit="return doSearch(this, '${command.name}', '${command.path}');">
    <table class="sventonFunctionLinksTable">
      <tr>
        <td style="white-space: nowrap;">
          <sventon:diffFunctionButtons command="${command}" diffCommand="${diffCommand}"/>
          <sventon:charsetSelectList charsets="${charsets}" currentCharset="${userRepositoryContext.charset}"/>
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

  <c:choose>
    <c:when test="${isIdentical}">
      <p><b><spring:message code="diff.error.identical.files" arguments="${diffCommand.fromTarget},${diffCommand.fromRevision},${diffCommand.toTarget},${diffCommand.toRevision}"/></b></p>
    </c:when>
    <c:otherwise>
      <c:choose>
        <c:when test="${!isBinary}">
          <table id="diffTable" class="sventonDiffTable" cellspacing="0">
            <tr>
              <th style="background-color: white;">
                <a href="javascript:;" onclick="document.location.hash='#diff0';">
                  <img src="images/icon_nextdiff.png" alt="Next diff" title="Next diff">
                </a>
              </th>
              <th class="lineNo">&nbsp;</th>
              <th style="background-color: white;">&nbsp;</th>
              <th width="50%" style="background-color: white; border-bottom: 1px solid black">${diffCommand.fromTarget} @ revision ${diffCommand.fromRevision}</th>
              <th class="lineNo">&nbsp;</th>
              <th style="background-color: white;">&nbsp;</th>
              <th width="50%" style="background-color: white; border-bottom: 1px solid black">${diffCommand.toTarget} @ revision ${diffCommand.toRevision}</th>
            </tr>
            <c:set var="diffCount" value="0"/>
            <c:forEach items="${diffResult}" var="row">
              <jsp:useBean id="row" type="org.sventon.model.SideBySideDiffRow"/>
              <tr <%= row.getRight().getRowNumber() != null ? "id=\"l" + row.getRight().getRowNumber().toString() + "\"" : "" %>>
                <td style="background-color: white;">
                  <c:if test="${!row.isUnchanged}">
                    <a name="diff${diffCount}"></a>
                    <c:set var="diffCount" value="${diffCount + 1}"/>
                    <a href="javascript:;" onclick="document.location.hash='#diff${diffCount}';">
                      <img src="images/icon_nextdiff.png" alt="Next diff" title="Next diff">
                    </a>
                  </c:if>
                </td>

                <td class="lineNo">
                  <%= row.getLeft().getRowNumber() != null ? row.getLeft().getRowNumber().toString() : "" %>
                </td>
                <td style="text-align: center; background-color: white;">
                  <b><%= row.getLeft().getAction().getSymbol() %></b>
                </td>
                <td class="<%= row.getLeft().getAction().getCSSClass() %>">
                  <span title="<%= row.getLeft().getAction().getDescription() %>">
                    <%
                      String line = row.getLeft().getLine();
                      out.print("".equals(line) ? "&nbsp;" : line);
                    %>
                  </span>
                </td>
                <td class="lineNo">
                  <%= row.getRight().getRowNumber() != null ? row.getRight().getRowNumber().toString() : "" %>
                </td>
                <td style="text-align: center; background-color: white;">
                  <b><%= row.getRight().getAction().getSymbol() %></b>
                </td>
                <td class="<%= row.getRight().getAction().getCSSClass() %>">
                  <span title="<%= row.getRight().getAction().getDescription() %>">
                    <%
                      line = row.getRight().getLine();
                      out.print("".equals(line) ? "&nbsp;" : line);
                    %>
                  </span>
                </td>
              </tr>
            </c:forEach>
          </table>
          <a name="diff${diffCount}"/>
        </c:when>
        <c:otherwise>
          <p><b><spring:message code="diff.error.binary"/></b></p>
        </c:otherwise>
      </c:choose>
    </c:otherwise>
  </c:choose>

<%@ include file="/WEB-INF/jspf/pageFoot.jspf"%>
</body>
</html>