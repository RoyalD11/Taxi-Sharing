package mohgahel.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class CarInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);
        Spinner yearSpinner = (Spinner) findViewById(R.id.carYear);
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this, R.array.year_spinner, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
    }
    public void createAccount(View view){



        EditText make = findViewById(R.id.make);
        EditText model = findViewById(R.id.model);
        EditText plate = findViewById(R.id.plate);
        EditText color = findViewById(R.id.color);

        /*if( TextUtils.isEmpty(make.getText())) {
            make.setError( "Car make is required!" );
        }
        else if ( TextUtils.isEmpty(model.getText())) {
            model.setError( "Car model is required!" );
        }
        else if ( TextUtils.isEmpty(plate.getText())) {
            plate.setError( "Car plate is required!" );
        }
        else if ( TextUtils.isEmpty(color.getText())) {
            color.setError( "Car color is required!" );
        }
        else{*/
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        //}
    }

}
