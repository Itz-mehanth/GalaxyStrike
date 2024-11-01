package com.example.fighterplane;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

// Enemy class with health, name, and position
class Enemy {
    String name;
    float x, y;
    public int maxHealth, currentHealth;
    Bitmap bitmap;
    public int attack;
    Enemy(String name, float x, float y, int maxHealth, Bitmap bitmap, int attack) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.bitmap = bitmap;
        this.currentHealth = maxHealth;
        this.attack = attack;
    }

    // Method to reduce health
    // Inside Enemy class
    void reduceHealth(int damage) {
        currentHealth -= damage;
        System.out.println("reducing health of enemy");
        GameView.score += 20;
        System.out.println("reducing health: "+ currentHealth );
        if (currentHealth < 0) {
            currentHealth = 0;
        }

        // Update GameView.score based on enemy's health decrease
        if (currentHealth == 0) {
            GameView.score += 10; // Increase GameView.score by 10 when an enemy is destroyed
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
    boolean isDying = false;
    float dyingPositionY;
    // Additional properties specific to MasterEnemy (if any)
    private float speed;

    MasterEnemy(String name, float x, float y, int maxHealth, float speed, Bitmap masterEnemyBitmap,int attack) {
        super(name, x, y, maxHealth,masterEnemyBitmap,attack);
        this.speed = speed;
        this.masterEnemyBitmap = masterEnemyBitmap;
        // MasterEnemy starts off-screen and should move in when needed
    }

    @Override
    boolean isDead() {
        return super.currentHealth <= 0;
    }

    public void draw(Canvas canvas) {
        // Draw other game elements...

        // Draw master enemy
        if (GameView.masterEnemy != null) {
            // Assuming you have a method to draw the master enemy
            canvas.drawBitmap(masterEnemyBitmap, GameView.masterEnemy.x, GameView.masterEnemy.y, null);
        }
    }

    // Call this method when the enemy dies
    public void startDying(Context context) {
        this.isDying = true;
        this.dyingPositionY = this.y; // Start dying position from current position
    }

    // Update the enemy's position if it's in the dying state
    public void updateDyingPosition() {
        if (isDying) {
            dyingPositionY += 5; // Adjust speed as needed
            if (dyingPositionY > GameView.screenHeight) {
                // Once off-screen, reset or remove the enemy
                isDying = false;
            }
        }
    }
}

class Hero{
    public String name;
    public float x, y;
    static int highscore;
    public Bitmap bitmap;
    public int currentHealth;
    public int maxHealth;
    public Hero(String name, float x, float y, int maxHealth, Bitmap bitmap){
        this.name = name;
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.currentHealth = maxHealth;
        this.maxHealth = maxHealth;
    }

    boolean isDead() {
        return currentHealth <= 0;
    }

}

// Bullet class with attack power and horizontal speed
class Bullet {
    float x, y;
    int attack;
    float speedX; // Speed in the X direction
    Bitmap bitmap;
    String name;
    Bullet(String name,float x, float y, int attack, float speedX, Bitmap bitmap) {
        this.x = x;
        this.y = y;
        this.attack = attack;
        this.speedX = speedX;
        this.bitmap = bitmap;
        this.name = name;
    }
}

class PowerUp {
    public float x, y;
    public Bitmap bitmap;
    public String type; // Different types of power-ups

    public PowerUp(Bitmap bitmap, float x, float y, String type) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.type = type;
    }
}

class Ship {
    private String name;
    private int cost;

    public Ship(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }
}


class GameManager {
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





