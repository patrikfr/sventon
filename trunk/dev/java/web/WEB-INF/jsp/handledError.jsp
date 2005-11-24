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
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
  <title><fmt:message key="${errorHeadingKey}"/></title>
  <%@ include file="/WEB-INF/jsp/head.jsp" %>
</head>

<h3><fmt:message key="${errorHeadingKey}"/></h3>

<p/>
<table class="sventonErrorMessageTable">
  <tr>
    <td>
      <fmt:message key="${errorMessageKey}"/>
    </td>
  </tr>
</table>
</html>