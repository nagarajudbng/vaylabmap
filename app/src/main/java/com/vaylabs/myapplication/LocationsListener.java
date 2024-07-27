package com.vaylabs.myapplication;

// Created by Nagaraju on 20/07/24.

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public interface LocationsListener {
    public void OnLocationsReceived(ArrayList<LocationDetails> locations);
}
