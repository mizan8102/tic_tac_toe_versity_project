package ticTacToe;
import java.sql.*;
import javax.swing.JOptionPane;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tictactoe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Connection Error: " + e.getMessage());
        }
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void saveGame(String player1Name, String player2Name, String winner) {
        String query = "INSERT INTO game_history (player1_name, player2_name, winner) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player1Name);
            pstmt.setString(2, player2Name);
            pstmt.setString(3, winner);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error saving game result: " + e.getMessage());
        }
    }
    
    public ResultSet getGameHistory() throws SQLException {
        String query = "SELECT * FROM game_history ORDER BY game_date DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }
} 