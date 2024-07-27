// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.vaylabs.myapplication;

import static com.google.android.libraries.places.api.model.Place.Field.ADDRESS;
import static com.google.android.libraries.places.api.model.Place.Field.ID;
import static com.google.android.libraries.places.api.model.Place.Field.LAT_LNG;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.codelabs.foodadminapp.feature_map.data.MapRepositoryImpl;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationListener;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.TravelMode;
import com.vaylabs.myapplication.repository.java.Bounds;
import com.vaylabs.myapplication.repository.java.Root;
import com.vaylabs.myapplication.repository.java.Viewport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The "My
 * Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link permission#ACCESS_FINE_LOCATION} and {@link
 * permission#ACCESS_COARSE_LOCATION} are requested at run time. If either
 * permission is not granted, the Activity is finished with an error message.
 */
// [START maps_android_sample_my_location]
public class MyLocationDemoActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static final String TAG = "MainActivity";
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in {@link
     * #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;
    private GeoApiContext geoApiContext;
    private GoogleMap map;
    private MapRepositoryImpl mapRepositoryImpl;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private Marker northEastMarker;
    private Marker southWestMarker;
    private LatLng destinationLatLng;
    private FusedLocationProviderClient fusedLocationClient;
    int REQUEST_LOCATION_PERMISSION = 100;
    private PlacesClient placesClient;
    private Polygon rectangle;
    private Marker northWestMarker;
    private Marker southEastMarker;

    private boolean isStarted = false;

    private LinearLayout controlLayout;
    private Button startButton;
    private Button cancelButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);
        controlLayout = findViewById(R.id.control_layout);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);

        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyAkW5YjPdRPMQsTHznctNC9LHRFWAjOST0");
        }
        // Create a new Places client instance.
        placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        List<Place.Field> fields = Arrays.asList(ID, ADDRESS, LAT_LNG);
        autocompleteFragment.setPlaceFields(fields);

        // Set up a PlaceSelectionListener to handle the response
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {

            }

            @Override
            public void onPlaceSelected(Place place) {
                // Handle the selected place data
                String address = place.getAddress();

                if (place.getLatLng() != null) {
                    double latitude = place.getLatLng().latitude;
                    double longitude = place.getLatLng().longitude;
                    destinationLatLng = new LatLng(latitude,longitude);
                    getDirections( new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),destinationLatLng, TravelMode.DRIVING);
//                    String latlong = latitude + "::" + longitude;

//                    Intent resultIntent = new Intent();
//                    resultIntent.putExtra("location", address);
//                    resultIntent.putExtra("latlong", latlong);
//                    setResult(Activity.RESULT_OK, resultIntent);
//                    finish();
                } else {
                    // Handle case where latLng is null
                    Toast.makeText(getApplicationContext(), "Location not found", Toast.LENGTH_SHORT).show();
                }
            }

//
//            @Override
//            public void onError(com.google.android.libraries.places.api.model.Status status) {
//                // Handle the error
//                Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
//            }
        });



        mapRepositoryImpl = new MapRepositoryImpl();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // at last we calling our map fragment to update.
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for runtime permissions
        if (ActivityCompat.checkSelfPermission(this,  permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permissions already granted, proceed to get location
            fetchLastLocation();
        } else {
            // Request location permissions

            ActivityCompat.requestPermissions(this, new String[]{ permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }


        geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyAkW5YjPdRPMQsTHznctNC9LHRFWAjOST0")
                .build();
        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener( v->{
            isStarted = !isStarted;
            if(isStarted){
                startButton.setText(R.string.stop);
//                cancelButton.setVisibility(View.VISIBLE);
            } else {
                startButton.setText(R.string.start);
            }
        });
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener( v->{
            isStarted = false;
            controlLayout.setVisibility(View.INVISIBLE);
            fetchLastLocation();
            destinationLatLng =  null;
            map.clear();
        });
        CustomBottomSheetDialogFragment bottomSheetDialogFragment = new CustomBottomSheetDialogFragment();
        bottomSheetDialogFragment.show( getSupportFragmentManager(), CustomBottomSheetDialogFragment.TAG);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        map.getUiSettings().setMyLocationButtonEnabled(false);

        enableMyLocation();


    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // [START maps_check_location_permission]
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true);
        // [END maps_check_location_permission]

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // get the last know location from your location manager.
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return ;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);

        // now get the lat/lon from the location and do something with it.
//        nowDoSomethingWith(location.getLatitude(), location.getLongitude());
        Log.d("MyLocationDemoActivity", "onMyLocationButtonClick: " + location.getLatitude() + " " + location.getLongitude());
        LatLng singapore = new LatLng(location.getLatitude(), location.getLongitude());
//        CameraPosition defaultCameraPosition = CameraPosition.fromLatLngZoom(singapore, 11f);
//        map.setLatLngBoundsForCameraTarget(new LatLngBounds(singapore, singapore));
        map.addMarker(new MarkerOptions().position(singapore).title("Marker"));
//        viewModel.getBoundsDetails(
//                location.getLatitude(), location.getLongitude(),con
//        )
//        mapRepositoryImpl.getBounds(location.getLatitude(), location.getLongitude());
//        mapRepositoryImpl.getBounds(location.getLatitude(), location.getLongitude());
        /*getLocations(location.getLatitude(),location.getLongitude(), new LocationsListener() {
            @Override
            public void OnLocationsReceived(ArrayList<LocationDetails> locations) {

                locations.forEach(locationDetails -> {
                    map.addMarker(new MarkerOptions()
                            .position(locationDetails.latLng)
                            .title(locationDetails.name)
                            .snippet(locationDetails.name));
                });
            }
        });*/


        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }
    private void fetchLastLocation() {
        // Check if permissions are granted
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Use FusedLocationProviderClient to get last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Use location object here
                                mLastLocation = location;
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 15f));
                                // Example: Log the current location
                                Log.d("MapsActivity", "Latitude: " + latitude + ", Longitude: " + longitude);

                                // You can do further operations with the location data
                            } else {
                                // Handle case where no last known location is available
                            }
                        }
                    });
        }
    }
    // [START maps_check_location_permission_result]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
            permission.ACCESS_FINE_LOCATION) || PermissionUtils
            .isPermissionGranted(permissions, grantResults,
                permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
            // [END_EXCLUDE]
        }
    }
    // [END maps_check_location_permission_result]

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
            .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
    private void getLocations(double latitude, double longitude, LocationsListener locationsListener){
        ArrayList<LocationDetails> locations = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeocodingService service = retrofit.create(GeocodingService.class);

        // Example bounds for Washington
//        String bounds = "36.47,-84.72|43.39,-65.90";
//        String apiKey = "YOUR_API_KEY"; // Replace with your actual API key
        String latLng = latitude + "," + longitude;
        Call<Root> call = service.getGeocode(latLng, "AIzaSyAkW5YjPdRPMQsTHznctNC9LHRFWAjOST0");
        call.enqueue(new Callback<Root>() {
            @Override
            public void onResponse(Call<Root> call, Response<Root> response) {
                if(response.isSuccessful()){
                    Root root = response.body();
                    AtomicInteger i = new AtomicInteger(1);
                    root.results.forEach(result -> {
                        Bounds viewport = result.geometry.bounds;
                        if(viewport!=null) {
                            Log.d(TAG, "onResponse: " +viewport.northeast.lat + " " + viewport.northeast.lng);
                            LatLng northeast = new LatLng(viewport.northeast.lat, viewport.northeast.lng);
                            LatLng southwest = new LatLng(viewport.southwest.lat, viewport.southwest.lng);
                            LocationDetails locationNorthEast = new LocationDetails(northeast,"northeast+"+i);
                            LocationDetails locationSouthWest = new LocationDetails(northeast,"southwest+"+i);
                            locations.add(locationNorthEast);
                            locations.add(locationSouthWest);
                            i.getAndIncrement();
                        }

                    });
                    locationsListener.OnLocationsReceived(locations);
                }
            }

            @Override
            public void onFailure(Call<Root> call, Throwable t) {

            }
        });
    }
    private void getDirections(LatLng origin, LatLng destination, TravelMode travelMode) {
        DirectionsApi.newRequest(geoApiContext)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(travelMode)
                .setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        // Draw route on map
                        // Note: This method runs on a background thread, so update UI on the main thread
//                        runOnUiThread(() -> {
//                            // Parse DirectionsResult and draw route on map (e.g., polylines)
//                            // Example:
//                             DirectionsRoute route = result.routes[0];
//                             PolylineOptions polylineOptions = new PolylineOptions();
//                             for (DirectionsLeg leg : route.legs) {
//                                 for (DirectionsStep step : leg.steps) {
//                                     // Decode step polyline and add to polylineOptions
//                                 }
//                             }
//                             map.addPolyline(polylineOptions);
//                        });
                        runOnUiThread(
                                () -> {
                                    if (result.routes != null && result.routes.length > 0) {
                                        List<com.google.maps.model.LatLng> decodedPath = result.routes[0].overviewPolyline.decodePath();
                                        LatLng[] path = new LatLng[decodedPath.size()];

                                        for (int i = 0; i < decodedPath.size(); ++i) {
                                            path[i] = new LatLng(decodedPath.get(i).lat, decodedPath.get(i).lng);
                                        }

                                        // Clear any existing polylines on the map
                                        map.clear();

                                        // Draw the polyline on the map
                                        map.addPolyline(new PolylineOptions().addAll(Arrays.asList(path))
                                                .width(12)
                                                .color(Color.BLUE)
                                                .geodesic(true));

                                        // Add markers for origin and destination again (optional)
                                        map.addMarker(new MarkerOptions().position(origin).title("Origin"));
                                        map.addMarker(new MarkerOptions().position(destination).title("Destination"));

                                        // Move camera to the starting point of the route
//                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 7));

                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        builder.include(origin);
                                        builder.include(destination);
                                        LatLngBounds bounds = builder.build();

                                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                                        map.animateCamera(cu);
                                        controlLayout.setVisibility(View.VISIBLE);
                                    } else {
                                        Log.e(TAG, "Directions request failed");
                                    }
                                }
                        );
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        // Handle error
                        Log.e(TAG, "Failed to get directions: " + e.getMessage());
                    }
                });


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(map!=null){
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
            northEastMarker.remove();
            southWestMarker.remove();
            northWestMarker.remove();
            southEastMarker.remove();
        }
        LatLngBounds curScreen = map.getProjection()
                .getVisibleRegion().latLngBounds;
        northEastMarker = map.addMarker(new MarkerOptions().position(curScreen.northeast).title("NorthEast"));
        southWestMarker = map.addMarker(new MarkerOptions().position(curScreen.southwest).title("SouthEast"));

        Log.d("MyLocationDemoActivity",
                "onLocationChanged: " + location.getLatitude() + " " + location.getLongitude()+
        "northEastMarker: " + northEastMarker.getPosition().latitude + " " + northEastMarker.getPosition().longitude+
                "southWestMarker: " + southWestMarker.getPosition().latitude + " " + southWestMarker.getPosition().longitude);
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = map.addMarker(markerOptions);

        //move map camera
        if(isStarted) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        }
        if(rectangle!=null){
            rectangle.remove();
        }
        double padding = 0.00;
        LatLng northeast = new LatLng(curScreen.northeast.latitude + padding, curScreen.northeast.longitude- padding);
        LatLng southwest = new LatLng(curScreen.southwest.latitude- padding, curScreen.southwest.longitude + padding);
        LatLng northwest = new LatLng(northeast.latitude , southwest.longitude );
        LatLng southeast = new LatLng(southwest.latitude , northeast.longitude);

        northWestMarker = map.addMarker(new MarkerOptions().position(northwest).title("northwest"));
        southEastMarker = map.addMarker(new MarkerOptions().position(southeast).title("southeast"));
/*
        // Create rectangle polygon options
        PolygonOptions rectOptions = new PolygonOptions()
                .add(northwest, northeast, southeast, southwest)
                .strokeColor(Color.RED);

        // Add rectangle to map
         rectangle = map.addPolygon(rectOptions);
 */
         }
    }

}
// [END maps_android_sample_my_location]