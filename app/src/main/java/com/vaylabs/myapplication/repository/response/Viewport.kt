package com.codelabs.foodadminapp.feature_map.data.model.response

import com.codelabs.foodadminapp.feature_map.data.model.response.Northeast
import com.codelabs.foodadminapp.feature_map.data.model.response.Southwest
import com.google.gson.annotations.SerializedName


data class Viewport (

    @SerializedName("northeast" ) var northeast : Northeast? = Northeast(),
    @SerializedName("southwest" ) var southwest : Southwest? = Southwest()

)