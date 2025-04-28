<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <title>Request Event - PetFestHub</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { font-family: Arial, sans-serif; background-color: #f2f4f5; margin: 0; padding: 0; }
        .container { max-width: 600px; background: white; margin: 50px auto; padding: 40px; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
        header { background-color: #8a2be2; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        header .logo { font-size: 20px; font-weight: bold; }
        h1 { text-align: center; color: #8a2be2; margin-top: 0; }
        input, textarea, button { width: 100%; padding: 12px; margin: 10px 0; font-size: 16px; border-radius: 5px; border: 1px solid #ccc; box-sizing: border-box; }
        nav { display: flex; }
        nav a { color: white; text-decoration: none; margin-left: 20px; font-size: 14px; }
        button { background-color: #8a2be2; color: white; border: none; cursor: pointer; }
        button:hover { background-color: #7a1da3; }
        label { font-weight: bold; margin-top: 10px; display: block; }
        .message { padding: 10px; margin-bottom: 15px; border-radius: 4px; text-align: center; font-size: 14px; }
        .message.success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .message.error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        input[type="file"] { padding: 10px; background-color: #eee; border: 1px solid #ccc; }
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
        <a href="login.jsp">Login/Signup</a>
    </nav>
</header>
<div class="container">
    <h1>Request an Event</h1>

    <%-- Display messages from servlet redirect --%>
    <c:if test="${not empty param.message}">
        <div class="message success"><c:out value="${param.message}"/></div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="message error"><c:out value="${param.error}"/></div>
    </c:if>

    <%-- MODIFIED: Added enctype for file upload --%>
    <form action="SendEventRequestServlet" method="post" enctype="multipart/form-data">
        <label for="eventName">Event Name:</label>
        <input type="text" name="eventName" id="eventName" required>

        <label for="location">Location:</label>
        <input type="text" name="location" id="location" required>

        <label for="date">Preferred Date:</label>
        <input type="date" name="date" id="date" required>

        <label for="description">Event Description:</label>
        <textarea name="description" id="description" rows="5" required></textarea>

        <%-- NEW: File input for image --%>
        <label for="eventImage">Event Image (Optional):</label>
        <input type="file" name="eventImage" id="eventImage" accept="image/png, image/jpeg, image/gif">

        <button type="submit">Send Request</button>
    </form>
</div>
</body>
</html>