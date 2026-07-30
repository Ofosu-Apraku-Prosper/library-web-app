package librarysystem.controller;

import jakarta.servlet.http.HttpSession;
import librarysystem.dao.BookDAO;
import librarysystem.dao.LoanDAO;
import librarysystem.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;

@Controller
public class BookController {

    private final BookDAO bookDAO;
    private final LoanDAO loanDAO;

    public BookController(BookDAO bookDAO, LoanDAO loanDAO) {
        this.bookDAO = bookDAO;
        this.loanDAO = loanDAO;
    }

    @GetMapping("/books")
    public String browseBooks(Model model) {
        try {
            model.addAttribute("books", bookDAO.getAvailableBooks());
        } catch (SQLException ex) {
            model.addAttribute("error", "Database error: " + ex.getMessage());
        }
        return "books";
    }

    @PostMapping("/books/borrow")
    public String borrowBook(@RequestParam int bookId, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        try {
            loanDAO.borrowBook(bookId, currentUser.getId());
            redirectAttributes.addFlashAttribute("message", "Book borrowed! Due back in 14 days.");
        } catch (SQLException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/books";
    }
}
