package com.example.johnrobert.manongcustomer;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ProviderProfile implements Serializable {

    String about, services, achievements, address;

    public ProviderProfile() {}

    public ProviderProfile(String about, String services, String achievements, String address) {
        this.about = about;
        this.services = services;
        this.achievements = achievements;
        this.address = address;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
