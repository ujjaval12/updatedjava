<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    // Basic Authorization Check
    Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
    if (isAdmin == null || !isAdmin) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?adminError=Admin access required.");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%-- Set title dynamically based on event --%>
    <title>Participants - <c:out value="${not empty eventDetails ? eventDetails.title : 'Event'}"/> - PetFestHub</title>
    <style>
        body { font-family: sans-serif; margin: 0; background-color: #f4f4f4; }
        .container { padding: 20px; max-width: 900px; margin: 20px auto; background-color: #fff; box-shadow: 0 0 10px rgba(0,0,0,0.1); border-radius: 8px;}
        h1, h2 { color: #333; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 20px; }
        h1 { border-bottom-width: 2px; border-color: #8a2be2;}
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 10px 12px; text-align: left; font-size: 14px; }
        th { background-color: #8a2be2; color: white; font-weight: bold; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        .message { padding: 10px; margin-bottom: 15px; border-radius: 4px; }
        .message.error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .no-participants { color: #555; font-style: italic; text-align: center; padding: 20px; }
        header { background-color: #8a2be2; color: white; padding: 15px 30px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
        header .logo { font-size: 20px; font-weight: bold; }
        nav a { color: white; text-decoration: none; margin-left: 20px; font-size: 14px; }
        nav a.logout-link { padding: 5px 10px; background-color: #dc3545; border-radius: 4px; }
        nav a.logout-link:hover { background-color: #c82333; }
        .event-details p { margin: 5px 0; font-size: 15px; color: #444;}
        .event-details strong { color: #333; }
        .back-link { display: inline-block; margin-bottom: 20px; color: #8a2be2; text-decoration: none; font-weight: bold; }
        .back-link:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <header>
        <div class="logo">PetFestHub - Admin</div>
        <%@ include file="includes/navbar.jsp" %>
    </header>

    <div class="container">
         <a href="${pageContext.request.contextPath}/AdminDashboardServlet" class="back-link">← Back to Dashboard</a>

         <%-- Display error if event wasn't found or DB error occurred --%>
         <c:if test="${not empty error}">
             <div class="message error"><c:out value="${error}"/></div>
         </c:if>

         <%-- Display Event Details if found --%>
         <c:if test="${not empty eventDetails}">
            <h1>Participants for: <c:out value="${eventDetails.title}"/></h1>
            <div class="event-details">
                 <p><strong>Date:</strong> <fmt:formatDate value="${eventDetails.eventDate}" pattern="yyyy-MM-dd"/></p>
                 <p><strong>Location:</strong> <c:out value="${not empty eventDetails.location ? eventDetails.location : 'N/A'}"/></p>
                 <p><strong>Event ID:</strong> <c:out value="${eventDetails.eventId}"/></p>
            </div>

            <h2>Participant List</h2>
            <table>
                <thead>
                    <tr>
                        <th>User ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Registration Time</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty participants}">
                            <c:forEach var="p" items="${participants}">
                                <tr>
                                    <td><c:out value="${p.userId}"/></td>
                                    <td><c:out value="${p.userName}"/></td>
                                    <td><c:out value="${p.userEmail}"/></td>
                                    <td><fmt:formatDate value="${p.registrationTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="4" class="no-participants">No participants have registered for this event yet.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </c:if> <%-- End check for eventDetails --%>

    </div>
</body>
</html>