package com.example.orderfood.Model;

public class Foods {

    public static final String  FOOD_NAME = "Food Name";
    public static final String  FOOD_ID = "Food Id";

    private String id, description, discount, image, menuId, name, price;

    public Foods() {}
    public Foods(String description, String discount, String image, String menuId, String name, String price) {
        this.description = description;
        this.discount = discount;
        this.image = image;
        this.menuId = menuId;
        this.name = name;
        this.price = price;
    }

//    public Foods(String id, String description, String discount, String image, String menuId, String name, String price) {
//        this.description = description;
//        this.discount = discount;
//        this.image = image;
//        this.menuId = menuId;
//        this.name = name;
//        this.price = price;
//        this.id = id;
//    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
}
