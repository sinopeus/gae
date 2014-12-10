<%@page import="java.util.List"%>
<%@page import="ds.gae.entities.Notification"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="ds.gae.CarRentalModel"%>
<%@page import="ds.gae.view.JSPSite"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<% 
	String renter = (String)session.getAttribute("renter");
	JSPSite currentSite = JSPSite.CONFIRM_QUOTES_RESPONSE;

%>   
 
<%@include file="_header.jsp" %>

<% 
if (currentSite != JSPSite.LOGIN && currentSite != JSPSite.PERSIST_TEST && renter == null) {
 %>
	<meta http-equiv="refresh" content="0;URL='/login.jsp'">
<% 
  request.getSession().setAttribute("lastSiteCall", currentSite);
} 
 %>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="style.css" />
	<title>Car Rental Application</title>
</head>
<body>
	<div id="mainWrapper">
		<div id="headerWrapper">
			<h1>Car Rental Application</h1>
		</div>
		<div id="navigationWrapper">
			<ul>
<% 
for (JSPSite site : JSPSite.publiclyLinkedValues()) {
	if (site == currentSite) {
 %> 
				<li><a class="selected" href="<%=site.url()%>"><%=site.label()%></a></li>
<% } else {
 %> 
				<li><a href="<%=site.url()%>"><%=site.label()%></a></li>
<% }}
 %> 

				</ul>
		</div>
		<div id="contentWrapper">
<% if (currentSite != JSPSite.LOGIN) { %>
			<div id="userProfile">
				<span>Logged-in as <%= renter %> (<a href="/login.jsp">change</a>)</span>
			</div>
<%
   }
 %>
			<div class="frameDiv" style="margin: 150px 150px;">
				<H2>Notifications</H2>
				<div class="group">
					<table>
				<%
	List<Notification> notifications = CarRentalModel.get().getNotifications(renter);
	
	if ( notifications != null && notifications.size() > 0) {
		
		for (Notification n : notifications) { 
	 %>
					<tr>
						<td><strong><%=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(n.getTime())%></strong></td>
						<td><%=n.getMsg()%></td>
					</tr>
						<%
		} 
	} else {
	 %>
					<p>No notifications</p>
	<%
	} 
	 %>	
	 				</table>
				</div>
			</div>

<%@include file="_footer.jsp" %>

