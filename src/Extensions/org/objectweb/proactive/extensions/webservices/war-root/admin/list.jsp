<%@ page import="java.util.*, org.apache.soap.Constants, org.apache.soap.server.*" %>

<h1>Service Listing</h1>

<% 
  String configFilename = config.getInitParameter(Constants.CONFIGFILENAME);
  if (configFilename == null)
    configFilename = application.getInitParameter(Constants.CONFIGFILENAME);

  ServiceManager serviceManager =
    org.apache.soap.server.http.ServerHTTPUtils.getServiceManagerFromContext(application, configFilename);

  String[] serviceNames = serviceManager.list ();
  if (serviceNames.length == 0) {
    out.println ("<p>Sorry, there are no services currently deployed.</p>");
  } else {
    out.println ("<p>Here are the deployed services (select one to see");
    out.println ("details)</p>");
    %>
    <ul>
    <%
    for (int i = 0; i < serviceNames.length; i++) {
      String id = serviceNames[i];
    %>
      <li><a href="showdetails.jsp?id=<%=id%>"><%= id%></a></li>
    <%
    }
    %>
    </ul>
    <%
  }
%>
