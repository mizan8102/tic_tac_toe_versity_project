Database Setup 

CREATE DATABASE tictactoe;
USE tictactoe;

CREATE TABLE game_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player1_name VARCHAR(50),
    player2_name VARCHAR(50),
    winner VARCHAR(50),
    game_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);