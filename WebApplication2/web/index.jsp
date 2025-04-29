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
        /* --- Styles --- */
        body { margin: 0; font-family: Arial, sans-serif; background: #f2f4f5; }
        header { background-color: #8a2be2; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        header .logo { font-size: 20px; font-weight: bold; }
        /* Styles for included navbar */
        nav { display: flex; }
        nav a { color: white; text-decoration: none; margin-left: 20px; font-size: 14px; }
        nav a.logout-link { padding: 5px 10px; background-color: #dc3545; border-radius: 4px; }
        nav a.logout-link:hover { background-color: #c82333; }
        /* --- Other page styles --- */
        .slider { width: 90%; max-width: 1000px; height: 400px; margin: 20px auto; overflow: hidden; border-radius: 10px; position: relative; }
        .slides { display: flex; width: 300%; height: 100%; animation: slide 12s infinite; }
        .slides img { width: calc(100% / 3); height: 100%; object-fit: cover; background-color: #ccc; }
        @keyframes slide { 0% { margin-left: 0%; } 33% { margin-left: 0%; } 36% { margin-left: -100%; } 66% { margin-left: -100%; } 69% { margin-left: -200%; } 99% { margin-left: -200%; } 100% { margin-left: 0%; } }
        .section-title { text-align: center; font-size: 28px; font-weight: bold; margin: 40px 0 20px 0; position: relative; color: #333; }
        .section-title::after { content: ""; display: block; width: 80px; height: 3px; background-color: #8a2be2; margin: 10px auto 0; border-radius: 2px; }
        .cards { display: flex; flex-wrap: wrap; justify-content: center; gap: 60px; padding: 0 20px 60px 20px; max-width: 1000px; margin: 0 auto; }
        .card { flex: 0 1 calc(33.33% - 40px); box-sizing: border-box; background: white; border-radius: 8px; box-shadow: 0 0 5px rgba(0,0,0,0.1); overflow: hidden; margin-bottom: 30px; display: flex; flex-direction: column; } /* Added flex */
        .card img { width: 100%; height: 150px; object-fit: cover; background-color: #eee; flex-shrink: 0; }
        .card-content { padding: 15px; flex-grow: 1; display: flex; flex-direction: column; } /* Added flex */
        .card-title { font-weight: bold; margin-bottom: 5px; }
        .card-date { font-size: 14px; color: #666; margin-bottom: 5px; } /* Adjusted */
        .card-location { font-size: 14px; color: #666; margin-bottom: 10px; font-style: italic; flex-grow: 1;} /* Added grow */
        .card a.participate-button { text-decoration: none; background: #28a745; color: white; padding: 8px 12px; display: block; border-radius: 4px; font-size: 14px; text-align: center; margin-top: auto; } /* Added styling */
        .card a.participate-button:hover { background-color: #218838; }
        .error-message { color: red; text-align: center; padding: 10px; background-color: #ffe0e0; border: 1px solid red; margin: 10px; }
        .no-events { text-align: center; color: #666; margin: 40px; }
    </style>
</head>
<body>
<header>
    <div class="logo">PetFestHub</div>
    <%@ include file="includes/navbar.jsp" %>
</header>

<c:if test="${not empty homeError}"><div class="error-message"><c:out value="${homeError}" /></div></c:if>

<div class="slider">
    <div class="slides">
        <img src="${pageContext.request.contextPath}/images/1.png" alt="Slide 1">
        <img src="${pageContext.request.contextPath}/images/images.jpg" alt="Slide 2">
        <img src="${pageContext.request.contextPath}/images/3.jpg" alt="Slide 3">
    </div>
</div>

<div class="section-title">Upcoming Events</div>

<div class="cards">
    <c:choose>
        <c:when test="${not empty upcomingEvents}">
            <c:forEach var="event" items="${upcomingEvents}">
                <div class="card">
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

                        <%-- **** MODIFIED: Changed link to Participate **** --%>
                        <a href="${pageContext.request.contextPath}/ParticipateServlet?eventId=${event.eventId}" class="participate-button">Participate</a>
                        <%-- **** END MODIFICATION **** --%>

                    </div>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise><p class="no-events">No upcoming events found.</p></c:otherwise>
    </c:choose>
</div>
</body>
</html>