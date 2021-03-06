package com.example.johnrobert.manongcustomer;

import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Request implements Serializable {

    private HashMap<String, String> questionsAndAnswers, images, quotes, booked;
    private HashMap<String, Double> location_latlng;
    private String serviceName, userId, key, locationName, cancellationReason, customerName;
    private Object timestamp;
    private Boolean isCancelled;

    public Request() { }

    public Request(HashMap<String, String> questionsAndAnswers, HashMap<String, String> images, HashMap<String, Double> location_latlng, String serviceName,
                   String userId, HashMap<String, String> quotes, Object timestamp, String locationName,
                   Boolean isCancelled, String cancellationReason, HashMap<String, String> booked, String customerName) {
        this.questionsAndAnswers = questionsAndAnswers;
        this.images = images;
        this.location_latlng = location_latlng;
        this.serviceName = serviceName;
        this.userId = userId;
        this.quotes = quotes;
        this.timestamp = timestamp;
        this.locationName = locationName;
        this.isCancelled = isCancelled;
        this.cancellationReason = cancellationReason;
        this.booked = booked;
        this.customerName = customerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public HashMap<String, String> getBooked() {
        return booked;
    }

    public void setBooked(HashMap<String, String> booked) {
        this.booked = booked;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Boolean getCancelled() {
        return isCancelled;
    }

    public void setCancelled(Boolean cancelled) {
        isCancelled = cancelled;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String, String> getQuotes() {
        return quotes;
    }

    public void setQuotes(HashMap<String, String> quotes) {
        this.quotes = quotes;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public HashMap<String, String> getQuestionsAndAnswers() {
        return questionsAndAnswers;
    }

    public void setQuestionsAndAnswers(HashMap<String, String> questionsAndAnswers) {
        this.questionsAndAnswers = questionsAndAnswers;
    }

    public HashMap<String, Double> getLocation_latlng() {
        return location_latlng;
    }

    public void setLocation_latlng(HashMap<String, Double> location_latlng) {
        this.location_latlng = location_latlng;
    }

    public HashMap<String, String> getImages() {
        return images;
    }

    public void setImages(HashMap<String, String> images) {
        this.images = images;
    }
}
