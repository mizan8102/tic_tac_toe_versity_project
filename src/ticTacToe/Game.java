package ticTacToe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;
import ticTacToe.DatabaseConnection;

public class Game extends JFrame {
    private JButton[][] buttons = new JButton[3][3];
    private boolean isXTurn = true;
    private JLabel statusLabel;
    private int movesCount = 0;
    private static final String PLAYER_X = "X";
    private static final String PLAYER_O = "O";
    private String player1Name;
    private String player2Name;
    private DatabaseConnection dbConnection;

    public Game() {
        dbConnection = DatabaseConnection.getInstance();
        
        // Get player names
        player1Name = JOptionPane.showInputDialog(this, "Enter Player 1 (X) name:", "Player Registration", JOptionPane.QUESTION_MESSAGE);
        player2Name = JOptionPane.showInputDialog(this, "Enter Player 2 (O) name:", "Player Registration", JOptionPane.QUESTION_MESSAGE);
        
        if (player1Name == null || player2Name == null || 
            player1Name.trim().isEmpty() || player2Name.trim().isEmpty()) {
            System.exit(0);
        }

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Tic Tac Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Initialize main panel with GridLayout and Button
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Game board panel
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        boardPanel.setBackground(new Color(220, 220, 220));

        // Initialize buttons
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = createGameButton();
                boardPanel.add(buttons[i][j]);
            }
        }

        // Status label
        statusLabel = new JLabel(player1Name + "'s turn (X)", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton resetButton = new JButton("New Game");
        JButton historyButton = new JButton("View History");
        
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));
        historyButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        resetButton.addActionListener(e -> resetGame());
        historyButton.addActionListener(e -> showGameHistory());

        buttonPanel.add(resetButton);
        buttonPanel.add(historyButton);

        // Add components to main panel
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void showGameHistory() {
        JDialog historyDialog = new JDialog(this, "Game History", true);
        historyDialog.setLayout(new BorderLayout());

        Vector<String> columnNames = new Vector<>();
        columnNames.add("Player 1");
        columnNames.add("Player 2");
        columnNames.add("Winner");
        columnNames.add("Date");

        Vector<Vector<String>> data = new Vector<>();

        try {
            ResultSet rs = dbConnection.getGameHistory();
            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("player1_name"));
                row.add(rs.getString("player2_name"));
                row.add(rs.getString("winner"));
                row.add(rs.getTimestamp("game_date").toString());
                data.add(row);
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading game history: " + e.getMessage());
        }

        JTable historyTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        historyDialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> historyDialog.dispose());
        historyDialog.add(closeButton, BorderLayout.SOUTH);

        historyDialog.setSize(600, 400);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
    }

    private void handleWin(String symbol) {
        String winner = symbol.equals(PLAYER_X) ? player1Name : player2Name;
        statusLabel.setText(winner + " wins!");
        dbConnection.saveGame(player1Name, player2Name, winner);
        disableAllButtons();
    }

    private void handleDraw() {
        statusLabel.setText("It's a draw!");
        dbConnection.saveGame(player1Name, player2Name, "Draw");
    }

    private void disableAllButtons() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private boolean checkWin() {
        String symbol;
        // Check rows (horizontal)
        for (int i = 0; i < 3; i++) {
            if (!buttons[i][0].getText().isEmpty() &&
                buttons[i][0].getText().equals(buttons[i][1].getText()) &&
                buttons[i][0].getText().equals(buttons[i][2].getText())) {
                // Highlight winning row
                buttons[i][0].setBackground(Color.GREEN);
                buttons[i][1].setBackground(Color.GREEN);
                buttons[i][2].setBackground(Color.GREEN);
                return true;
            }
        }

        // Check columns (vertical)
        for (int j = 0; j < 3; j++) {
            if (!buttons[0][j].getText().isEmpty() &&
                buttons[0][j].getText().equals(buttons[1][j].getText()) &&
                buttons[0][j].getText().equals(buttons[2][j].getText())) {
                // Highlight winning column
                buttons[0][j].setBackground(Color.GREEN);
                buttons[1][j].setBackground(Color.GREEN);
                buttons[2][j].setBackground(Color.GREEN);
                return true;
            }
        }

        // Check diagonal (top-left to bottom-right)
        if (!buttons[0][0].getText().isEmpty() &&
            buttons[0][0].getText().equals(buttons[1][1].getText()) &&
            buttons[0][0].getText().equals(buttons[2][2].getText())) {
            // Highlight winning diagonal
            buttons[0][0].setBackground(Color.GREEN);
            buttons[1][1].setBackground(Color.GREEN);
            buttons[2][2].setBackground(Color.GREEN);
            return true;
        }

        // Check diagonal (top-right to bottom-left)
        if (!buttons[0][2].getText().isEmpty() &&
            buttons[0][2].getText().equals(buttons[1][1].getText()) &&
            buttons[0][2].getText().equals(buttons[2][0].getText())) {
            // Highlight winning diagonal
            buttons[0][2].setBackground(Color.GREEN);
            buttons[1][1].setBackground(Color.GREEN);
            buttons[2][0].setBackground(Color.GREEN);
            return true;
        }

        return false;
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
                buttons[i][j].setBackground(null);
            }
        }
        isXTurn = true;
        movesCount = 0;
        statusLabel.setText(player1Name + "'s turn (X)");
    }

    private JButton createGameButton() {
        JButton button = new JButton();
        button.setFont(new Font("Arial", Font.BOLD, 40));
        button.setForeground(Color.BLACK);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        button.setPreferredSize(new Dimension(100, 100));
        button.setMinimumSize(new Dimension(100, 100));
        button.setMaximumSize(new Dimension(100, 100));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton clickedButton = (JButton) e.getSource();
                if (clickedButton.getText().isEmpty()) {
                    String symbol = isXTurn ? PLAYER_X : PLAYER_O;
                    clickedButton.setText(symbol);
                    clickedButton.setForeground(isXTurn ? Color.BLUE : Color.RED);
                    movesCount++;

                    if (checkWin()) {
                        handleWin(symbol);
                    } else if (movesCount == 9) {
                        handleDraw();
                    } else {
                        isXTurn = !isXTurn;
                        String currentPlayer = isXTurn ? player1Name : player2Name;
                        symbol = isXTurn ? PLAYER_X : PLAYER_O;
                        statusLabel.setText(currentPlayer + "'s turn (" + symbol + ")");
                    }
                }
            }
        });
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Game().setVisible(true);
        });
    }
} 