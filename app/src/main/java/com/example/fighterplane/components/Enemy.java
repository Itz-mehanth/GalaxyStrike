package com.example.fighterplane.components;

import android.graphics.Bitmap;

import com.example.fighterplane.GameView;

// Enemy class with health, name, and position
public class Enemy {
    public String name;
    public float x;
    public float y;
    public int maxHealth, currentHealth;
    public Bitmap bitmap;
    public int attack;
    public Enemy(String name, float x, float y, int maxHealth, Bitmap bitmap, int attack) {
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
    public void reduceHealth(int damage) {
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
    public boolean isDead() {
        return currentHealth <= 0;
    }
}