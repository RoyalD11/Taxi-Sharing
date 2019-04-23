package com.example.royald.mysecondapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
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

    //Databse variables used for authentication
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener firebaseAuthListener;

    //Variable for login button
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the instance of the authenticator in the databse
        mAuth = FirebaseAuth.getInstance();
        //mAuth.signOut(); //Used to force sign the user out if there is ever an error in the code, so the app will default to the sign in screen.

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

        //Reference the login button on the screen
        loginButton = (Button) findViewById(R.id.LoginButton);

        //Waits for the login button to be pressed
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Reference the necessary fields
                EditText email = (EditText) findViewById(R.id.EmailBox);
                EditText password = (EditText) findViewById(R.id.PasswordBox);

                //Get the information of those fields
                final String userEmail = email.getText().toString();
                final String userPassword = password.getText().toString();

                //Checks if the required field is empty and if a valid email is entered.
                if(TextUtils.isEmpty(email.getText())|| !isEmailValid(email.getText().toString()))
                    email.setError("Valid email is required!");

                else if(TextUtils.isEmpty(password.getText()))
                    password.setError("Password is Required!");

                //Checks the database to see if the username and pass provided is valid, displays an error if it is not valid, sends a signal to onAuthStateChanged if valid
                else {

                    //Attempt to sign in with provided email and password
                    mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //If sign in fails post a toast to the screen
                            if(!task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Sign In Error", Toast.LENGTH_SHORT).show();
                                task.getException().printStackTrace();
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
