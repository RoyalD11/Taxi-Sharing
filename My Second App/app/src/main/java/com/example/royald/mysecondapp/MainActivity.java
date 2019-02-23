package com.example.royald.mysecondapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.royald.mysecondapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /* called when the user taps Send*/
    public void login(View view){
        Intent intent = new Intent(this, PreMatchActivity.class);
        EditText editText = (EditText) findViewById(R.id.EmailBox);
        startActivity(intent);

    }

    //Called when user hits sign up button
    public void toSignUpScreen(View view){
        Intent intent = new Intent(this, signUpScreen.class);
        startActivity(intent);
    }

    //Called when user hits forgot password button
    public void toForgotPasswordScreen(View view){
        Intent intent = new Intent(this, forgotPasswordActivity.class);
        startActivity(intent);
    }
}
