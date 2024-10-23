package com.example.fighterplane;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

// Enemy class with health, name, and position
class Enemy {
    String name;
    float x, y;
    int maxHealth, currentHealth;
    public  int score = GameView.score;

    Enemy(String name, float x, float y, int maxHealth) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    // Method to reduce health
    // Inside Enemy class
    void reduceHealth(int damage) {
        currentHealth -= damage;
        if (currentHealth < 0) {
            currentHealth = 0;
        }

        // Update score based on enemy's health decrease
        if (currentHealth == 0) {
            score += 10; // Increase score by 10 when an enemy is destroyed
        }
    }


    // Check if enemy is dead
    boolean isDead() {
        return currentHealth <= 0;
    }
}


// MasterEnemy class extending Enemy
class MasterEnemy extends Enemy {
    public Bitmap masterEnemyBitmap;
    // Additional properties specific to MasterEnemy (if any)
    private float speed;

    MasterEnemy(String name, float x, float y, int maxHealth, float speed, Bitmap masterEnemyBitmap) {
        super(name, x, y, maxHealth);
        this.speed = speed;
        this.masterEnemyBitmap = masterEnemyBitmap;
        // MasterEnemy starts off-screen and should move in when needed
    }

    // Update method to control the movement and other behaviors
    @Override
    void reduceHealth(int damage) {
        currentHealth -= damage;
        if (currentHealth < 0) {
            currentHealth = 0;
        }

        // Update score based on enemy's health decrease
        if (currentHealth == 0) {
            score += 1000; // Increase score by 10 when an enemy is destroyed
        }
    }


    // Check if enemy is dead
    @Override
    boolean isDead() {
        return currentHealth <= 0;
    }

    void update() {
        // Move the MasterEnemy from left side to the screen
            x -= speed;

    }

    public void draw(Canvas canvas) {
        // Draw other game elements...

        // Draw master enemy
        if (GameView.masterEnemy != null) {
            // Assuming you have a method to draw the master enemy
            canvas.drawBitmap(masterEnemyBitmap, GameView.masterEnemy.x, GameView.masterEnemy.y, null);
        }
    }

    // Optionally, you can override draw method if you have specific drawing logic
    // @Override
    // void draw(Canvas canvas) {
    //     // Draw MasterEnemy on the canvas
    // }

}

