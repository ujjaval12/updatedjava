<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
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
    <title>Admin Dashboard - PetFestHub</title>
    <style>
        /* Existing styles */
        body { font-family: sans-serif; margin: 0; background-color: #f4f4f4; }
        .container { padding: 20px; max-width: 1400px; margin: 20px auto; background-color: #fff; box-shadow: 0 0 10px rgba(0,0,0,0.1); border-radius: 8px;}
        h1, h2 { color: #333; border-bottom: 2px solid #8a2be2; padding-bottom: 10px; margin-top: 30px; }
        h1 { margin-top: 0; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; margin-bottom: 40px; }
        th, td { border: 1px solid #ddd; padding: 10px 12px; text-align: left; font-size: 14px; vertical-align: middle; }
        th { background-color: #8a2be2; color: white; font-weight: bold; white-space: nowrap; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        td form { margin: 0 5px 0 0; display: inline-block; }
        .action-button { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; color: white; text-decoration: none; display: inline-block; margin-right: 5px; }
        .approve-button { background-color: #28a745; }
        .approve-button:hover { background-color: #218838; }
        .decline-button { background-color: #dc3545; }
        .decline-button:hover { background-color: #c82333; }
        .view-participants-button { background-color: #007bff; }
        .view-participants-button:hover { background-color: #0056b3; }
        /* **** NEW: Delete Button Style **** */
        .delete-button { background-color: #ffc107; color: #212529; /* Dark text on yellow */}
        .delete-button:hover { background-color: #e0a800; }
        /* **** END NEW **** */
        .message { padding: 10px; margin-bottom: 15px; border-radius: 4px; }
        .message.success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .message.error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .no-requests, .no-events { color: #555; font-style: italic; text-align: center; padding: 20px; }
        header { background-color: #8a2be2; color: white; padding: 15px 30px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
        header .logo { font-size: 20px; font-weight: bold; }
        nav a { color: white; text-decoration: none; margin-left: 20px; font-size: 14px; }
        nav a.logout-link { padding: 5px 10px; background-color: #dc3545; border-radius: 4px; }
        nav a.logout-link:hover { background-color: #c82333; }
        .request-image-thumbnail { max-width: 100px; max-height: 75px; height: auto; width: auto; border: 1px solid #eee; vertical-align: middle; display: block; margin: auto; }
    </style>
    <%-- **** NEW: Simple Javascript confirmation **** --%>
    <script>
        function confirmDelete(eventId) {
            return confirm('Are you sure you want to delete event ID ' + eventId + '? This action cannot be undone.');
        }
    </script>
    <%-- **** END NEW **** --%>
</head>
<body>
    <header>
        <div class="logo">PetFestHub - Admin Dashboard</div>
        <%@ include file="includes/navbar.jsp" %>
    </header>

    <div class="container">
        <c:if test="${not empty param.message}"><div class="message success"><c:out value="${param.message}"/></div></c:if>
        <c:if test="${not empty param.error}"><div class="message error"><c:out value="${param.error}"/></div></c:if>
        <c:if test="${not empty requestScope.dashboardError}"><div class="message error"><c:out value="${requestScope.dashboardError}"/></div></c:if>

        <h2>Pending Event Requests</h2>
        <table>
             <thead><tr><th>ID</th><th>Event Name</th><th>Location</th><th>Req. Date</th><th>Description</th><th>Image</th><th>Requester Name</th><th>Requester Email</th><th>Requested At</th><th>Action</th></tr></thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty pendingRequests}">
                        <c:forEach var="req" items="${pendingRequests}"><!-- ... request rows ... --></c:forEach>
                         <%-- Existing loop content for requests --%>
                         <c:forEach var="req" items="${pendingRequests}">
                            <tr>
                                <td><c:out value="${req.requestId}"/></td>
                                <td><c:out value="${req.eventName}"/></td>
                                <td><c:out value="${req.location}"/></td>
                                <td><fmt:formatDate value="${req.requestedDate}" pattern="yyyy-MM-dd"/></td>
                                <td><c:out value="${req.description}"/></td>
                                <td style="text-align: center;">
                                    <c:if test="${not empty req.imageFilename}"><a href="${pageContext.request.contextPath}/ImageServlet/${req.imageFilename}" target="_blank"><img src="${pageContext.request.contextPath}/ImageServlet/${req.imageFilename}" alt="Req Img" class="request-image-thumbnail"></a></c:if>
                                    <c:if test="${empty req.imageFilename}">No Image</c:if>
                                </td>
                                <td><c:out value="${not empty req.requesterName ? req.requesterName : 'User Deleted'}"/></td>
                                <td><c:out value="${not empty req.requesterEmail ? req.requesterEmail : 'N/A'}"/></td>
                                <td><fmt:formatDate value="${req.requestedAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                <td>
                                    <form action="ApproveEventServlet" method="post" style="display:inline-block;"><input type="hidden" name="requestId" value="${req.requestId}"><button type="submit" class="action-button approve-button">Approve</button></form>
                                    <form action="DeclineRequestServlet" method="post" style="display:inline-block;"><input type="hidden" name="requestId" value="${req.requestId}"><button type="submit" class="action-button decline-button">Decline</button></form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise><tr><td colspan="10" class="no-requests">No pending event requests found.</td></tr></c:otherwise>
                </c:choose>
            </tbody>
        </table>


        <h2>Approved Events Management</h2>
        <table>
             <thead>
                 <tr>
                     <th>Event ID</th>
                     <th>Title</th>
                     <th>Date</th>
                     <th>Location</th>
                     <th>Participants</th>
                     <th>Actions</th> <%-- **** NEW Column **** --%>
                 </tr>
            </thead>
            <tbody>
                 <c:choose>
                    <c:when test="${not empty approvedEvents}">
                        <c:forEach var="event" items="${approvedEvents}">
                            <tr>
                                <td><c:out value="${event.eventId}"/></td>
                                <td><c:out value="${event.title}"/></td>
                                <td><fmt:formatDate value="${event.eventDate}" pattern="yyyy-MM-dd"/></td>
                                <td><c:out value="${event.location}"/></td>
                                <td>
                                    <a href="ViewParticipantsServlet?eventId=${event.eventId}" class="action-button view-participants-button">View List</a>
                                </td>
                                <%-- **** NEW Actions Cell **** --%>
                                <td>
                                     <%-- Edit button placeholder/link (requires EditEventServlet later) --%>
                                     <%-- <a href="EditEventServlet?eventId=${event.eventId}" class="action-button edit-button">Edit</a> --%>

                                     <%-- Delete button form --%>
                                     <form action="DeleteEventServlet" method="post" style="display:inline-block;" onsubmit="return confirmDelete(${event.eventId});">
                                         <input type="hidden" name="eventId" value="${event.eventId}">
                                         <button type="submit" class="action-button delete-button">Delete</button>
                                     </form>
                                </td>
                                <%-- **** END NEW **** --%>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise><tr><td colspan="6" class="no-events">No events have been approved yet.</td></tr></c:otherwise> <%-- Updated colspan --%>
                </c:choose>
            </tbody>
        </table>

    </div>
</body>
</html>