package librarysystem.dao;

import librarysystem.model.Loan;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LoanDAO {

    public static final int LOAN_PERIOD_DAYS = 14;
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("0.50");

    private final DataSource dataSource;

    public LoanDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void borrowBook(int bookId, int userId) throws SQLException {
        String checkSql = "SELECT available_copies FROM books WHERE id = ? FOR UPDATE";
        String insertLoanSql = "INSERT INTO loans (book_id, user_id, borrow_date, due_date, status) " +
                                "VALUES (?, ?, ?, ?, 'BORROWED')";
        String decrementSql = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ?";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int availableCopies;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, bookId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Book not found.");
                        availableCopies = rs.getInt("available_copies");
                    }
                }

                if (availableCopies <= 0) {
                    throw new SQLException("No copies of this book are currently available.");
                }

                LocalDate today = LocalDate.now();
                LocalDate dueDate = today.plusDays(LOAN_PERIOD_DAYS);

                try (PreparedStatement insertStmt = conn.prepareStatement(insertLoanSql)) {
                    insertStmt.setInt(1, bookId);
                    insertStmt.setInt(2, userId);
                    insertStmt.setDate(3, Date.valueOf(today));
                    insertStmt.setDate(4, Date.valueOf(dueDate));
                    insertStmt.executeUpdate();
                }

                try (PreparedStatement decStmt = conn.prepareStatement(decrementSql)) {
                    decStmt.setInt(1, bookId);
                    decStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public BigDecimal returnBook(int loanId) throws SQLException {
        String getLoanSql = "SELECT book_id, due_date, status FROM loans WHERE id = ? FOR UPDATE";
        String updateLoanSql = "UPDATE loans SET return_date = ?, fine = ?, status = 'RETURNED' WHERE id = ?";
        String incrementSql = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookId;
                LocalDate dueDate;
                String status;

                try (PreparedStatement getStmt = conn.prepareStatement(getLoanSql)) {
                    getStmt.setInt(1, loanId);
                    try (ResultSet rs = getStmt.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Loan record not found.");
                        bookId = rs.getInt("book_id");
                        dueDate = rs.getDate("due_date").toLocalDate();
                        status = rs.getString("status");
                    }
                }

                if ("RETURNED".equals(status)) {
                    throw new SQLException("This book has already been returned.");
                }

                LocalDate today = LocalDate.now();
                BigDecimal fine = calculateFine(dueDate, today);

                try (PreparedStatement updateStmt = conn.prepareStatement(updateLoanSql)) {
                    updateStmt.setDate(1, Date.valueOf(today));
                    updateStmt.setBigDecimal(2, fine);
                    updateStmt.setInt(3, loanId);
                    updateStmt.executeUpdate();
                }

                try (PreparedStatement incStmt = conn.prepareStatement(incrementSql)) {
                    incStmt.setInt(1, bookId);
                    incStmt.executeUpdate();
                }

                conn.commit();
                return fine;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static BigDecimal calculateFine(LocalDate dueDate, LocalDate returnOrTodayDate) {
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnOrTodayDate);
        if (daysLate <= 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return FINE_PER_DAY.multiply(BigDecimal.valueOf(daysLate)).setScale(2, RoundingMode.HALF_UP);
    }

    public List<Loan> getActiveLoansForUser(int userId) throws SQLException {
        String sql = "SELECT l.*, b.title AS book_title, u.username, u.full_name " +
                     "FROM loans l JOIN books b ON l.book_id = b.id JOIN users u ON l.user_id = u.id " +
                     "WHERE l.user_id = ? AND l.status = 'BORROWED' ORDER BY l.due_date";
        return runLoanQuery(sql, userId, true);
    }

    public List<Loan> getLoanHistoryForUser(int userId) throws SQLException {
        String sql = "SELECT l.*, b.title AS book_title, u.username, u.full_name " +
                     "FROM loans l JOIN books b ON l.book_id = b.id JOIN users u ON l.user_id = u.id " +
                     "WHERE l.user_id = ? ORDER BY l.borrow_date DESC";
        return runLoanQuery(sql, userId, true);
    }

    public List<Loan> getAllActiveLoans() throws SQLException {
        String sql = "SELECT l.*, b.title AS book_title, u.username, u.full_name " +
                     "FROM loans l JOIN books b ON l.book_id = b.id JOIN users u ON l.user_id = u.id " +
                     "WHERE l.status = 'BORROWED' ORDER BY l.due_date";
        return runLoanQuery(sql, 0, false);
    }

    public int countActiveLoans() throws SQLException {
        return countWhere("status = 'BORROWED'");
    }

    public int countOverdueLoans() throws SQLException {
        return countWhere("status = 'BORROWED' AND due_date < CURDATE()");
    }

    public BigDecimal getTotalFinesCollected() throws SQLException {
        String sql = "SELECT COALESCE(SUM(fine), 0) AS total FROM loans WHERE status = 'RETURNED'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalFinesPaidByUser(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(fine), 0) AS total FROM loans WHERE user_id = ? AND status = 'RETURNED'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            }
        }
    }

    private int countWhere(String whereClause) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM loans WHERE " + whereClause;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private List<Loan> runLoanQuery(String sql, int userId, boolean filterByUser) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (filterByUser) stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date returnDateSql = rs.getDate("return_date");
                    loans.add(new Loan(
                            rs.getInt("id"),
                            rs.getInt("book_id"),
                            rs.getString("book_title"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getDate("borrow_date").toLocalDate(),
                            rs.getDate("due_date").toLocalDate(),
                            returnDateSql == null ? null : returnDateSql.toLocalDate(),
                            rs.getBigDecimal("fine"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return loans;
    }
}
