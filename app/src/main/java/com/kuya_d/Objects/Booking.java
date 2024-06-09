package com.kuya_d.Objects;

import java.util.ArrayList;

public class Booking {
    String id;
    String customerUid;
    String firstName;
    String lastName;
    String mobile;
    long eventDate;
    String purok;
    String barangay;
    double total;
    String eventType;
    int bundleSize;
    int headcount;
    String status;
    ArrayList<ShopItem> dishes;
    long timestamp;

    public Booking() {
    }

    public Booking(String id, String customerUid, String firstName, String lastName, String mobile, long eventDate, String purok, String barangay, double total, String eventType, int bundleSize, int headcount, String status, ArrayList<ShopItem> dishes, long timestamp) {
        this.id = id;
        this.customerUid = customerUid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.eventDate = eventDate;
        this.purok = purok;
        this.barangay = barangay;
        this.total = total;
        this.eventType = eventType;
        this.bundleSize = bundleSize;
        this.headcount = headcount;
        this.status = status;
        this.dishes = dishes;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerUid() {
        return customerUid;
    }

    public void setCustomerUid(String customerUid) {
        this.customerUid = customerUid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public long getEventDate() {
        return eventDate;
    }

    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    public String getPurok() {
        return purok;
    }

    public void setPurok(String purok) {
        this.purok = purok;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getBundleSize() {
        return bundleSize;
    }

    public void setBundleSize(int bundleSize) {
        this.bundleSize = bundleSize;
    }

    public int getHeadcount() {
        return headcount;
    }

    public void setHeadcount(int headcount) {
        this.headcount = headcount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<ShopItem> getDishes() {
        return dishes;
    }

    public void setDishes(ArrayList<ShopItem> dishes) {
        this.dishes = dishes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
