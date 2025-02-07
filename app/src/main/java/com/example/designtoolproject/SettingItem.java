package com.example.designtoolproject;

public class SettingItem {
    private String title;
    private String description;
    private int type; // 0 - title, 1 - switch, 2 - dialog

    public SettingItem(String title, String description, int type) {
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getType() { return type; }
}

