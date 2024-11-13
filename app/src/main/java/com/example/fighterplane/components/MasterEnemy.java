package com.example.fighterplane.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.example.fighterplane.GameView;

// MasterEnemy class extending Enemy
public class MasterEnemy extends Enemy {
    public Bitmap masterEnemyBitmap;
    public boolean isDying = false;
    public float dyingPositionY;
    // Additional properties specific to MasterEnemy (if any)
    private float speed;

    public MasterEnemy(String name, float x, float y, int maxHealth, float speed, Bitmap masterEnemyBitmap, int attack) {
        super(name, x, y, maxHealth,masterEnemyBitmap,attack);
        this.speed = speed;
        this.masterEnemyBitmap = masterEnemyBitmap;
        // MasterEnemy starts off-screen and should move in when needed
    }

    @Override
    public boolean isDead() {
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
