<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%-- JSTL core library - declared once at the top --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    // Authorization Check
    if (session.getAttribute("userId") == null || session.getAttribute("userName") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+to+access+this+page."); // Use context path
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Welcome - PetFestHub</title>
    <style>
        /* Basic styles */
         body { margin: 0; font-family: Arial, sans-serif; background: #f2f4f5; display: flex; flex-direction: column; min-height: 100vh; }
        header { background-color: #8a2be2; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        header .logo { font-size: 20px; font-weight: bold; }
        /* Styles for included navbar */
        nav { display: flex; }
        nav a { color: white; text-decoration: none; margin-left: 20px; font-size: 14px; }
        nav a.logout-link { padding: 5px 10px; background-color: #dc3545; border-radius: 4px; }
        nav a.logout-link:hover { background-color: #c82333; }
        /* Other styles */
        .main-content { flex: 1; padding: 40px; text-align: center;}
        .main-content h1 { color: #333; }
        .main-content p { color: #555; line-height: 1.6; }
    </style>
</head>
<body>

    <header>
        <div class="logo">PetFestHub</div>
         <%-- **** CORRECTED: Include directive replaces the <nav> block **** --%>
         <%@ include file="includes/navbar.jsp" %>
         <%-- **** NO surrounding <nav> tags here **** --%>
    </header>

    <div class="main-content">
        <h1>Welcome Back to PetFestHub!</h1>

        <p>Hello, <c:out value="${sessionScope.userName}"/>!</p>
        <p>You are successfully logged in.</p>

        <%-- Optional: Display user ID --%>
        <%-- <p>(User ID: <c:out value="${sessionScope.userId}"/>)</p> --%>

        <p>What would you like to do next?</p>
        <p>
            <a href="${pageContext.request.contextPath}/ViewEventsServlet">View Events</a> |
            <a href="create-event.jsp">Request an Event</a>
            <%-- Add more links as needed --%>
        </p>
    </div>

</body>
</html>