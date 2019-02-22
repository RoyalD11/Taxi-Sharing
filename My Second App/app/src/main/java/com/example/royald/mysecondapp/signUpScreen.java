package com.example.royald.mysecondapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class signUpScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); //Setting so that the screen focuses the input field you are currently on

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
}
