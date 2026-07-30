package librarysystem.controller;

import jakarta.servlet.http.HttpSession;
import librarysystem.dao.LoanDAO;
import librarysystem.model.Loan;
import librarysystem.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoanController {

    private final LoanDAO loanDAO;

    public LoanController(LoanDAO loanDAO) {
        this.loanDAO = loanDAO;
    }

    @GetMapping("/my-loans")
    public String myLoans(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        try {
            List<Loan> activeLoans = loanDAO.getActiveLoansForUser(currentUser.getId());

            // Pre-compute today's estimated fine for each active loan (loanId -> fine),
            // since Thymeleaf can't easily call a static Java method with two args.
            Map<Integer, BigDecimal> estimatedFines = new HashMap<>();
            for (Loan loan : activeLoans) {
                estimatedFines.put(loan.getId(), LoanDAO.calculateFine(loan.getDueDate(), LocalDate.now()));
            }

            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("estimatedFines", estimatedFines);
            model.addAttribute("history", loanDAO.getLoanHistoryForUser(currentUser.getId()));
        } catch (SQLException ex) {
            model.addAttribute("error", "Database error: " + ex.getMessage());
        }
        return "my-loans";
    }

    @PostMapping("/loans/return")
    public String returnLoan(@RequestParam int loanId, @RequestParam(defaultValue = "/my-loans") String redirectTo,
                              RedirectAttributes redirectAttributes) {
        try {
            BigDecimal fine = loanDAO.returnBook(loanId);
            String message = fine.compareTo(BigDecimal.ZERO) > 0
                    ? "Book returned. Fine charged: $" + fine
                    : "Book returned on time. No fine.";
            redirectAttributes.addFlashAttribute("message", message);
        } catch (SQLException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + redirectTo;
    }
}
