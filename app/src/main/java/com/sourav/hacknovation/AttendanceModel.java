package com.sourav.hacknovation;
public class AttendanceModel {
    public String name;
    public String roll;
    public String email;
    public String profileImage;
    public boolean present;

    public AttendanceModel() {}

    public AttendanceModel(String name, String roll,String email) {
        this.name = name;
        this.roll = roll;
        this.email = email;
        this.present = false;
    }
}
