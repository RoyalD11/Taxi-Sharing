package com.example.royald.mysecondapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.royald.mysecondapp.MESSAGE";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        //If user provides valid log in information this will log them into the app and change the screen
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(MainActivity.this, PreMatchActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };
    }

    /* called when the user taps Send*/
    public void login(View view){
        EditText email = (EditText) findViewById(R.id.EmailBox);
        EditText password = (EditText) findViewById(R.id.PasswordBox);

        final String userEmail = email.getText().toString();
        final String userPassword = password.getText().toString();

        //Checks if the required field is empty and if a valid email is entered.
        if(TextUtils.isEmpty(email.getText())) email.setError("Email is required!");
        else if(TextUtils.isEmpty(password.getText())) password.setError("Password is Required!");

        //Checks the database to see if the username and pass provided is valid, displays an error if it is not valid, sends a signal to onAuthStateChanged if valid
        else {
           mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
                   if(!task.isSuccessful()){
                       Toast.makeText(MainActivity.this, "Sign In Error", Toast.LENGTH_SHORT).show();
                   }
               }
           });
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
