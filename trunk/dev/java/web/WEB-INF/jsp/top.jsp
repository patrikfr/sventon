<%@ page session="false"%>
<table width="100%" class="sventonHeader"><tr>
<td>sventon subversion web client</td>
<td align="right">
<c:choose>
  <c:when test="${empty uid}" > 
You are not logged in
  </c:when> 
  <c:otherwise> 
You are logged in as: <c:out value="${uid}" /> - <a href="clearsession.svn">Log out</a>
  </c:otherwise> 
</c:choose>  
</td></tr></table>
<p>
  <span class="sventonLocation">
  Repository path: <a href="repobrowser.svn">
  <c:out value="${url}"/></a> /
  <c:forTokens items="${pathPart}" delims="/" var="pathSegment">
  	<c:set var="accuPath" scope="page" value="${accuPath}${pathSegment}/"/>
  	<a href="<c:out value="repobrowser.svn?path=${accuPath}&revision=${revision}"/>"><c:out value="${pathSegment}"/></a> /
  </c:forTokens>
   	<c:out value="${target}"/>
  <p> 
      Revision: <c:out value="${revision}" /> 
      </p>
      </span>
 <p> 
 <form method="get" action="repobrowser.svn">
    <c:out value="${status.errorMessage}"/>
 <table>
 <tr>
 <td>Go to Revision</td><td colspan="2">Go to path</td>
 </tr>
<tr>
<td><input type="text" name="revision" value="<c:out value="${revision}"/>"/></td>
<td><input class="sventonGoTo" type="text" name="path" value="<c:out value="${path}"/>" /></td>
<td><input type="submit" value="go to"/></td>
</tr>
</table>
</form>
</p>


