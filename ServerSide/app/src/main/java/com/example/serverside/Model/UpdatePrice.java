package com.example.serverside.Model;

public class UpdatePrice {
    String key, price;

    public UpdatePrice() {
    }

    public UpdatePrice(String key, String price) {
        this.key = key;
        this.price = price;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
