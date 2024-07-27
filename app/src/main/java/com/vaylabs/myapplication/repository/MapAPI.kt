package com.codelabs.foodadminapp.feature_map.data

import com.codelabs.foodadminapp.feature_map.data.model.response.MapBoundsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


// Created by Nagaraju on 20/07/24.

interface MapAPI {
//   https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY
//    https://maps.googleapis.com/maps/api/geocode/json?address=Washington&bounds=36.47,-84.72%7C43.39,-65.90&key=YOUR_API_KEY
    @GET("maps/api/geocode/json")
    suspend fun getReverseGeocode(
            @Query("latlng") latlng:String,
            @Query("key") key:String
    ):Response<MapBoundsResponse>

}