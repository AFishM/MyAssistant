package com.name.myassistant.m;

/**
 * Created by xu on 16-3-3.
 */
public class Alarm {
    int id;
    int hour;
    int minute;
    boolean isOpen;
    String note;
    int addressId;
    String weatherAddress;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getWeatherAddress() {
        return weatherAddress;
    }

    public void setWeatherAddress(String weatherAddress) {
        this.weatherAddress = weatherAddress;
    }
}
