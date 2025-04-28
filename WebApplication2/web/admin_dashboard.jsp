<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    // Basic Authorization Check
    Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
    if (isAdmin == null || !isAdmin) {
        response.sendRedirect("login.jsp?adminError=Admin access required.");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard - PetFestHub</title>
    <style>
        body { font-family: sans-serif; margin: 0; background-color: #f4f4f4; }
        .container { padding: 20px; max-width: 1200px; margin: 20px auto; background-color: #fff; box-shadow: 0 0 10px rgba(0,0,0,0.1); border-radius: 8px;}
        h1 { color: #333; border-bottom: 2px solid #8a2be2; padding-bottom: 10px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 10px 12px; text-align: left; font-size: 14px; vertical-align: middle; /* Align content vertically */ }
        th { background-color: #8a2be2; color: white; font-weight: bold; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        td form { margin: 0; display: inline-block; }
        .approve-button { background-color: #28a745; color: white; padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
        .approve-button:hover { background-color: #218838; }
        .message { padding: 10px; margin-bottom: 15px; border-radius: 4px; }
        .message.success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .message.error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .no-requests { color: #555; font-style: italic; text-align: center; padding: 20px; }
        header { background-color: #8a2be2; color: white; padding: 15px 30px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
        header .logo { font-size: 20px; font-weight: bold; }
        .logout-link { color: white; text-decoration: none; font-size: 14px; padding: 5px 10px; background-color: #dc3545; border-radius: 4px; }
        .logout-link:hover { background-color: #c82333; }
        /* Style for image thumbnail */
        .request-image-thumbnail {
            max-width: 100px;
            max-height: 75px;
            height: auto;
            width: auto;
            border: 1px solid #eee;
            vertical-align: middle; /* Helps alignment in cell */
            display: block; /* Prevents extra space below */
            margin: auto; /* Center if needed */
        }
    </style>
</head>
<body>
    <header>
        <div class="logo">PetFestHub - Admin Dashboard</div>
        <%-- <a href="AdminLogoutServlet" class="logout-link">Logout</a> --%>
    </header>

    <div class="container">
        <h1>Pending Event Requests</h1>

        <!-- Display feedback messages -->
        <c:if test="${not empty param.message}"><div class="message success"><c:out value="${param.message}"/></div></c:if>
        <c:if test="${not empty param.error}"><div class="message error"><c:out value="${param.error}"/></div></c:if>
        <c:if test="${not empty requestScope.dashboardError}"><div class="message error"><c:out value="${requestScope.dashboardError}"/></div></c:if>

        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Event Name</th>
                    <th>Location</th>
                    <th>Req. Date</th>
                    <th>Description</th>
                    <th>Image</th> <%-- **** NEW Column Header **** --%>
                    <th>Requested At</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty pendingRequests}">
                        <c:forEach var="req" items="${pendingRequests}">
                            <tr>
                                <td><c:out value="${req.requestId}"/></td>
                                <td><c:out value="${req.eventName}"/></td>
                                <td><c:out value="${req.location}"/></td>
                                <td><fmt:formatDate value="${req.requestedDate}" pattern="yyyy-MM-dd"/></td>
                                <td><c:out value="${req.description}"/></td>

                                <%-- **** NEW: Image display cell **** --%>
                                <td style="text-align: center;"> <%-- Center content in this cell --%>
                                    <c:if test="${not empty req.imageFilename}">
                                        <%-- Link to full image in new tab, display thumbnail --%>
                                        <a href="${pageContext.request.contextPath}/ImageServlet/${req.imageFilename}" target="_blank">
                                          <img src="${pageContext.request.contextPath}/ImageServlet/${req.imageFilename}"
                                               alt="Requested Event Image"
                                               class="request-image-thumbnail">
                                        </a>
                                    </c:if>
                                    <%-- Display text if no image was submitted with the request --%>
                                    <c:if test="${empty req.imageFilename}">
                                        No Image
                                    </c:if>
                                </td>
                                <%-- **** END NEW **** --%>

                                <td><fmt:formatDate value="${req.requestedAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                <td>
                                    <form action="ApproveEventServlet" method="post">
                                        <input type="hidden" name="requestId" value="${req.requestId}">
                                        <button type="submit" class="approve-button">Approve</button>
                                    </form>
                                    <%-- Consider adding a Reject button/form here too --%>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <%-- **** MODIFIED: Correct colspan **** --%>
                        <tr><td colspan="8" class="no-requests">No pending event requests found.</td></tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</body>
</html>