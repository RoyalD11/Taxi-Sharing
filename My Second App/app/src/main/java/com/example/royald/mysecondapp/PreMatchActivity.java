package com.example.royald.mysecondapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class PreMatchActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    Location lastLocation;
    Location pickup = new Location(LocationManager.GPS_PROVIDER);
    LocationRequest locationRequest;

    private FusedLocationProviderClient fusedLocationProviderClient;

    //Vars used for finding a driver
    private boolean driverFound = false;
    private int radius = 1;
    private String foundDriverID;

    private SupportMapFragment mapFragment;

    private Marker pickupLocationMarker;

    private LatLng pickupLocation;

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private String userId;
    private String imageUrl;

    private ImageView profileButton;

    private Button findMatchPrompt;

    private TextView searchingText;

    private Spinner promptQ1Spin, promptQ2Spin, promptQ3Spin, promptQ4Spin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_match);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        promptQ1Spin = (Spinner) findViewById(R.id.promptQ1Spinner);
        ArrayAdapter<CharSequence> promptQ1Adapter = ArrayAdapter.createFromResource(this, R.array.promptQ1, android.R.layout.simple_spinner_item);
        promptQ1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        promptQ1Spin.setAdapter(promptQ1Adapter);

        promptQ2Spin = (Spinner) findViewById(R.id.promptQ2Spinner);
        ArrayAdapter<CharSequence> promptQ2Adapter = ArrayAdapter.createFromResource(this, R.array.promptQ2, android.R.layout.simple_spinner_item);
        promptQ2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        promptQ2Spin.setAdapter(promptQ2Adapter);

        promptQ3Spin = (Spinner) findViewById(R.id.promptQ3Spinner);
        ArrayAdapter<CharSequence> promptQ3Adapter = ArrayAdapter.createFromResource(this, R.array.promptQ3, android.R.layout.simple_spinner_item);
        promptQ3Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        promptQ3Spin.setAdapter(promptQ3Adapter);

        promptQ4Spin = (Spinner) findViewById(R.id.promptQ4Spinner);
        ArrayAdapter<CharSequence> promptQ4Adapter = ArrayAdapter.createFromResource(this, R.array.promptQ4, android.R.layout.simple_spinner_item);
        promptQ4Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        promptQ4Spin.setAdapter(promptQ4Adapter);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        myRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(userId);

        profileButton = (ImageView) findViewById(R.id.userProfileButton);

        findMatchPrompt = (Button) findViewById(R.id.findMatchPromptButton);

        searchingText = (TextView) findViewById(R.id.searchingForDriverTitle);

        pickup.setLatitude(45.322722222222225);
        pickup.setLongitude(-75.6665);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //If data exists and there is more than one child in the section of the database i'm referencing
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    //Create a map that stores all the data
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("profileImageUrl") != null) {
                        imageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(imageUrl).into(profileButton);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        findMatchPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_id = mAuth.getCurrentUser().getUid();
                DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(user_id);

                final String promptQ1 = promptQ1Spin.getSelectedItem().toString();
                final String promptQ2 = promptQ2Spin.getSelectedItem().toString();
                final String promptQ3 = promptQ3Spin.getSelectedItem().toString();
                final String promptQ4 = promptQ4Spin.getSelectedItem().toString();

                myRef.child("Match Q4").setValue(promptQ1);
                myRef.child("Match Q5").setValue(promptQ2);
                myRef.child("Match Q6").setValue(promptQ3);
                myRef.child("Match Q7").setValue(promptQ4);

                //Saves customers current location to the database
                DatabaseReference currentRef = FirebaseDatabase.getInstance().getReference("CustomerLocation");
                GeoFire geoFireCurrent = new GeoFire(currentRef);
                geoFireCurrent.setLocation(user_id, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                //Sets a marker at the pickup location
                pickupLocation = new LatLng(pickup.getLatitude(), pickup.getLongitude());
                mMap.addMarker((new MarkerOptions().position(pickupLocation).title("Pickup Location")));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pickupLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

                //saves pickup location to the database
                DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("CustomerPickupLocation");
                GeoFire geoFireRequest = new GeoFire(requestRef);
                geoFireRequest.setLocation(user_id, new GeoLocation(pickup.getLatitude(), pickup.getLongitude()));

                //calls this method
                searchingForMatchPrompt(v);

                getClosestDriver();


            }
        });
    }

    private void getClosestDriver() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driverAvailable");

        GeoFire driverLocationGeofire = new GeoFire(driverLocation);

        GeoQuery driverQuery = driverLocationGeofire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        driverQuery.removeAllListeners();

        driverQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound) {
                    driverFound = true;
                    foundDriverID = key;
                }
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius+=1;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Ottawa, Canada.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }
            else{
                checkLocationPermission();
            }
        }
    }

    //This method (disguised as a variable) gets the location of the user
    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult){
            for(Location location : locationResult.getLocations()){
                lastLocation = location;

                LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    public void toUserProfile(View view) {
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
    }

    public void displayPrompt(View view) {
        EditText destination = (EditText) findViewById(R.id.enterDestination);

        if (TextUtils.isEmpty(destination.getText()))
            destination.setError("Destination is required!");
        else {
            String destinationText = ((EditText) findViewById(R.id.enterDestination)).getText().toString();

            myRef.child("Destination").setValue(destinationText);

            ((ImageView) findViewById(R.id.userProfileButton)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.profileText)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.destinationText)).setVisibility(View.GONE);
            ((EditText) findViewById(R.id.enterDestination)).setVisibility(View.GONE);
            ((Button) findViewById(R.id.findMatch)).setVisibility(View.GONE);

            (findViewById(R.id.findMatchPromptView)).setVisibility(View.VISIBLE);
        }
    }

    public void removePrompt(View view) {
        (findViewById(R.id.findMatchPromptView)).setVisibility(View.GONE);

        ((ImageView) findViewById(R.id.userProfileButton)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.profileText)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.destinationText)).setVisibility(View.VISIBLE);
        ((EditText) findViewById(R.id.enterDestination)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.findMatch)).setVisibility(View.VISIBLE);
    }

    public void searchingForMatchPrompt(View view){
        (findViewById(R.id.findMatchPromptView)).setVisibility(View.GONE);
        searchingText.setVisibility(View.VISIBLE);
    }

}
