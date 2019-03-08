package com.example.royald.mysecondapp;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class forgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    public void submitButton(View view){

        EditText email = (EditText) findViewById(R.id.forgotPasswordEmailInput);

        //The email field needs to be populated in order to press the button.
        if(TextUtils.isEmpty(email.getText())) email.setError("Email is required!");
        else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
