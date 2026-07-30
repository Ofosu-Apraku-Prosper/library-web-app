package librarysystem.controller;

import librarysystem.dao.BookDAO;
import librarysystem.dao.LoanDAO;
import librarysystem.dao.UserDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookDAO bookDAO;
    private final LoanDAO loanDAO;
    private final UserDAO userDAO;

    public AdminController(BookDAO bookDAO, LoanDAO loanDAO, UserDAO userDAO) {
        this.bookDAO = bookDAO;
        this.loanDAO = loanDAO;
        this.userDAO = userDAO;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            model.addAttribute("titleCount", bookDAO.countTitles());
            model.addAttribute("totalCopies", bookDAO.countTotalCopies());
            model.addAttribute("userCount", userDAO.countUsers());
            model.addAttribute("activeLoanCount", loanDAO.countActiveLoans());
            model.addAttribute("overdueCount", loanDAO.countOverdueLoans());
            model.addAttribute("finesCollected", loanDAO.getTotalFinesCollected());
            model.addAttribute("activeLoans", loanDAO.getAllActiveLoans());
        } catch (SQLException ex) {
            model.addAttribute("error", "Database error: " + ex.getMessage());
        }
        return "admin/dashboard";
    }

    @GetMapping("/inventory")
    public String inventory(@RequestParam(required = false) String q, Model model) {
        try {
            model.addAttribute("books", (q == null || q.isBlank())
                    ? bookDAO.getAllBooks() : bookDAO.searchByTitle(q));
        } catch (SQLException ex) {
            model.addAttribute("error", "Database error: " + ex.getMessage());
        }
        return "admin/inventory";
    }

    @PostMapping("/inventory/add")
    public String addBook(@RequestParam String title, @RequestParam String author,
                           @RequestParam(required = false) String isbn, @RequestParam int copies,
                           RedirectAttributes redirectAttributes) {
        try {
            bookDAO.addBook(title, author, isbn, copies);
            redirectAttributes.addFlashAttribute("message", "\"" + title + "\" added to the catalog.");
        } catch (SQLException ex) {
            redirectAttributes.addFlashAttribute("error", "Database error: " + ex.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    @PostMapping("/inventory/delete")
    public String deleteBook(@RequestParam int bookId, RedirectAttributes redirectAttributes) {
        try {
            bookDAO.deleteBook(bookId);
            redirectAttributes.addFlashAttribute("message", "Book deleted.");
        } catch (SQLException ex) {
            redirectAttributes.addFlashAttribute("error",
                    "Could not delete — it likely has loan history. " + ex.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    @GetMapping("/users")
    public String users(Model model) {
        try {
            model.addAttribute("users", userDAO.getAllUsers());
        } catch (SQLException ex) {
            model.addAttribute("error", "Database error: " + ex.getMessage());
        }
        return "admin/users";
    }

    @PostMapping("/users/role")
    public String updateRole(@RequestParam int userId, @RequestParam String role,
                              RedirectAttributes redirectAttributes) {
        try {
            userDAO.updateUserRole(userId, role);
            redirectAttributes.addFlashAttribute("message", "Role updated.");
        } catch (SQLException ex) {
            redirectAttributes.addFlashAttribute("error", "Database error: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
