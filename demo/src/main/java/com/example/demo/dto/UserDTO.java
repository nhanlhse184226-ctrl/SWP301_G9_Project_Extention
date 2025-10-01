package com.example.demo.dto;

public class UserDTO {
    private int driverID;
    private String Name;
    private String Email;
    private String Password;

    public UserDTO() {
        this.driverID = 0;
        this.Name = "";
        this.Email = "";
        this.Password = "";
    }

    public UserDTO(int driverID, String Name, String Email, String Password) {
        this.driverID = driverID;
        this.Name = Name;
        this.Email = Email;
        this.Password = Password;
    }

    public int getDriverID() {
        return driverID;
    }

    public void setDriverID(int driverID) {
        this.driverID = driverID;
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
}
