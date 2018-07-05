package com.example.johnrobert.manongcustomer;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Service implements Serializable {

    private String serviceName;

    private ArrayList<String> title, subtitle;

    private ArrayList<ArrayList<String>> answers;

    private ArrayList<Boolean> isInput;
    private ArrayList<Integer> viewTypes;

    private String locationName;
    private Double latitude;
    private Double longtitude;

    public Service() {}


    public Service(String serviceName, ArrayList<String> title, ArrayList<String> subtitle, ArrayList<ArrayList<String>> answers, ArrayList<Boolean> isInput, ArrayList<Integer> viewTypes, String locationName, Double latitude, Double longtitude) {
        this.serviceName = serviceName;
        this.title = title;
        this.subtitle = subtitle;
        this.answers = answers;
        this.isInput = isInput;
        this.viewTypes = viewTypes;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ArrayList<String> getTitle() {
        return title;
    }

    public void setTitle(ArrayList<String> title) {
        this.title = title;
    }

    public ArrayList<String> getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(ArrayList<String> subtitle) {
        this.subtitle = subtitle;
    }

    public ArrayList<ArrayList<String>> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<ArrayList<String>> answers) {
        this.answers = answers;
    }

    public ArrayList<Boolean> getIsInput() {
        return isInput;
    }

    public void setIsInput(ArrayList<Boolean> isInput) {
        this.isInput = isInput;
    }

    public ArrayList<Integer> getViewTypes() {
        return viewTypes;
    }

    public void setViewTypes(ArrayList<Integer> viewTypes) {
        this.viewTypes = viewTypes;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(Double longtitude) {
        this.longtitude = longtitude;
    }
}
