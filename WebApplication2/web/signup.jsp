<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Add JSTL core tag library --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <title>Sign Up - PetFestHub</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        /* Your existing styles */
        body { margin: 0; font-family: Arial, sans-serif; background: #f2f4f5; display: flex; flex-direction: column; min-height: 100vh; }
        header { background-color: #8a2be2; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        header .logo { font-size: 20px; font-weight: bold; }
        nav { display: flex; }
        nav a { color: white; text-decoration: none; margin-left: 20px; font-size: 14px; }
        .main-content { flex: 1; display: flex; justify-content: center; align-items: center; padding: 20px; }
        .signup-container { background: white; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); padding: 40px 30px; max-width: 400px; width: 100%; text-align: center; }
        .signup-container h2 { margin-top:0; margin-bottom: 20px; color: #444; } /* Adjusted margin */
        .signup-container input[type="text"],
        .signup-container input[type="email"],
        .signup-container input[type="password"] { box-sizing: border-box; width: 100%; padding: 12px; margin: 10px 0; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
        .signup-container button { background: #8a2be2; color: white; padding: 10px; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; width: 100%; }
        .signup-container button:hover { background: #7a1da3; }
        .login-link { margin-top: 15px; font-size: 14px; }
        .login-link a { color: #8a2be2; text-decoration: none; }

         /* Styles for messages (can be shared in a CSS file later) */
        .message { padding: 10px; margin-bottom: 15px; border-radius: 4px; text-align: center; font-size: 14px; }
        .message.success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .message.error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
    </style>
</head>
<body>

<header>
    <div class="logo">PetFestHub</div>
    <nav>
        <%-- Update links --%>
        <a href="${pageContext.request.contextPath}/home">Home</a>
        <a href="${pageContext.request.contextPath}/ViewEventsServlet">Events</a>
        <a href="gallery.jsp">Gallery</a>
        <a href="create-event.jsp">Create Event</a>
        <a href="login.jsp">Login/Signup</a>
    </nav>
</header>

<div class="main-content">
    <div class="signup-container">
        <h2>Sign Up</h2>

        <%-- **** Use JSTL to display messages **** --%>
        <c:if test="${not empty param.message}">
            <div class="message success">
                <c:out value="${param.message}"/>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="message error">
                <c:out value="${param.error}"/>
            </div>
        </c:if>
        <%-- **** END message display **** --%>

        <%-- Action should match SignUp servlet mapping --%>
        <form action="SignupServlet" method="post">
             <input type="text" name="name" placeholder="Your Name" required>
            <input type="email" name="email" placeholder="Email" required>
            <input type="password" name="password" placeholder="Password" required>
            <button type="submit">Register</button>
        </form>
         <div class="login-link">
            Already have an account? <a href="login.jsp">Log In</a>
        </div>
    </div>
</div>

</body>
</html>