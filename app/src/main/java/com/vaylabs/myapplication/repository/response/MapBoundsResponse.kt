package com.codelabs.foodadminapp.feature_map.data.model.response

import com.google.gson.annotations.SerializedName


data class MapBoundsResponse (

  @SerializedName("results" ) var results : ArrayList<Results> = arrayListOf()

)