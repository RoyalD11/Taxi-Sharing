package com.example.royald.mysecondapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class UserProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private String userId;

    private TextView nameView, emailView, ageView, genderView, phoneView, mQ1View, mQ2View, mQ3View;
    private RatingBar ratingBar;

    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String age;
    private String gender;
    private String phone;
    private String mQ1;
    private String mQ2;
    private String mQ3;
    private String imageUrl;

    private ImageView profileImage;

    private float rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        nameView = (TextView) findViewById(R.id.nameText);
        emailView = (TextView) findViewById(R.id.emailText);
        ageView = (TextView) findViewById(R.id.ageText);
        genderView = (TextView) findViewById(R.id.genderText);
        phoneView = (TextView) findViewById(R.id.phoneText);
        mQ1View = (TextView) findViewById(R.id.q1AnswerText);
        mQ2View = (TextView) findViewById(R.id.q2AnswerText);
        mQ3View = (TextView) findViewById(R.id.q3AnswerText);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        profileImage = (ImageView) findViewById(R.id.profileImageView);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        myRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(userId);

        ratingBar.setFocusable(false);

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
                        fullName = firstName + " " + lastName;
                        nameView.setText(fullName);
                    }

                    if(map.get("Email") != null){
                        email = map.get("Email").toString();
                        emailView.setText(email);
                    }

                    if(map.get("Age") != null){
                        age = map.get("Age").toString();
                        ageView.setText(age);
                    }

                    if(map.get("Gender") != null){
                        gender = map.get("Gender").toString();
                        genderView.setText(gender);
                    }

                    if(map.get("Phone Number") != null){
                        phone = map.get("Phone Number").toString();
                        phoneView.setText(phone);
                    }

                    if(map.get("Match Q1") != null){
                        mQ1 = map.get("Match Q1").toString();
                        mQ1View.setText(mQ1);
                    }

                    if(map.get("Match Q2") != null){
                        mQ2 = map.get("Match Q2").toString();
                        mQ2View.setText(mQ2);
                    }

                    if(map.get("Match Q3") != null){
                        mQ3 = map.get("Match Q3").toString();
                        mQ3View.setText(mQ3);
                    }

                    //Convert the string to a float before setting the rating bar to the value
                    if(map.get("Rating") != null){
                        rating = Float.parseFloat(map.get("Rating").toString());
                        ratingBar.setRating(rating);
                    }

                    if(map.get("profileImageUrl") != null){
                        imageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(imageUrl).into(profileImage);
                    }
                }
            }

            //Not needed but is part of the listener
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Function to go the the Edit Profile Activity
    public void toEditProfile(View view){
        Intent intent = new Intent(this, EditProfile.class);
        startActivity(intent);
    }

    //Function called when sign out button is pressed, signs out the current user and returns to the main activity screen
    public void signOut(View view){
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
