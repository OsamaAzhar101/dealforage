package com.oasys.dealforage.entity;

public class Product {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getResalePrice() {
        return resalePrice;
    }

    public void setResalePrice(double resalePrice) {
        this.resalePrice = resalePrice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String id;
    private String title;
    private double price;
    private double resalePrice;
    private String category;


}
