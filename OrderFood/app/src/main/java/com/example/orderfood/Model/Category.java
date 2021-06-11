package com.example.orderfood.Model;

public class Category {

    public static final String CATEGORY_NAME = "Category Name";
    public static final String CATEGORY_ID = "Category Id";

    private String Name;
    private String Image;

    public Category(){}

    public Category(String name, String image) {
        Name = name;
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
