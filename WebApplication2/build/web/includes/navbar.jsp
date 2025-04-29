<%-- File: Web Pages/includes/navbar.jsp --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<nav>
    <%-- These links are always visible --%>
    <a href="${pageContext.request.contextPath}/home">Home</a>
    <a href="${pageContext.request.contextPath}/ViewEventsServlet">Events</a>
    <a href="${pageContext.request.contextPath}/gallery.jsp">Gallery</a>

    <%-- **** NEW LOGIC **** --%>
    <%-- Show "Create Event" link ONLY if NOT logged in as Admin --%>
    <%-- This means show it if logged out OR logged in as a regular user --%>
    <c:if test="${empty sessionScope.isAdmin}"> <%-- True if isAdmin attribute is null/not set --%>
        <a href="${pageContext.request.contextPath}/create-event.jsp">Create Event</a>
    </c:if>

    <%-- Logic for Admin Dashboard, Logout / Login --%>
    <c:choose>
        <%-- Case: User OR Admin is logged in --%>
        <c:when test="${not empty sessionScope.userId or not empty sessionScope.isAdmin}">
            <%-- Show Admin Dashboard link ONLY if ADMIN is logged in --%>
            <c:if test="${not empty sessionScope.isAdmin}">
                <a href="${pageContext.request.contextPath}/AdminDashboardServlet">Admin Dashboard</a>
            </c:if>
            <%-- Show Logout link (for either user or admin) --%>
            <a href="${pageContext.request.contextPath}/LogoutServlet">Logout (<c:out value="${not empty sessionScope.isAdmin ? sessionScope.adminEmail : sessionScope.userName}"/>)</a>
        </c:when>
        <%-- Case: No one is logged in --%>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/login.jsp">Login/Signup</a>
        </c:otherwise>
    </c:choose>
    <%-- **** END NEW LOGIC **** --%>
</nav>