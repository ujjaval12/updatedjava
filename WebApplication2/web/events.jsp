<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Required tag libraries --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html>
<head>
    <title>PetFestHub - All Events</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
     <style>
        /* Your existing CSS styles */
        body { margin: 0; font-family: Arial, sans-serif; background: #f2f4f5; }
        header { background-color: #8a2be2; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        header .logo { font-size: 20px; font-weight: bold; }
        nav { display: flex; }
        nav a { color: white; text-decoration: none; margin-left: 20px; font-size: 14px; }
        nav a.logout-link { padding: 5px 10px; background-color: #dc3545; border-radius: 4px; }
        nav a.logout-link:hover { background-color: #c82333; }
        .cards { display: flex; flex-wrap: wrap; justify-content: center; gap: 60px; padding: 40px 20px 60px 20px; max-width: 1000px; margin: 0 auto; }
        .card { flex: 0 1 calc(33.33% - 40px); box-sizing: border-box; background: white; border-radius: 8px; box-shadow: 0 0 5px rgba(0,0,0,0.1); overflow: hidden; margin-bottom: 30px; display: flex; flex-direction: column; /* Ensure content pushes button down */ }
        .card img { width: 100%; height: 150px; object-fit: cover; background-color: #eee; flex-shrink: 0; /* Prevent image shrinking */ }
        .card-content { padding: 15px; flex-grow: 1; /* Allow content to grow */ display: flex; flex-direction: column; }
        .card-title { font-weight: bold; margin-bottom: 5px; }
        .card-date { font-size: 14px; color: #666; margin-bottom: 5px; }
        .card-location { font-size: 14px; color: #666; margin-bottom: 10px; font-style: italic; flex-grow: 1; /* Push button to bottom */ }
        .card a.participate-button { text-decoration: none; background: #28a745; /* Green for participate */ color: white; padding: 8px 12px; display: block; /* Make it block */ border-radius: 4px; font-size: 14px; text-align: center; margin-top: auto; /* Push to bottom */ }
        .card a.participate-button:hover { background-color: #218838; }
        .page-title { text-align: center; font-size: 28px; font-weight: bold; margin: 40px 0 20px 0; position: relative; color: #333; }
        .page-title::after { content: ""; display: block; width: 80px; height: 3px; background-color: #8a2be2; margin: 10px auto 0; border-radius: 2px; }
        .message { padding: 10px; margin-bottom: 15px; border-radius: 4px; text-align: center; font-size: 14px; } /* Global message style */
        .message.success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .message.error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .no-events { text-align: center; color: #666; margin: 40px; }
    </style>
</head>
<body>
<header>
    <div class="logo">PetFestHub</div>
    <%@ include file="includes/navbar.jsp" %>
</header>

<div class="page-title">All Events</div>

<%-- Display messages passed back from ParticipateServlet or ViewEventsServlet --%>
<c:if test="${not empty param.message}"><div class="message success"><c:out value="${param.message}"/></div></c:if>
<c:if test="${not empty param.error}"><div class="message error"><c:out value="${param.error}"/></div></c:if>
<c:if test="${not empty requestScope.eventsError}"><div class="message error"><c:out value="${requestScope.eventsError}"/></div></c:if>

<div class="cards">
    <c:choose>
        <c:when test="${not empty allEvents}">
            <c:forEach var="event" items="${allEvents}">
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
                        <%-- Link points to ParticipateServlet, passing the eventId --%>
                        <%-- Added class for styling --%>
                        <a href="${pageContext.request.contextPath}/ParticipateServlet?eventId=${event.eventId}" class="participate-button">Participate</a>
                        <%-- **** END MODIFICATION **** --%>
                    </div>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise><p class="no-events">No events have been created yet.</p></c:otherwise>
    </c:choose>
</div>
</body>
</html>