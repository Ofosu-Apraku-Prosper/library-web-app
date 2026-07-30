package librarysystem.controller;

import jakarta.servlet.http.HttpSession;
import librarysystem.dao.UserDAO;
import librarysystem.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@Controller
public class AuthController {

    private final UserDAO userDAO;

    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        return session.getAttribute("currentUser") != null ? "redirect:/dashboard" : "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                         HttpSession session, Model model) {
        try {
            User user = userDAO.validateLogin(username, password);
            if (user == null) {
                model.addAttribute("error", "Invalid username or password.");
                return "login";
            }
            session.setAttribute("currentUser", user);
            return "redirect:/dashboard";
        } catch (SQLException ex) {
            model.addAttribute("error", "Database error: " + ex.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName, @RequestParam String username,
                            @RequestParam String email, @RequestParam String password,
                            @RequestParam String confirmPassword, Model model) {

        if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            model.addAttribute("error", "All fields are required.");
            return "register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }

        try {
            if (userDAO.usernameExists(username)) {
                model.addAttribute("error", "That username is already taken.");
                return "register";
            }
            userDAO.registerUser(fullName, username, email, password);
            return "redirect:/login?registered";
        } catch (SQLException ex) {
            model.addAttribute("error", "Database error: " + ex.getMessage());
            return "register";
        }
    }
}
