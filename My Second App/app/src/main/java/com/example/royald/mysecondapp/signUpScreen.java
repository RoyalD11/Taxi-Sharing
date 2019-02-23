package com.example.royald.mysecondapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class signUpScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
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

        //If-statement used to check if all fields have something entered in them
        if(TextUtils.isEmpty(email.getText())) email.setError("Email is required!");
        else if(TextUtils.isEmpty(confirmEmail.getText()) || !(email.getText().toString().equals(confirmEmail.getText().toString()))) confirmEmail.setError("Email must match!");
        else if(TextUtils.isEmpty(password.getText())) password.setError("Password is Required!");
        else if(TextUtils.isEmpty(confirmPassword.getText()) || !(password.getText().toString().equals(confirmPassword.getText().toString()))) confirmPassword.setError("Password must match!");

        else if (TextUtils.isEmpty(firstName.getText())) firstName.setError("First Name is Required!");
        else if (TextUtils.isEmpty(lastName.getText())) lastName.setError("Last Name is Required!");

        else if(TextUtils.isEmpty(phoneNumber.getText())) phoneNumber.setError("Phone Number is Required");

        else if(TextUtils.isEmpty(creditCard.getText())) creditCard.setError("Credit Card Number is Required");

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


        else {
            Intent intent = new Intent(this, PreMatchActivity.class);
            startActivity(intent);
        }
    }
}
