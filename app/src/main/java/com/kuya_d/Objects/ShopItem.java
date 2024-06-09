package com.kuya_d.Objects;

public class ShopItem {
    String id;
    String categoryId;
    String productName;
    String productDetails;
    double price;
    boolean available;
    Long thumbnail;

    public ShopItem() {
    }

    public ShopItem(String id, String categoryId, String productName, String productDetails, double price, boolean available, Long thumbnail) {
        this.id = id;
        this.categoryId = categoryId;
        this.productName = productName;
        this.productDetails = productDetails;
        this.price = price;
        this.available = available;
        this.thumbnail = thumbnail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Long getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Long thumbnail) {
        this.thumbnail = thumbnail;
    }
}
