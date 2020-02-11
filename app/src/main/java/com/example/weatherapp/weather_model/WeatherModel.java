package com.example.weatherapp.weather_model;

import java.util.ArrayList;

public class WeatherModel {

    private float message;
    private float cnt;
    public ArrayList< Object > list = new ArrayList < > ();
    City city;

    public City getCity() {
        return city;
    }
    public float getCNT() {return cnt;}

    public ArrayList<Object> getList() {
        return list;
    }
}