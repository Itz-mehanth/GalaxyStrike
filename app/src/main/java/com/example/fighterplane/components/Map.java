package com.example.fighterplane.components;

public class Map {
    private String name;
    private int imageResId;

    public Map(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}

