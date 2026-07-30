package librarysystem.dao;

import librarysystem.model.Book;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BookDAO {

    private final DataSource dataSource;

    public BookDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Book> getAllBooks() throws SQLException {
        String sql = "SELECT * FROM books ORDER BY id";
        return runQuery(sql, null);
    }

    public List<Book> searchByTitle(String keyword) throws SQLException {
        String sql = "SELECT * FROM books WHERE title LIKE ? ORDER BY id";
        return runQuery(sql, "%" + keyword + "%");
    }

    public List<Book> getAvailableBooks() throws SQLException {
        String sql = "SELECT * FROM books WHERE available_copies > 0 ORDER BY title";
        return runQuery(sql, null);
    }

    private List<Book> runQuery(String sql, String param) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (param != null) stmt.setString(1, param);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        }
        return books;
    }

    public void addBook(String title, String author, String isbn, int totalCopies) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, total_copies, available_copies) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, isbn);
            stmt.setInt(4, totalCopies);
            stmt.setInt(5, totalCopies);
            stmt.executeUpdate();
        }
    }

    public void deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public int countTitles() throws SQLException {
        return countFrom("SELECT COUNT(*) AS total FROM books");
    }

    public int countTotalCopies() throws SQLException {
        return countFrom("SELECT COALESCE(SUM(total_copies), 0) AS total FROM books");
    }

    private int countFrom(String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        );
    }
}
