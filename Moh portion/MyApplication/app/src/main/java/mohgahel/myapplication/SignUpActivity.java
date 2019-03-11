package mohgahel.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void carInfo(View view){
        EditText firstName = findViewById(R.id.firstName);
        EditText lastName = findViewById(R.id.lastName);
        EditText email = findViewById(R.id.email);
        EditText confirmEmail = findViewById(R.id.confirmEmail);
        EditText pass = findViewById(R.id.pass);
        EditText confirmPass = findViewById(R.id.confirmPass);
        EditText phone = findViewById(R.id.phone);
        EditText city = findViewById(R.id.city);

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
            Intent intent = new Intent(this, CarInfoActivity.class);
            startActivity(intent);
        //}
    }
}
