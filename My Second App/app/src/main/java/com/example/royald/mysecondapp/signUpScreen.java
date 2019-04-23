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
import android.util.Patterns;
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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class signUpScreen extends AppCompatActivity {

    //All variables needed
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private Button createAccount;

    private EditText email, confirmEmail, password, confirmPassword, firstName, lastName, phoneNumber, creditCard;

    private Spinner ageRange, genderSpin, q1Spin, q2Spin, q3Spin;

    private  String currentQ1Answer, currentQ2Answer, currentQ3Answer, compareValue;

    private ImageView profileImage;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        //get the current state of the firebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //If user provides valid log in information this will log them into the app and change the screen
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(signUpScreen.this, PreMatchActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        //Sets the input mode so that the first field is not automatically selected
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); //Setting so that the screen focuses the input field you are currently on

        //Set the user image on the screen
        profileImage = (ImageView) findViewById(R.id.profilePictureSignUp);
        profileImage.setImageResource(R.drawable.newuserimage);

        //Declare the Spinners
        Spinner ageSpinner = (Spinner) findViewById(R.id.ageRangeSpinner);
        Spinner genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        Spinner q1Spinner = (Spinner) findViewById(R.id.Q1Spinner);
        Spinner q2Spinner = (Spinner) findViewById(R.id.Q2Spinner);
        Spinner q3Spinner = (Spinner) findViewById(R.id.Q3Spinner);

        //Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(this, R.array.age_spinner, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender_spinner, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> q1Adapter = ArrayAdapter.createFromResource(this, R.array.Q1_spinner, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> q2Adapter = ArrayAdapter.createFromResource(this, R.array.Q2_spinner, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> q3Adapter = ArrayAdapter.createFromResource(this, R.array.Q3_spinner, android.R.layout.simple_spinner_item);

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

        //onclick listener for the profile image to open the gallery
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1); //1 means open Gallery, 2 would be open Camera
            }
        });

        //reference the button on the screen
        createAccount = (Button) findViewById(R.id.createAccountButton);

        //On click listener for the create account button
        createAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                //Variables used to check for correct responses
                email = (EditText) findViewById(R.id.emailSignUp);
                confirmEmail = (EditText) findViewById(R.id.emailReEnterSignUp);
                password = (EditText) findViewById(R.id.passwordSignUp);
                confirmPassword = (EditText) findViewById(R.id.passwordlReEnterSignUp);

                firstName = (EditText) findViewById(R.id.firstNameText);
                lastName = (EditText) findViewById(R.id.lastNameText);

                phoneNumber = (EditText) findViewById(R.id.phoneNumberText);

                creditCard = (EditText) findViewById(R.id.creditCardInput);

                ageRange = (Spinner) findViewById(R.id.ageRangeSpinner);
                genderSpin = (Spinner) findViewById(R.id.genderSpinner);

                q1Spin = (Spinner) findViewById(R.id.Q1Spinner);
                q2Spin = (Spinner) findViewById(R.id.Q2Spinner);
                q3Spin = (Spinner) findViewById(R.id.Q3Spinner);

                compareValue = "Answer";
                currentQ1Answer = q1Spin.getSelectedItem().toString();
                currentQ2Answer = q2Spin.getSelectedItem().toString();
                currentQ3Answer = q3Spin.getSelectedItem().toString();

                final String userEmail = email.getText().toString();
                final String userPassword = password.getText().toString();

                //If-statement used to check if all fields are valid
                if(TextUtils.isEmpty(email.getText()) || !isEmailValid(email.getText().toString()))
                    email.setError("Valid Email is required!");

                else if(TextUtils.isEmpty(confirmEmail.getText()) || !(email.getText().toString().equals(confirmEmail.getText().toString())))
                    confirmEmail.setError("Emails must match!");

                else if(TextUtils.isEmpty(password.getText()))
                    password.setError("Password is Required!");

                else if(password.getText().length() < 6)
                    password.setError("Password must 6 or more characters.");

                else if(TextUtils.isEmpty(confirmPassword.getText()) || !(password.getText().toString().equals(confirmPassword.getText().toString())))
                    confirmPassword.setError("Passwords must match!");


                else if (TextUtils.isEmpty(firstName.getText()))
                    firstName.setError("First Name is Required!");

                else if (TextUtils.isEmpty(lastName.getText()))
                    lastName.setError("Last Name is Required!");


                else if(TextUtils.isEmpty(phoneNumber.getText()) || isPhoneValid(phoneNumber.getText().toString()))
                    phoneNumber.setError("Valid Phone Number is Required");

                else if(TextUtils.isEmpty(creditCard.getText()) || !isCreditValid(creditCard.getText().toString()))
                    creditCard.setError("Valid Credit Card Number is Required");


                    //These three branches will make sure that a valid option is chosen from the matching question spinners.
                else if(currentQ1Answer.equals(compareValue)){
                    TextView errorText = (TextView)q1Spin.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText("Invalid.");//changes the selected item text to this
                }
                else if(currentQ2Answer.equals(compareValue)){
                    TextView errorText = (TextView)q2Spin.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText("Invalid.");//changes the selected item text to this
                }
                else if(currentQ3Answer.equals(compareValue)){
                    TextView errorText = (TextView)q3Spin.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText("Invalid.");//changes the selected item text to this
                }

                //If all field are valid the button will check for proper log in creation
                else {

                    //Creates a new user with the email and pass provided
                    mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(signUpScreen.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //If the creation fails post a toast to the screen
                            if(!task.isSuccessful()){
                                Toast.makeText(signUpScreen.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
                                task.getException().printStackTrace();
                            }

                            //If successful get reference to the current user and save all user information
                            else{
                                final String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(user_id);

                                //User information to be saved to the database
                                final String fName = firstName.getText().toString();
                                final String lName = lastName.getText().toString();
                                final String pNumber = phoneNumber.getText().toString();
                                final String cCard = creditCard.getText().toString();
                                final String age = ageRange.getSelectedItem().toString();
                                final String gender = genderSpin.getSelectedItem().toString();

                                //Map used to store the information to the database, field name and data
                                Map userData = new HashMap();
                                userData.put("First Name", fName);
                                userData.put("Last Name", lName);
                                userData.put("Email", userEmail);
                                userData.put("Age", age);
                                userData.put("Gender", gender);
                                userData.put("Phone Number", pNumber);
                                userData.put("Credit Card", cCard);
                                userData.put("Match Q1", currentQ1Answer);
                                userData.put("Match Q2", currentQ2Answer);
                                userData.put("Match Q3", currentQ3Answer);
                                userData.put("Rating", 0.0f);
                                userData.put("Searching", "No");

                                //Adds the map to the user based off their user ID
                                myDatabase.setValue(userData);

                                //If statement to set the users profile image to the databse, will only trigger if a picture was chosen
                                //Converts the image to a bitmap for less data storage
                                if(resultUri != null){
                                    final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(user_id);
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

                                                DatabaseReference myDatabaseImage = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(user_id);

                                                Map imageMap = new HashMap();

                                                imageMap.put("profileImageUrl", downloadUrl.toString());

                                                myDatabaseImage.updateChildren(imageMap);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });

    }

    //Returns true is the email is valid
    boolean isEmailValid(CharSequence emailString){
        return Patterns.EMAIL_ADDRESS.matcher(emailString).matches();
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

    //Starts the listener
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    //Stops the listener
    @Override
    protected void onStop(){
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
