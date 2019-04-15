package mohgahel.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    private Button login;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener fireBaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        auth.signOut();
        fireBaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        login = (Button) findViewById(R.id.loginButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText email = (EditText) findViewById(R.id.confirmEmail);
                EditText password = (EditText) findViewById(R.id.password);
                final String driverEmail = email.getText().toString();
                final String driverPassword = password.getText().toString();

                if(TextUtils.isEmpty(email.getText())) email.setError("Email is required.");
                else if (TextUtils.isEmpty(password.getText())) password.setError("Password is required.");
                else{
                    auth.signInWithEmailAndPassword(driverEmail, driverPassword).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Login unsuccessful.", Toast.LENGTH_SHORT).show();
                                task.getException().printStackTrace();
                            }
                        }
                    });
                }
            }
        });

    }

    public void signUpScreen(View view){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);

    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(fireBaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(fireBaseAuthListener);
    }
}
