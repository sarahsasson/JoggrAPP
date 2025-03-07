package com.example.joggrapp;

public class Group {
    private final String name;
    private final String description;

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
