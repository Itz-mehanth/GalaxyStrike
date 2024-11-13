package com.example.fighterplane.components;

import android.graphics.Bitmap;

 // Bullet class with attack power and horizontal speed
public class Bullet {
    public float x;
     public float y;
    public int attack;
    public float speedX; // Speed in the X direction
    public Bitmap bitmap;
    public String name;
    public Bullet(String name, float x, float y, int attack, float speedX, Bitmap bitmap) {
        this.x = x;
        this.y = y;
        this.attack = attack;
        this.speedX = speedX;
        this.bitmap = bitmap;
        this.name = name;
    }
}
