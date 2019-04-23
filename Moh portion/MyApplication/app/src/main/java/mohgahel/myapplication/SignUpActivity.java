package mohgahel.myapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private Button confirmButton;
    private EditText email, password, firstName, phone, car, plate;
    private String driverEmail, driverPassword, driverID, driverName, driverPhone, driverCar, driverPlate;
    private ImageView profilePic;
    private Uri resultUri;
    private FirebaseAuth auth;
    private DatabaseReference driverDatabase;
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

        profilePic = (ImageView) findViewById(R.id.driverProfile);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        confirmButton = (Button) findViewById(R.id.confirmSignUp);

        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                email = (EditText) findViewById(R.id.email);
                password = (EditText) findViewById(R.id.password);
                driverEmail = email.getText().toString();
                driverPassword = password.getText().toString();


                firstName = findViewById(R.id.firstName);
                EditText confirmEmail = findViewById(R.id.confirmEmail);
                EditText confirmPass = findViewById(R.id.confirmPassword);
                phone = findViewById(R.id.phone);
                car = findViewById(R.id.car);
                plate = findViewById(R.id.plate);

                //driverID = auth.getCurrentUser().getUid();
                //driverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("driverID");

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
                                driverID = auth.getCurrentUser().getUid();
                                DatabaseReference DriverDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID);
                                driverName = firstName.getText().toString();
                                driverPhone = phone.getText().toString();
                                driverCar = car.getText().toString();
                                driverPlate = plate.getText().toString();

                                Map drivers = new HashMap();
                                drivers.put("Name", driverName);
                                drivers.put("Email", driverEmail);
                                drivers.put("Phone Number", driverPhone);
                                drivers.put("Car Make", driverCar);
                                drivers.put("License Plate", driverPlate);
                                drivers.put("Rating", 0.0f);
                                DriverDb.setValue(drivers);

                                if(resultUri != null){
                                    final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(driverID);
                                    Bitmap bitmap = null;

                                    try {
                                        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                                    }
                                    catch (IOException e){
                                        e.printStackTrace();
                                    }

                                    ByteArrayOutputStream boas = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, boas);
                                    byte[] data = boas.toByteArray();
                                    UploadTask uploadTask = filePath.putBytes(data);

                                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                            if(!task.isSuccessful()){
                                                throw task.getException();
                                            }
                                            return filePath.getDownloadUrl();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()){
                                                Uri downloadUrl = task.getResult();

                                                DatabaseReference myDatabaseImage = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID);

                                                Map imageMap = new HashMap();

                                                imageMap.put("profileImageUrl", downloadUrl.toString());

                                                myDatabaseImage.updateChildren(imageMap);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profilePic.setImageURI(resultUri);
        }
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
