package librarysystem.config;

import jakarta.servlet.http.HttpSession;
import librarysystem.model.User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes "currentUser" available to every Thymeleaf template automatically,
 * so we don't have to add it to the model in every single controller method.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}
