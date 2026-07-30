package librarysystem.controller;

import jakarta.servlet.http.HttpSession;
import librarysystem.dao.LoanDAO;
import librarysystem.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.SQLException;

@Controller
public class DashboardController {

    private final LoanDAO loanDAO;

    public DashboardController(LoanDAO loanDAO) {
        this.loanDAO = loanDAO;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        try {
            model.addAttribute("totalFinesPaid", loanDAO.getTotalFinesPaidByUser(currentUser.getId()));
        } catch (SQLException ex) {
            model.addAttribute("totalFinesPaid", "N/A");
        }
        return "dashboard";
    }
}
