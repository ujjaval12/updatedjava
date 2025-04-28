<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>PetFestHub - Home</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        /* Your CSS styles */
        body { margin: 0; font-family: Arial, sans-serif; background: #f2f4f5; }
        header { background-color: #8a2be2; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        header .logo { font-size: 20px; font-weight: bold; }
        nav a { color: white; text-decoration: none; margin-left: 20px; font-size: 14px; }
        .slider { width: 90%; max-width: 1000px; height: 400px; margin: 20px auto; overflow: hidden; border-radius: 10px; position: relative; }
        .slides { display: flex; width: 300%; height: 100%; animation: slide 12s infinite; }
        .slides img { width: 100%; height: 100%; object-fit: cover; }
        @keyframes slide { 0% { margin-left: 0%; } 33% { margin-left: 0%; } 36% { margin-left: -100%; } 66% { margin-left: -100%; } 69% { margin-left: -200%; } 99% { margin-left: -200%; } 100% { margin-left: 0%; } }
        .section-title { text-align: center; font-size: 28px; font-weight: bold; margin: 40px 0 20px 0; position: relative; color: #333; }
        .section-title::after { content: ""; display: block; width: 80px; height: 3px; background-color: #8a2be2; margin: 10px auto 0; border-radius: 2px; }
        .cards { display: flex; flex-wrap: wrap; justify-content: center; gap: 60px; padding: 0 20px 60px 20px; max-width: 1000px; margin: 0 auto; }
        .card { flex: 0 1 calc(33.33% - 40px); box-sizing: border-box; background: white; border-radius: 8px; box-shadow: 0 0 5px rgba(0,0,0,0.1); overflow: hidden; margin-bottom: 30px; }
        .card img { width: 100%; height: 150px; object-fit: cover; background-color: #eee; }
        .card-content { padding: 15px; }
        .card-title { font-weight: bold; margin-bottom: 5px; }
        .card-date { font-size: 14px; color: #666; margin-bottom: 10px; }
        .card-location { font-size: 14px; color: #666; margin-bottom: 10px; font-style: italic;}
        .card a { text-decoration: none; background: #8a2be2; color: white; padding: 8px 12px; display: inline-block; border-radius: 4px; font-size: 14px; }
        .error-message { color: red; text-align: center; padding: 10px; background-color: #ffe0e0; border: 1px solid red; margin: 10px; }
        .no-events { text-align: center; color: #666; margin: 40px; }
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

<c:if test="${not empty homeError}"><div class="error-message"><c:out value="${homeError}" /></div></c:if>

<div class="slider">
    <div class="slides">
        <img src="${pageContext.request.contextPath}/images/slider1.jpg" alt="Slide 1">
        <img src="${pageContext.request.contextPath}/images/slider2.jpg" alt="Slide 2">
        <img src="${pageContext.request.contextPath}/images/slider3.jpg" alt="Slide 3">
    </div>
</div>

<div class="section-title">Upcoming Events</div>

<div class="cards">
    <c:choose>
        <c:when test="${not empty upcomingEvents}">
            <c:forEach var="event" items="${upcomingEvents}">
                <div class="card">
                    <%-- MODIFIED: Point to ImageServlet --%>
                    <c:choose>
                        <c:when test="${not empty event.imageFilename}">
                            <img src="${pageContext.request.contextPath}/ImageServlet/${event.imageFilename}" alt="<c:out value="${event.title}"/>">
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}/images/default-event.png" alt="Default Event Image">
                        </c:otherwise>
                    </c:choose>
                    <div class="card-content">
                        <div class="card-title"><c:out value="${event.title}"/></div>
                        <div class="card-date"><fmt:formatDate value="${event.eventDate}" pattern="yyyy-MM-dd"/></div>
                         <div class="card-location"><c:if test="${not empty event.location}">Location: <c:out value="${event.location}"/></c:if></div>
                        <a href="EventDetailsServlet?eventId=${event.eventId}">Learn More</a>
                    </div>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise><p class="no-events">No upcoming events found.</p></c:otherwise>
    </c:choose>
</div>
</body>
</html>