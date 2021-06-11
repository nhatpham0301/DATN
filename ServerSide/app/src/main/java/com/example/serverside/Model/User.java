package com.example.serverside.Model;

public class User {
    private String Name;
    private String Phone;
    private String Password;
    private String IsStaff;

    public User() {}

    public User(String name, String phone, String password, String isStaff) {
        Name = name;
        Phone = phone;
        Password = password;
        IsStaff = isStaff;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }
}
