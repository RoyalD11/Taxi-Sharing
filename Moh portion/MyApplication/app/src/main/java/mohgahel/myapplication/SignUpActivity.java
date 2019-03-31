package mohgahel.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseRegistrar;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private Button continueSign;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener fireBaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        fireBaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(SignUpActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        continueSign = (Button) findViewById(R.id.continueSignUp);

        continueSign.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                EditText email = (EditText) findViewById(R.id.email);
                EditText password = (EditText) findViewById(R.id.pass);
                final String driverEmail = email.getText().toString();
                final String driverPassword = password.getText().toString();


                EditText firstName = findViewById(R.id.firstName);
                EditText lastName = findViewById(R.id.lastName);
                //EditText email = findViewById(R.id.email);
                EditText confirmEmail = findViewById(R.id.confirmEmail);
                //EditText pass = findViewById(R.id.pass);
                EditText confirmPass = findViewById(R.id.confirmPass);
                EditText phone = findViewById(R.id.phone);
                EditText city = findViewById(R.id.city);

                if(TextUtils.isEmpty(email.getText())) email.setError("Email is required.");
                else if (TextUtils.isEmpty(password.getText())) password.setError("Password is required.");
                else{
                    auth.createUserWithEmailAndPassword(driverEmail, driverPassword).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(SignUpActivity.this, "SignUp unsuccessful.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String driverId = auth.getCurrentUser().getUid();
                                DatabaseReference currentDriverDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
                                currentDriverDb.setValue(true);
                            }
                        }
                    });
                }
            }
        });


    }

    public void carInfo(View view){


        /*if( TextUtils.isEmpty(firstName.getText())) {
            firstName.setError( "First name is required!" );
        }
        else if( TextUtils.isEmpty(lastName.getText())) {
            lastName.setError( "Last name is required!" );
        }
        else if( TextUtils.isEmpty(email.getText())) {
            email.setError( "Email is required!" );
        }
        else if( TextUtils.isEmpty(confirmEmail.getText()) || !(TextUtils.equals(email.getText(), confirmEmail.getText()))) {
            confirmEmail.setError( "Please confirm email!" );
        }
        else if( TextUtils.isEmpty(pass.getText())) {
            pass.setError( "Password is required!" );
        }
        else if( TextUtils.isEmpty(confirmPass.getText()) || !(TextUtils.equals(pass.getText(), confirmPass.getText()))) {
            confirmPass.setError("Please confirm password!");
        }
        else if( TextUtils.isEmpty(phone.getText())) {
            phone.setError( "Phone number is required!" );
        }
        else if( TextUtils.isEmpty(city.getText())) {
            city.setError( "City is required!" );
        }
        else {*/




            //Intent intent = new Intent(this, CarInfoActivity.class);
            //startActivity(intent);
        //}
    }
    @Override
    protected void onStart(){
        super.onStart();
        auth.addAuthStateListener(fireBaseAuthListener);
    }
    @Override
    protected void onStop(){
        super.onStop();
        auth.removeAuthStateListener(fireBaseAuthListener);
    }

}
