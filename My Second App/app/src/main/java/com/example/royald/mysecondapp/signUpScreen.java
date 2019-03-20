package com.example.royald.mysecondapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signUpScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

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

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); //Setting so that the screen focuses the input field you are currently on

        ImageView image = (ImageView) findViewById(R.id.profilePictureSignUp);
        image.setImageResource(R.drawable.userimage);

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

    }

    /* called when the user taps Send*/
    public void createAccount(View view){

        //Variables used to check for correct responses
        EditText email = (EditText) findViewById(R.id.emailSignUp);
        EditText confirmEmail = (EditText) findViewById(R.id.emailReEnterSignUp);
        EditText password = (EditText) findViewById(R.id.passwordSignUp);
        EditText confirmPassword = (EditText) findViewById(R.id.passwordlReEnterSignUp);

        EditText firstName = (EditText) findViewById(R.id.firstNameText);
        EditText lastName = (EditText) findViewById(R.id.lastNameText);

        EditText phoneNumber = (EditText) findViewById(R.id.phoneNumberText);

        EditText creditCard = (EditText) findViewById(R.id.creditCardInput);

        Spinner q1Spinner = (Spinner) findViewById(R.id.Q1Spinner);
        Spinner q2Spinner = (Spinner) findViewById(R.id.Q2Spinner);
        Spinner q3Spinner = (Spinner) findViewById(R.id.Q3Spinner);

        String compareValue = "Answer";
        String currentQ1Answer = q1Spinner.getSelectedItem().toString();
        String currentQ2Answer = q2Spinner.getSelectedItem().toString();
        String currentQ3Answer = q3Spinner.getSelectedItem().toString();

        final String userEmail = email.getText().toString();
        final String userPassword = password.getText().toString();

        //If-statement used to check if all fields are valid
        if(TextUtils.isEmpty(email.getText()) || !isEmailValid(email.getText().toString()))
            email.setError("Valid Email is required!");

        else if(TextUtils.isEmpty(confirmEmail.getText()) || !(email.getText().toString().equals(confirmEmail.getText().toString())))
            confirmEmail.setError("Emails must match!");

        else if(TextUtils.isEmpty(password.getText()))
            password.setError("Password is Required!");

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

        //If all field are valid the button will start the new activity
        else {
            mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(signUpScreen.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Toast.makeText(signUpScreen.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(user_id);
                        current_user_db.setValue(true);
                    }
                }
            });
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
