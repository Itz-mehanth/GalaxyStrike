package com.example.fighterplane.components;


public class GameManager {
    private int score;
    private boolean isGameOver;

    public GameManager() {
        score = 0;
        isGameOver = false;
    }

    public void startGame() {
        isGameOver = false;
        // Initialize game settings
    }

    public void endTheGame() {
        isGameOver = true;
        // Handle end of game logic
    }

    public boolean isGameOver() {
        return isGameOver;
    }
}