package com.vaylabs.myapplication;

// Created by Nagaraju on 20/07/24.

import com.google.android.gms.maps.model.LatLng;

public class LocationDetails{
    LatLng latLng;
    String name;

    public LocationDetails(LatLng northeast, String s) {
        latLng = northeast;
        name = s;
    }
}
