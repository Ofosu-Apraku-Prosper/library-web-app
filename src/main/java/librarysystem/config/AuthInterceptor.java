package librarysystem.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import librarysystem.model.User;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Runs before every page in /dashboard, /books, /my-loans, and /admin/**.
 * No logged-in user in the session -> bounce to /login.
 * Logged in but not an admin, trying to reach /admin/** -> bounce to /dashboard.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        if (request.getRequestURI().startsWith("/admin") && !currentUser.isAdmin()) {
            response.sendRedirect("/dashboard");
            return false;
        }

        return true;
    }
}
