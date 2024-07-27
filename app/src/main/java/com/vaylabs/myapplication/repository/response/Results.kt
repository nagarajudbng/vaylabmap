package com.codelabs.foodadminapp.feature_map.data.model.response

import com.codelabs.foodadminapp.feature_map.data.model.response.AddressComponents
import com.codelabs.foodadminapp.feature_map.data.model.response.Geometry
import com.google.gson.annotations.SerializedName


data class Results (

    @SerializedName("address_components" ) var addressComponents : ArrayList<AddressComponents> = arrayListOf(),
    @SerializedName("formatted_address"  ) var formattedAddress  : String?                      = null,
    @SerializedName("geometry"           ) var geometry          : Geometry?                    = Geometry(),
    @SerializedName("place_id"           ) var placeId           : String?                      = null,
    @SerializedName("types"              ) var types             : ArrayList<String>            = arrayListOf()

)