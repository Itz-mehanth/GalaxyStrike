package com.example.fighterplane.components;

import android.graphics.Bitmap;

public class Ship {
    private String name;
    private int cost;
    private int imageResourceId; // Add this field to hold the image resource ID
    private Bitmap bitmap;
    // Constructor to initialize all properties
    public Ship(String name, int cost, int imageResourceId) {
        this.name = name;
        this.cost = cost;
        this.imageResourceId = imageResourceId;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}
