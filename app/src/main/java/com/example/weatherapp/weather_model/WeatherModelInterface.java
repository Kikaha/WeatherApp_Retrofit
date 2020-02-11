package com.example.weatherapp.weather_model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherModelInterface {

    @GET("forecast")
    Call<WeatherModel> getWeatherForecast(@Query("lat") double lat,
                                          @Query("lon") double lon,
                                          @Query("cnt") int number,
                                          @Query("units") String units,
                                          @Query("appid") String apiKey);

}
