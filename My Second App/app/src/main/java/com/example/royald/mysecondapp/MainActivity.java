package com.example.royald.mysecondapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.royald.mysecondapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /* called when the user taps Send*/
    public void login(View view){
        EditText email = (EditText) findViewById(R.id.EmailBox);
        EditText password = (EditText) findViewById(R.id.PasswordBox);

        //Checks if the required field is empty and if a valid email is entered.
        if(TextUtils.isEmpty(email.getText())) email.setError("Email is required!");
        else if(TextUtils.isEmpty(password.getText())) password.setError("Password is Required!");
        else {
           Intent intent = new Intent(this, PreMatchActivity.class);
            startActivity(intent);
        }

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
