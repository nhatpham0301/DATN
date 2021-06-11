package com.example.serverside.Model;

import java.util.List;

public class Request {
    private String id, Phone, Name, Address, Total, Status, Comment, paymentMethod, paymentState, latLng;
    private List<Order> Foods;

    public Request() {
    }

    public Request(String id, String phone, String name, String address, String total, String status, String comment, String paymentMethod, String paymentState, String latLng, List<Order> foods) {
        Phone = phone;
        Name = name;
        Address = address;
        Total = total;
        Status = status;
        Comment = comment;
        this.paymentMethod = paymentMethod;
        this.paymentState = paymentState;
        this.latLng = latLng;
        Foods = foods;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public List<Order> getFoods() {
        return Foods;
    }

    public void setFoods(List<Order> foods) {
        Foods = foods;
    }
}