package pl.highelo.eatoutwithstrangers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";

    private Toolbar mToolbar;

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 1001;

    private boolean mLocationPermissionGranted = false;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Place mSelectedPlace;
    private Marker mMarker;
    Button selectButton;

    private ImageView mMyLocationIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMyLocationIcon = (ImageView) findViewById(R.id.myLocationIcon);
        selectButton = (Button) findViewById(R.id.selectButton);
        getLocationPermissions();
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
            autocompleteSupportFragment.setCountries("PL");
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
                    result.putExtra("place", mSelectedPlace);
                    result.putExtra("placeName", mSelectedPlace.getName());
                    result.putExtra("placeAddress", mSelectedPlace.getAddress());
                    result.putExtra("placeLatLng", mSelectedPlace.getLatLng().latitude + "," + mSelectedPlace.getLatLng().longitude);
                    setResult(RESULT_OK, result);
                    Places.deinitialize();
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
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            LatLng ll = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera(ll, 15f, "");
                        } else {
                            Toast.makeText(MapActivity.this, "Nie można znaleźć obecnej lokalizacji", Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
                Toast.makeText(MapActivity.this, "Mapa jest gotowa!", Toast.LENGTH_LONG).show();
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
