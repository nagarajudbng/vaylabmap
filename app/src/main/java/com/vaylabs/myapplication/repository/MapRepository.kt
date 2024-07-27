package com.codelabs.foodadminapp.feature_map.data

import com.codelabs.foodadminapp.feature_map.data.model.response.MapBoundsResponse
import okhttp3.Response


// Created by Nagaraju on 20/07/24.

interface MapRepository {
    suspend fun getBounds(lat: Double, long: Double) : MapBoundsResponse

}