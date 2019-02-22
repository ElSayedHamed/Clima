package com.el_sayed.clima;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChangeCityController extends AppCompatActivity {
    Intent newCityIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         newCityIntent = new Intent(ChangeCityController.this,WeatherController.class);
        ;
        setContentView(R.layout.change_city_layout);
        final EditText  editTextField = (EditText) findViewById(R.id.queryET);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String newCity = editTextField.getText().toString();
//                newCityIntent.putExtra("CityOnBack",newCity);

                finish();
            }
        });

        editTextField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {


                String newCity = editTextField.getText().toString();

                newCityIntent.putExtra("City",newCity.trim());
                startActivity(newCityIntent);

                return false;
            }
        });
    }

}
