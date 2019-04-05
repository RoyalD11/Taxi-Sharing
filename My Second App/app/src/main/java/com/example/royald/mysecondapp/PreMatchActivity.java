package com.example.royald.mysecondapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PreMatchActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private String userId;
    private String imageUrl;

    private ImageView profileButton;

    private Button findMatchPrompt;

    private Spinner promptQ1Spin, promptQ2Spin, promptQ3Spin, promptQ4Spin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_match);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //If data exists and there is more than one child in the section of the database i'm referencing
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    //Create a map that stores all the data
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("profileImageUrl") != null){
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

        // Add a marker in Ottawa at the airport and move the camera
        LatLng ottawa = new LatLng(45.32, -75.66);
        mMap.addMarker(new MarkerOptions().position(ottawa).title("Ottawa International Airport"));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(ottawa).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);
    }

    public void toUserProfile(View view){
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
    }

    public void displayPrompt(View view){
        EditText destination = (EditText) findViewById(R.id.enterDestination);

        if(TextUtils.isEmpty(destination.getText())) destination.setError("Destination is required!");
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

    public void removePrompt(View view){
        (findViewById(R.id.findMatchPromptView)).setVisibility(View.GONE);

        ((ImageView) findViewById(R.id.userProfileButton)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.profileText)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.destinationText)).setVisibility(View.VISIBLE);
        ((EditText) findViewById(R.id.enterDestination)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.findMatch)).setVisibility(View.VISIBLE);
    }
}
