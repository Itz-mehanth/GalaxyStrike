package com.example.fighterplane.components;

import android.graphics.Bitmap;

public class Hero{
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

    public boolean isDead() {
        return currentHealth <= 0;
    }
}
