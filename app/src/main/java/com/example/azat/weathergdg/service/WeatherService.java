package com.example.azat.weathergdg.service;


import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface WeatherService {

    @GET("forecast")
    Observable<ResponseBody> getWeather5Day(@Query("q") String cityName, @Query("APPID") String appId, @Query("units") String units);

    @GET("forecast")
    Observable<ResponseBody> getWeather5Day(@Query("lat") double lat,@Query("lon") double lon,  @Query("APPID") String appId, @Query("units") String units);
}
