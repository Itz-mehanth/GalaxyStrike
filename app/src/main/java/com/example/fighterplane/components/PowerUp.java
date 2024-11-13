package com.example.fighterplane.components;

import android.graphics.Bitmap;

public class PowerUp {
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
