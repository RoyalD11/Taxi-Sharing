package com.example.royald.mysecondapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    //All Variables needed for the work in this class
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private String userId;

    private EditText firstNameView, lastNameView, phoneView, creditCardView;

    private Spinner ageSpinner, genderSpinner, q1Spinner, q2Spinner, q3Spinner;

    private ArrayAdapter<CharSequence> ageAdapter, genderAdapter, q1Adapter, q2Adapter, q3Adapter;

    private Button returnToProfile;

    private String firstName, lastName, age, gender, phone, cCard,
            mQ1, mQ2, mQ3, currentQ1Answer, currentQ2Answer, currentQ3Answer,
            compareValue, imageUrl;

    private int spinnerPosition;

    private ImageView profileImage;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Get a reference to the Button on the view page
        returnToProfile = (Button) findViewById(R.id.returnProfileButton);

        //Get a reference to the Spinner on the design page
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

        //Needed elements from the design page, all the editTexts
        firstNameView = findViewById(R.id.firstNameText);
        lastNameView = findViewById(R.id.lastNameText);
        phoneView = findViewById(R.id.phoneNumberText);
        creditCardView = findViewById(R.id.creditCardText);

        profileImage = (ImageView) findViewById(R.id.profilePictureSignUp);

        //Firebase variables
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        myRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(userId);

        //Gets the current user's information and displays it in the required fields.
        getUserInfo();

        //Listener for the profile image, opens the users gallery so they can choose a picture
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1); //1 means open Gallery, 2 would be open Camera
            }
        });

        //Listener for the return to profile button
        returnToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Variables used to check for correct responses
                compareValue = "Answer";
                currentQ1Answer = q1Spinner.getSelectedItem().toString();
                currentQ2Answer = q2Spinner.getSelectedItem().toString();
                currentQ3Answer = q3Spinner.getSelectedItem().toString();

                //If-statement used to check if all fields are valid
                if (TextUtils.isEmpty(firstNameView.getText()))
                    firstNameView.setError("First Name is Required!");

                else if (TextUtils.isEmpty(lastNameView.getText()))
                    lastNameView.setError("Last Name is Required!");


                else if(TextUtils.isEmpty(phoneView.getText()) || isPhoneValid(phoneView.getText().toString()))
                    phoneView.setError("Valid Phone Number is Required");

                else if(TextUtils.isEmpty(creditCardView.getText()) || !isCreditValid(creditCardView.getText().toString()))
                    creditCardView.setError("Valid Credit Card Number is Required");

                //These three branches will make sure that a valid option is chosen from the matching question spinners.
                else if(currentQ1Answer.equals(compareValue)){
                    TextView errorText = (TextView)q1Spinner.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText("Invalid.");//changes the selected item text to this
                }
                else if(currentQ2Answer.equals(compareValue)){
                    TextView errorText = (TextView)q2Spinner.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText("Invalid.");//changes the selected item text to this
                }
                else if(currentQ3Answer.equals(compareValue)){
                    TextView errorText = (TextView)q3Spinner.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText("Invalid.");//changes the selected item text to this
                }

                //If all field are valid the button update the information and return to the profile
                else {

                    //Current value of all the fields in the profile screen
                    firstName = firstNameView.getText().toString();
                    lastName= lastNameView.getText().toString();
                    age = ageSpinner.getSelectedItem().toString();
                    gender = genderSpinner.getSelectedItem().toString();
                    phone = phoneView.getText().toString();
                    cCard = creditCardView.getText().toString();
                    mQ1 = q1Spinner.getSelectedItem().toString();
                    mQ2 = q2Spinner.getSelectedItem().toString();
                    mQ3 = q3Spinner.getSelectedItem().toString();

                    //Sets the value to the database
                    myRef.child("First Name").setValue(firstName);
                    myRef.child("Last Name").setValue(lastName);
                    myRef.child("Age").setValue(age);
                    myRef.child("Gender").setValue(gender);
                    myRef.child("Phone Number").setValue(phone);
                    myRef.child("Credit Card").setValue(cCard);
                    myRef.child("Match Q1").setValue(mQ1);
                    myRef.child("Match Q2").setValue(mQ2);
                    myRef.child("Match Q3").setValue(mQ3);

                    //If statement to set the users profile image to the databse, will only trigger if a picture was chosen
                    //Converts the image to a bitmap for less data storage
                    if(resultUri != null){
                        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);
                        Bitmap bitmap = null;

                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }

                        //Compresses the image for better data usage when sent to databse
                        ByteArrayOutputStream boas = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, boas);
                        byte[] data = boas.toByteArray();
                        UploadTask uploadTask = filePath.putBytes(data);

                        //Uploads the image to the database
                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(!task.isSuccessful()){
                                    throw task.getException();
                                }
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    Uri downloadUrl = task.getResult();

                                    DatabaseReference myDatabaseImage = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(userId);

                                    Map imageMap = new HashMap();

                                    imageMap.put("profileImageUrl", downloadUrl.toString());

                                    myDatabaseImage.updateChildren(imageMap);
                                }
                            }
                        });
                    }

                    //Makes a toast to tell the user the profile has been updated
                    Toast.makeText(EditProfile.this, "Updating Profile", Toast.LENGTH_LONG).show();
                    toUserProfile(view);
                }
            }
        });
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
                        spinnerPosition = ageAdapter.getPosition(age);
                        ageSpinner.setSelection(spinnerPosition);
                    }

                    if(map.get("Gender") != null){
                        gender = map.get("Gender").toString();
                        spinnerPosition = genderAdapter.getPosition(gender);
                        genderSpinner.setSelection(spinnerPosition);
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
                        spinnerPosition = q1Adapter.getPosition(mQ1);
                        q1Spinner.setSelection(spinnerPosition);
                    }

                    if(map.get("Match Q2") != null){
                        mQ2 = map.get("Match Q2").toString();
                        spinnerPosition = q2Adapter.getPosition(mQ2);
                        q2Spinner.setSelection(spinnerPosition);
                    }

                    if(map.get("Match Q3") != null){
                        mQ3 = map.get("Match Q3").toString();
                        spinnerPosition = q3Adapter.getPosition(mQ3);
                        q3Spinner.setSelection(spinnerPosition);
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

    //Used to get the image uri based on the data of the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profileImage.setImageURI(resultUri);
        }
    }

    //Returns true is the phone number is valid
    boolean isPhoneValid(String phoneString){
        if(phoneString.length() <  10 || phoneString.length() > 11) return true;
        return false;
    }

    //Returns true is the phone number is valid
    boolean isCreditValid(String creditString){
        if(creditString.length() == 16) return true;
        return false;
    }

    //Returns the the user profile screen
    public void toUserProfile(View view){
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
    }
}
