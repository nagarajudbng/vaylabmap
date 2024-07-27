package com.codelabs.foodadminapp.feature_map.data

import android.util.Log
import com.codelabs.foodadminapp.feature_map.data.model.response.MapBoundsResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// Created by Nagaraju on 20/07/24.

class MapRepositoryImpl : MapRepository {
    override suspend fun getBounds(lat: Double, long: Double): MapBoundsResponse {
        val latlong:String = "$lat,$long"
        Log.d("Response 0",latlong)
        val response = getAPI().getReverseGeocode(latlong,"AIzaSyAkW5YjPdRPMQsTHznctNC9LHRFWAjOST0")
        Log.d("Response 1",response.toString())
        if (response.isSuccessful) {
            Log.d("Response 2",response.body().toString())
            return response.body()!!
        } else {
            Log.d("Response 3",response.message())
            throw Exception(response.message())
        }
    }

    fun getAPI(): MapAPI {
        return getRetrofit().create(MapAPI::class.java)
    }
    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(getOkHTTP())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun getOkHTTP(): OkHttpClient {
        return OkHttpClient.Builder()

            .addInterceptor(Interceptor { chain ->
                val originalRequest: Request = chain.request()
                Log.d("Host",originalRequest.url.host)
                val newRequest: Request = originalRequest.newBuilder()
                    .header("Host", originalRequest.url.host)
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept", "*/*")
                    .build()
                chain.proceed(newRequest)
            })
            .build()
    }

}