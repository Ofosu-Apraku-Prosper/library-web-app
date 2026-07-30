package librarysystem.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Loan {
    private int id;
    private int bookId;
    private String bookTitle;
    private int userId;
    private String username;
    private String fullName;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal fine;
    private String status;

    public Loan(int id, int bookId, String bookTitle, int userId, String username, String fullName,
                LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate,
                BigDecimal fine, String status) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fine = fine;
        this.status = status;
    }

    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public BigDecimal getFine() { return fine; }
    public String getStatus() { return status; }
    public boolean isOverdue() { return returnDate == null && dueDate.isBefore(LocalDate.now()); }
}
