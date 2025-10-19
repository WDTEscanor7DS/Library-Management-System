package libms;
import java.sql.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.Base64;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LibMS { public static final String DB_URL = "jdbc:sqlite:database.db"; 

    public Connection connect() { 
        try { 
            Connection conn = DriverManager.getConnection(DB_URL); 
        if (conn != null) { 
            System.out.println("Connection successful!");
        } return conn; 
    } catch (SQLException e) { 
        System.out.println("Connection failed: " + e.getMessage()); 
        return null; 
    } 
}
    
    public void refreshTable(JTable table){
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Category");
        model.addColumn("Quantity");
        model.addColumn("Status");
        String query = "SELECT * FROM Library";
        try(Connection conn = connect();
            Statement stmt = conn.createStatement()){
            ResultSet rs = stmt.executeQuery(query);
            
            while(rs.next()){
                int id = rs.getInt("ID");
                String title = rs.getString("BookName");
                String author = rs.getString("Author");
                String category = rs.getString("Category");
                int qty = rs.getInt("Amount");
                String status = rs.getString("Status");
                
                model.addRow(new Object[]{id,title,author,category,qty,status});
            }
            table.setModel(model);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
public boolean verifyLogin(String Username, String Password) {
    String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
    Connection conn = connect();
    if (conn == null) {
        System.out.println("Database connection failed. Cannot verify login.");
        return false;
    }

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, Username);
        pstmt.setString(2, Password);
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    } catch (SQLException e) {
        System.out.println("Login query failed: " + e.getMessage());
        return false;
    }
}
    
    public boolean addBook(String bookName, String author, String category, int amount,String status){
        String query = "INSERT INTO Library(BookName, Author, Category, Amount, Status) VALUES (?, ?, ?, ?, ?)";
        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query);){
                pstmt.setString(1, bookName);
                pstmt.setString(2, author);
                pstmt.setString(3, category);
                pstmt.setInt(4, amount);
                pstmt.setString(5, status);
                pstmt.executeUpdate();
                
                return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
public boolean updateBook(int id, String bookName, String author, String category, int amount, String status) {
    String query = "UPDATE Library SET BookName = ?, Author = ?, Category = ?, Amount = ?, Status = ? WHERE ID = ?";
    
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, bookName);
        pstmt.setString(2, author);
        pstmt.setString(3, category);
        pstmt.setInt(4, amount);
        pstmt.setString(5, status);
        pstmt.setInt(6, id);

        int updated = pstmt.executeUpdate();
        return updated > 0;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

public void sortTable(JTable table, String sortOption) {
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("ID");
    model.addColumn("Title");
    model.addColumn("Author");
    model.addColumn("Category");
    model.addColumn("Quantity");
    model.addColumn("Status");

    String query = "SELECT * FROM Library";

    switch (sortOption) {
        case "ID":
            query += " ORDER BY ID ASC";
            break;
        case "ASC":
            query += " ORDER BY BookName ASC";
            break;
        case "DECS":
            query += " ORDER BY BookName DESC";
            break;
        case "AMNT":
            query += " ORDER BY Amount DESC";
            break;
        default:
            break;
    }

    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            int id = rs.getInt("ID");
            String title = rs.getString("BookName");
            String author = rs.getString("Author");
            String category = rs.getString("Category");
            int qty = rs.getInt("Amount");
            String status = rs.getString("Status");
            model.addRow(new Object[]{id, title, author, category, qty, status});
        }
        table.setModel(model);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public void searchBooks(JTable table, String keyword) {
    String sql = "SELECT * FROM Library WHERE " +
                 "CAST(ID AS TEXT) LIKE ? OR " +
                 "BookName LIKE ? OR " +
                 "Author LIKE ? OR " +
                 "Category LIKE ?";
    
    DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Book Name", "Author", "Category", "Amount", "Status"}, 0
    );

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        String likeKeyword = "%" + keyword + "%";
        pstmt.setString(1, likeKeyword);
        pstmt.setString(2, likeKeyword);
        pstmt.setString(3, likeKeyword);
        pstmt.setString(4, likeKeyword);

        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("ID"),
                rs.getString("BookName"),
                rs.getString("Author"),
                rs.getString("Category"),
                rs.getInt("Amount"),
                rs.getString("Status")
            });
        }

        table.setModel(model);

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public void searchAndSortBooks(JTable table, String keyword, String sortOption) {
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("ID");
    model.addColumn("Title");
    model.addColumn("Author");
    model.addColumn("Category");
    model.addColumn("Quantity");
    model.addColumn("Status");

    String orderBy;
    switch (sortOption) {
        case "ASC":
            orderBy = "ORDER BY BookName ASC";
            break;
        case "DECS":
            orderBy = "ORDER BY BookName DESC";
            break;
        case "AMNT":
            orderBy = "ORDER BY Amount DESC";
            break;
        case "ID":
        default:
            orderBy = "ORDER BY ID ASC";
            break;
    }

    String query = "SELECT * FROM Library WHERE "
                 + "BookName LIKE ? OR Author LIKE ? OR Category LIKE ? "
                 + orderBy;

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        String searchPattern = "%" + keyword + "%";
        pstmt.setString(1, searchPattern);
        pstmt.setString(2, searchPattern);
        pstmt.setString(3, searchPattern);

        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("ID");
            String title = rs.getString("BookName");
            String author = rs.getString("Author");
            String category = rs.getString("Category");
            int qty = rs.getInt("Amount");
            String status = rs.getString("Status");
            model.addRow(new Object[]{id, title, author, category, qty, status});
        }

        table.setModel(model);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

        public void fetchAll(JTable table){
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        model.setRowCount(0);
        model.addColumn("ID");
        model.addColumn("BookName");
        model.addColumn("Author");
        model.addColumn("Category");
        model.addColumn("Amount");
        model.addColumn("Status");
        
        
        } 
        
        public ResultSet getAllBooks() {
            Connection conn = connect();
        try {
               String sql = "Select * FROM Library";
               PreparedStatement ps = conn.prepareStatement(sql);
               
               return ps.executeQuery();
               
            }catch (Exception e) {
                e.printStackTrace();
                
                return null;
            }
        }
        public void fetchAllBooks(JTable table){
            String sql = "Select * FROM Library";
            String[] columnNames = {"ID", "Book Name" , "Author", "Category" , "Amount", "Status"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            try{
                Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()){
                    int id = rs.getInt("ID");
                    String bookName = rs.getString("BookName");
                    String author = rs.getString("Author");
                    String category = rs.getString("Category");
                    int amount = rs.getInt("Amount");
                    String status = rs.getString("Status");
                    
                    Object[] row = {id,bookName,author,category,amount,status};
                    model.addRow(row);
                }
                table.setModel(model);
            }catch(SQLException e){
            System.out.println("Connection failed: " + e.getMessage());
            }  
        }
    }

