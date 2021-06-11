package com.example.orderfood.Model;

public class ShippingInformation {
    private String orderId, shipperIphone;
    private Double lat, lng;

    public ShippingInformation() {
    }

    public ShippingInformation(String orderId, String shipperIphone, Double lat, Double lng) {
        this.orderId = orderId;
        this.shipperIphone = shipperIphone;
        this.lat = lat;
        this.lng = lng;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getShipperIphone() {
        return shipperIphone;
    }

    public void setShipperIphone(String shipperIphone) {
        this.shipperIphone = shipperIphone;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
