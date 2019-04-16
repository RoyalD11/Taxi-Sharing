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
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreMatchActivity extends FragmentActivity implements OnMapReadyCallback{

    //Vars used to either display the map or display elements on the map
    private GoogleMap mMap;
    Location lastLocation;
    Location pickup = new Location(LocationManager.GPS_PROVIDER);
    LocationRequest locationRequest;
    private SupportMapFragment mapFragment;
    private Marker driverLocationMarker;
    private LatLng pickupLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    //Vars used for finding a driver
    private boolean driverFound = false;
    private int radius = 1;
    private String foundDriverID;

    //Vars used for accessing the database for information
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private String userId;
    private String imageUrl;

    //Vars for screen elements to be referenced later
    private ImageView profileButton;
    private Button findMatchPrompt;
    private TextView searchingText;
    private Spinner promptQ1Spin, promptQ2Spin, promptQ3Spin, promptQ4Spin;

    //Runs once the activity is opened on the device
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_match);

        //Sets the input mode so that the first field is not automatically selected
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); //Setting so that the screen focuses the input field you are currently on

        //One of many elements used to get the location of the user
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Declare and populate all the spinners that are in the find match prompt
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

        //create the firebase authentication variable and get the current user id of the customer signed in
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        myRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(userId);

        //Get reference to screen elements needed later
        profileButton = (ImageView) findViewById(R.id.userProfileButton);
        findMatchPrompt = (Button) findViewById(R.id.findMatchPromptButton);
        searchingText = (TextView) findViewById(R.id.searchingForDriverTitle);

        //lat and lng of the pick up location at the airport, as the location never changes
        pickup.setLatitude(45.322722222222225);
        pickup.setLongitude(-75.6665);

        //Listener used to get the profile image of the user to display on the screen
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //If data exists and there is more than one child in the section of the database i'm referencing
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    //Create a map that stores all the data retrieved from the database
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //Sets the profile image to the image view in the top left of the screen
                    if (map.get("profileImageUrl") != null) {
                        imageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(imageUrl).into(profileButton);
                    }
                }
            }

            //Useless method but is needed so no errors arise
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //onclick listener for the find match button in the prompt
        findMatchPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Get the current users id and reference the database at that users position
                final String user_id = mAuth.getCurrentUser().getUid();
                DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(user_id);

                //finds the proper attributes to be saved
                final String promptQ1 = promptQ1Spin.getSelectedItem().toString();
                final String promptQ2 = promptQ2Spin.getSelectedItem().toString();
                final String promptQ3 = promptQ3Spin.getSelectedItem().toString();
                final String promptQ4 = promptQ4Spin.getSelectedItem().toString();

                //Sets these values to the database
                myRef.child("Match Q4").setValue(promptQ1);
                myRef.child("Match Q5").setValue(promptQ2);
                myRef.child("Match Q6").setValue(promptQ3);
                myRef.child("Match Q7").setValue(promptQ4);

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

                //Calls the function to get the closest driver
                getClosestDriver();


            }
        });
    }

    //Finds the driver closest to the user
    private void getClosestDriver() {

        //Get a reference to the database where the active drivers current location is saved
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driverAvailable");

        //Create the Geofire for the driver database made above
        GeoFire driverLocationGeofire = new GeoFire(driverLocation);

        //Query the database for the driver closest to the passenger, checks in a circle around the passengers location, radius starts at 1km and is
        //increased by one for every iteration that does not find a driver.
        GeoQuery driverQuery = driverLocationGeofire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        driverQuery.removeAllListeners(); //removes the listener before opening the query so no errors occur

        //Listener that does the query
        driverQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            //Once the driver has been found this method saves the driver ID and gives the driver the customers ID that found it.
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound) {
                    driverFound = true;
                    foundDriverID = key;

                    //Give the driver found the Id of the customer who found them. Figure out how to append this id to an array so multiple can find the same driver
                    //LOOK HERE
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(foundDriverID);
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);
                    driverRef.updateChildren(map);

                    //Changes the text on the screen to say the driver was found then calls the location to get the drivers location
                    searchingText.setText("Driver Found!");
                    getDriverLocation();
                    searchingText.setText("Finding Passengers.");

                }
            }

            //Useless methods needed for implementation so no errors arise
            @Override
            public void onKeyExited(String key) {
            }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            //If a driver was not found by the query then increase the radius by one and recursively call the function
            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius+=1;
                    getClosestDriver();
                }
            }

            //Useless methods needed for implementation so no errors arise
            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    //Function that finds the drivers location and shows where the driver is on the map
    private void getDriverLocation() {

        //Gets reference to the database where the active drivers location is stored
        //REMEMBER TO CHANGE THIS WHEN MOH UPDATES DATABASE
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driverAvailable").child(foundDriverID).child("l");

        //Starts an event listener that is called each time the drivers location is changed
        driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            //Runs every time the drivers location changes
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double lat = 0;
                    double lng = 1;

                    //gets the drivers lat and lng if they are not null
                    if(map.get(0) != null){
                        lat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        lng = Double.parseDouble(map.get(1).toString());
                    }

                    //make a variable to store this lat and lng
                    //Remove a marker for the driver if one exists already
                    LatLng driverLatLng = new LatLng(lat, lng);
                    if(driverLocationMarker != null){
                        driverLocationMarker.remove();
                    }

                    //Create a marker and add it to the map for the drivers location
                    driverLocationMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver"));
                }
            }

            //Useless method needed so no errors arise
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

        //makes a request for the users current location every second, sets the accuracy to high
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Checks for permission before getting the location, this is a more recent feature so it is only needed if the android version is 6(Marshmallow) or above
        //If permissions given then set the location on the map to the users current location
        //If no permissions given then call the check permissions function
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
            final String customerId = mAuth.getCurrentUser().getUid();
            for(Location location : locationResult.getLocations()){
                lastLocation = location;

                LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                //Saves customers current location to the database
                DatabaseReference currentRef = FirebaseDatabase.getInstance().getReference("CustomerLocation");
                GeoFire geoFireCurrent = new GeoFire(currentRef);
                geoFireCurrent.setLocation(customerId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        }
    };

    //If no permissions given for the app this method asks for permission
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    //Called when permission is given, sets the location on the map to users current location
    //Otherwise puts a toast on the screen that tells the user they must give permission
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

    //Function that changes the screen to the user profile screen
    public void toUserProfile(View view) {
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
        finish();
        return;
    }

    //Displays the find match prompt when the find match button is pressed
    //Hides the information that was on the screen previously and displays the prompt on the screen
    //Only will trigger if there is a destination entered in the field
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

    //Removes the find match prompt and displays the information that was on the activity originally
    public void removePrompt(View view) {
        (findViewById(R.id.findMatchPromptView)).setVisibility(View.GONE);

        ((ImageView) findViewById(R.id.userProfileButton)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.profileText)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.destinationText)).setVisibility(View.VISIBLE);
        ((EditText) findViewById(R.id.enterDestination)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.findMatch)).setVisibility(View.VISIBLE);
    }

    //Hides the find match prompt and displays the searching message on the screen
    public void searchingForMatchPrompt(View view){
        (findViewById(R.id.findMatchPromptView)).setVisibility(View.GONE);
        searchingText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("CustomerLocation");
        DatabaseReference userRefPickup = FirebaseDatabase.getInstance().getReference("CustomerPickupLocation");

        GeoFire userGeo = new GeoFire(userRef);
        GeoFire userGeoPickup = new GeoFire(userRefPickup);

        userGeo.removeLocation(userId);
        userGeoPickup.removeLocation(userId);
    }
}
