<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %> <%-- Ensure session is available --%>
<%-- JSTL core library needed for c:if and c:out --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    // --- Basic Authorization Check ---
    // Check if user information exists in the session. If not, redirect to login.
    if (session.getAttribute("userId") == null || session.getAttribute("userName") == null) {
        response.sendRedirect("login.jsp?error=Please login to access this page.");
        return; // Stop processing
    }
    // --- End Authorization Check ---
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
        nav { display: flex; }
        nav a { color: white; text-decoration: none; margin-left: 20px; font-size: 14px; }
        .main-content { flex: 1; padding: 40px; text-align: center;}
        .main-content h1 { color: #333; }
        .main-content p { color: #555; line-height: 1.6; }
         /* Basic logout link style */
        .logout-link { color: white; text-decoration: none; font-size: 14px; padding: 5px 10px; background-color: #dc3545; border-radius: 4px; }
        .logout-link:hover { background-color: #c82333; }
    </style>
</head>
<body>

    <header>
        <div class="logo">PetFestHub</div>
        <nav>
            <a href="${pageContext.request.contextPath}/home">Home</a>
            <a href="${pageContext.request.contextPath}/ViewEventsServlet">Events</a>
            <a href="gallery.jsp">Gallery</a>
            <a href="create-event.jsp">Create Event</a>
            <%-- Placeholder for logout --%>
            <%-- <a href="UserLogoutServlet" class="logout-link">Logout</a> --%>
        </nav>
    </header>

    <div class="main-content">
        <h1>Welcome Back to PetFestHub!</h1>

        <%-- Retrieve user name from session using JSTL/EL --%>
        <p>Hello, <c:out value="${sessionScope.userName}"/>!</p>
        <p>You are successfully logged in.</p>

        <%-- Optional: Display user ID --%>
        <%-- <p>(User ID: <c:out value="${sessionScope.userId}"/>)</p> --%>

        <p>What would you like to do next?</p>
        <%-- Add links to other features available to logged-in users --%>
        <p>
            <a href="${pageContext.request.contextPath}/ViewEventsServlet">View Events</a> |
            <a href="create-event.jsp">Request an Event</a>
            <%-- Add more links as needed --%>
        </p>

    </div>

</body>
</html>