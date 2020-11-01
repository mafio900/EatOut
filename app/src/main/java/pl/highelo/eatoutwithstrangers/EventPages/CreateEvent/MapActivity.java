package pl.highelo.eatoutwithstrangers.EventPages.CreateEvent;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

import pl.highelo.eatoutwithstrangers.ModelsAndUtilities.LocationResolver;
import pl.highelo.eatoutwithstrangers.R;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";

    private Toolbar mToolbar;

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 1001;

    private boolean mLocationPermissionGranted = false;

    private GoogleMap mMap;
    private Location mLocation;
    private Place mSelectedPlace;
    private Marker mMarker;
    Button selectButton;

    private ImageView mMyLocationIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Lokalizacja wydarzenia");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMyLocationIcon = (ImageView) findViewById(R.id.myLocationIcon);
        selectButton = (Button) findViewById(R.id.selectButton);
        getLocationPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Places.deinitialize();
    }

    private void init(){
        mMyLocationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceCurrentLocation();
            }
        });
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
            AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
            autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
            autocompleteSupportFragment.setCountry("PL");
            autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    mSelectedPlace = place;
                    geoLocate();
                    selectButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(MapActivity.this, status.getStatusMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        selectButton = (Button) findViewById(R.id.selectButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedPlace != null){
                    Intent result = new Intent();
                    result.putExtra("placeName", mSelectedPlace.getName());
                    result.putExtra("placeAddress", mSelectedPlace.getAddress());
                    result.putExtra("placeLatLng", mSelectedPlace.getLatLng());
                    setResult(RESULT_OK, result);
                    finish();
                }
            }
        });
    }

    private void geoLocate(){
        if(mSelectedPlace != null){
            Toast.makeText(this, mSelectedPlace.getAddress(), Toast.LENGTH_LONG).show();
            moveCamera(mSelectedPlace.getLatLng(), 15f, mSelectedPlace.getName());
        }
    }

    private void getDeviceCurrentLocation(){
        try {
            if(mLocationPermissionGranted){
                LocationResolver.LocationResult locationResult = new LocationResolver.LocationResult() {
                    @Override
                    public void gotLocation(Location location) {
                        mLocation = location;
                        LatLng ll = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                        moveCamera(ll, 15f, "");
                    }
                };
                LocationResolver locationResolver = new LocationResolver();
                locationResolver.getLocation(this, locationResult, 20000);
            }
        }catch (SecurityException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("")){
            if(mMarker != null){
                mMarker.remove();
            }
            mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        }
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if(mLocationPermissionGranted){
                    getDeviceCurrentLocation();
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.getUiSettings().setRotateGesturesEnabled(false);
                    init();
                }
            }
        });
    }

    private void getLocationPermissions(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if(grantResults.length > 0){
                    for(int i : grantResults){
                        if(!(i == PackageManager.PERMISSION_GRANTED)){
                            Toast.makeText(this, R.string.location_permissions_denied, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;

                    initMap();
                }
                break;
        }
    }
}
