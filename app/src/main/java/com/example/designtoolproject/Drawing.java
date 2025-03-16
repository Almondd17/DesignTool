package com.example.designtoolproject;

public class Drawing {
    private String id;
    private String name;
    private String base64Image;

    public Drawing(String id, String name, String base64Image) {
        this.id = id;
        this.name = name;
        this.base64Image = base64Image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setName(String name) {
        this.name = name;
    }
}

