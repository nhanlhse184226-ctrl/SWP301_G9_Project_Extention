package com.example.demo.dto;

public class UserDTO {
    private int userID;
    private String Name;
    private String Email;
    private String Password;
    private long phone;
    private int roleID;

    public UserDTO() {
        this.userID = 0;
        this.Name = "";
        this.Email = "";
        this.Password = "";
        this.phone = 0;
        this.roleID = 0;
    }

    public UserDTO(int userID, String Name, String Email, String Password, long phone, int roleID) {
        this.userID = userID;
        this.Name = Name;
        this.Email = Email;
        this.Password = Password;
        this.phone = phone;
        this.roleID = roleID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }
}
