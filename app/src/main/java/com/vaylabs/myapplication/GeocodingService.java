package com.vaylabs.myapplication;

// Created by Nagaraju on 20/07/24.

import com.vaylabs.myapplication.repository.java.Root;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingService {

    @GET("geocode/json")
    Call<Root> getGeocode(
            @Query("latlng") String address,
            @Query("key") String apiKey
    );
}
