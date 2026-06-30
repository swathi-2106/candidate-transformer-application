package com.candidate.transformer.model;

/**
 * Standardized location model for candidate residence or employment history location.
 */
public class Location {

    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String formattedAddress;

    public Location() {
    }

    public Location(String city, String state, String country, String postalCode, String formattedAddress) {
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
        this.formattedAddress = formattedAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    @Override
    public String toString() {
        return "Location{" +
                "city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", formattedAddress='" + formattedAddress + '\'' +
                '}';
    }
}
