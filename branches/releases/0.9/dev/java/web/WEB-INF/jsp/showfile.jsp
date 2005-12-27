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
<%@ include file="/WEB-INF/jspf/include.jspf"%>

<html>
  <head>
    <title>Show file</title>
    <%@ include file="/WEB-INF/jspf/head.jspf"%>
    <link rel="stylesheet" type="text/css" href="jhighlight.css" >
  </head>

  <body>
    <%@ include file="/WEB-INF/jspf/top.jspf"%>

    <c:url value="get.svn" var="downloadUrl">
      <c:param name="path" value="${command.path}${entry.name}" />
    </c:url>

    <c:url value="showlog.svn" var="showLogUrl">
      <c:param name="path" value="${command.path}${entry.name}" />
    </c:url>

    <p>
      <table class="sventonHeader"><tr><td>
    Show File - <b>${command.target}</b>&nbsp;<a href="javascript:toggleElementVisibility('propertiesDiv'); changeHideShowDisplay('propertiesLink');">[<span id="propertiesLink">show</span> properties]</a></td></tr></table>
      <%@ include file="/WEB-INF/jspf/sventonheader.jspf"%>
    </p>

  <br/>

    <table class="sventonFunctionLinksTable">
      <tr>
        <td><a href="${showLogUrl}&revision=${command.revision}">[Show log]</a></td>
        <td><a href="${downloadUrl}&revision=${command.revision}">[Download]</a></td>
      </tr>
    </table>

    <c:choose>
      <c:when test="${isBinary}">
        <c:choose>
          <c:when test="${isImage}">
<p>
  <a href="${downloadUrl}&revision=${command.revision}&disp=inline">
    <img src="${downloadUrl}&revision=${command.revision}&disp=thumb" alt="Thumbnail" border="0"/>
  </a>
</p>
          </c:when>
          <c:otherwise>
            <c:choose>
              <c:when test="${isArchive}">
                <%@ include file="/WEB-INF/jspf/showarchive.jspf"%>
              </c:when>
              <c:otherwise>
<p>File is in binary format.</p>
              </c:otherwise>
            </c:choose>
          </c:otherwise>
        </c:choose>
      </c:when>
     	<c:otherwise>
<pre class="codeBlock">
<c:out value="${fileContents}" escapeXml="false"/>
</pre>

      </c:otherwise>
    </c:choose>
<br>
<%@ include file="/WEB-INF/jspf/foot.jspf"%>
  </body>
</html>
