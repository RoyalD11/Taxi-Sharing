package com.example.royald.mysecondapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private String userId;

    private EditText firstNameView, lastNameView, phoneView, creditCardView;

    private Spinner ageSpinner, genderSpinner, q1Spinner, q2Spinner, q3Spinner;

    private ArrayAdapter<CharSequence> ageAdapter, genderAdapter, q1Adapter, q2Adapter, q3Adapter;

    private String firstName;
    private String lastName;
    private String age;
    private String gender;
    private String phone;
    private String cCard;
    private String mQ1;
    private String mQ2;
    private String mQ3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //FIX THIS, THE SPINNERS SHOULD DEFAULT TO WHAT THEY ANSWERED

        ageSpinner = (Spinner) findViewById(R.id.ageRangeSpinner);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        q1Spinner = (Spinner) findViewById(R.id.Q1Spinner);
        q2Spinner = (Spinner) findViewById(R.id.Q2Spinner);
        q3Spinner = (Spinner) findViewById(R.id.Q3Spinner);

        //Create an ArrayAdapter using the string array and a default spinner layout
        ageAdapter = ArrayAdapter.createFromResource(this, R.array.age_spinner, android.R.layout.simple_spinner_item);
        genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender_spinner, android.R.layout.simple_spinner_item);
        q1Adapter = ArrayAdapter.createFromResource(this, R.array.Q1_spinner, android.R.layout.simple_spinner_item);
        q2Adapter = ArrayAdapter.createFromResource(this, R.array.Q2_spinner, android.R.layout.simple_spinner_item);
        q3Adapter = ArrayAdapter.createFromResource(this, R.array.Q3_spinner, android.R.layout.simple_spinner_item);


        //Specify the layout to use when the list of choices appears
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        q1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        q2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        q3Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        //Apply the adapter to the spinner
        ageSpinner.setAdapter(ageAdapter);
        genderSpinner.setAdapter(genderAdapter);
        q1Spinner.setAdapter(q1Adapter);
        q2Spinner.setAdapter(q2Adapter);
        q3Spinner.setAdapter(q3Adapter);

        firstNameView = findViewById(R.id.firstNameText);
        lastNameView = findViewById(R.id.lastNameText);
        phoneView = findViewById(R.id.phoneNumberText);
        creditCardView = findViewById(R.id.creditCardText);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        myRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(userId);

        getUserInfo();
    }

    //Function that will get the user's information from the database
    private void getUserInfo(){

        //Is called on activity load
        //Gets the data from my reference of my database, specifically gets the current user from Users -> Passengers
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //If data exists and there is more than one child in the section of the database i'm referencing
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    //Create a map that stores all the data
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //If the key has a null value the 'if' is skipped
                    //If it has a value then we convert the value to a string and set it to the required TextView
                    if(map.get("First Name") != null && map.get("Last Name") != null){
                        firstName = map.get("First Name").toString();
                        lastName = map.get("Last Name").toString();
                        firstNameView.setText(firstName);
                        lastNameView.setText(lastName);
                    }

                    if(map.get("Age") != null){
                        age = map.get("Age").toString();
                    }

                    if(map.get("Gender") != null){
                        gender = map.get("Gender").toString();
                    }

                    if(map.get("Phone Number") != null){
                        phone = map.get("Phone Number").toString();
                        phoneView.setText(phone);
                    }

                    if(map.get("Credit Card") != null){
                        cCard = map.get("Credit Card").toString();
                        creditCardView.setText(cCard);
                    }

                    if(map.get("Match Q1") != null){
                        mQ1 = map.get("Match Q1").toString();
                    }

                    if(map.get("Match Q2") != null){
                        mQ2 = map.get("Match Q2").toString();
                    }

                    if(map.get("Match Q3") != null){
                        mQ3 = map.get("Match Q3").toString();
                    }
                }
            }

            //Not needed but is part of the listener
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void toUserProfile(View view){
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
    }
}
