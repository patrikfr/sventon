<%@ include file="/WEB-INF/jsp/include.jsp"%>
<%@ page import="de.berlios.sventon.svnsupport.SourceLine"%>

<html>
  <head>
    <title>Show file</title>
    <%@ include file="/WEB-INF/jsp/head.jsp"%>
    <link rel="stylesheet" type="text/css" href="jhighlight.css" >
  </head>

  <body>
    <%@ include file="/WEB-INF/jsp/top.jsp"%>

    <p>
      <table class="sventonHeader"><tr><td>
    Show File - <b><c:out value="${command.target}"/></b></td></tr></table>
    </p>

  <br/>

    <table class="sventonFunctionLinksTable">
      <tr>
      </tr>
    </table>

<c:set var="leftLines" value="${leftFileContents}" />
<c:set var="rightLines" value="${rightFileContents}" />
<jsp:useBean id="leftLines" type="de.berlios.sventon.svnsupport.CustomArrayList" />
<jsp:useBean id="rightLines" type="de.berlios.sventon.svnsupport.CustomArrayList" />

<table class="codeBlock" cellspacing="0" cellpadding="1">
  <tr>
    <th>&nbsp;</th>
    <th width="50%">Revision X</th>
    <th>&nbsp;</th>
    <th width="50%">Revision Y</th>
  </tr>

<%
  SourceLine line = null;
  for (int i = 0; i < leftLines.size(); i++) {
%>
  <tr>
<%
    line = (SourceLine) leftLines.get(i);
    String css = "src";
    String sign = "&nbsp;";
    if ("A".equals(line.getAction())) {
      css = "srcAdd";
      sign = "+";
    } else if ("D".equals(line.getAction())) {
      css = "srcDel";
      sign = "-";
    } else if ("C".equals(line.getAction())) {
      css = "srcChg";
      sign = "+";
    }
%>
    <td><b><%=sign%></b></td>
    <td class="<%=css%>"><% if ("".equals(line.getLine())) out.print("&nbsp;"); else out.print(line.getLine());%></td>
<%
    line = (SourceLine) rightLines.get(i);
%>
    <td><b><%=sign%></b></td>
    <td class="<%=css%>"><% if ("".equals(line.getLine())) out.print("&nbsp;"); else out.print(line.getLine());%></td>
  </tr>
<%
  }
%>

</table>
<br>
<%@ include file="/WEB-INF/jsp/foot.jsp"%>
  </body>
</html>
