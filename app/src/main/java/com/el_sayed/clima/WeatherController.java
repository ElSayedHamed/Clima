package com.el_sayed.clima;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.nio.file.Files;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity {
    // Request Codes:
    final int REQUEST_CODE = 123; // Request Code for permission request callback
    final int NEW_CITY_CODE = 456; // Request code for starting new activity for result callback

    // Base URL for the OpenWeatherMap API. More secure https is a premium feature =(
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";

    // App ID to use OpenWeather data
    final String APP_ID = "e72ca729af228beabd5d20e3b7749713";

    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;

    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // Don't want to type 'Clima' in all the logs, so putting this in a constant here.
    final String LOGCAT_TAG = "Clima";

    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    boolean mUseLocation = true;
    TextView mTemperatre;
    TextView mCity ;
    ImageView mCondition;
    JSONObject responseOnPause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

         mTemperatre = (TextView) findViewById(R.id.tempTV);
        mCity = (TextView) findViewById(R.id.locationTV);
        mCondition = (ImageView) findViewById(R.id.weatherSymbolIV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(WeatherController.this,ChangeCityController.class);
                startActivity(myIntent);
            }
        });
    }

    @Override
    protected void onPause ()
    {
        super.onPause();

        if (mLocationManager !=null) mLocationManager.removeUpdates(mLocationListener);

    }
    @Override
    protected  void onResume()
    {
        super.onResume();
        Log.d("Clima", "onResume() called");

        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        if (!city.isEmpty())
        {
            Log.d("Clima", "Getting weather for New City: " + city);

            getWeatherForNewCity(city);
        }else {
            Log.d("Clima", "Getting weather for current location");

            getWeatherForCurrentLocation();
        }
    }


    private void getWeatherForNewCity(String city)
    {
        RequestParams params = new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsDoSomeNetworking(params);
    }

    private void getWeatherForCurrentLocation()
    {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d("Clima", "onLocationChanged() callback received");
                String Longitude = String.valueOf(location.getLongitude());
                String Latitude = String.valueOf(location.getLatitude());

                Log.d("Clima", "Longitude is: "+Longitude);
                Log.d("Clima", "Latitude is: "+Latitude);

                RequestParams params = new RequestParams();
                params.put("lat",Latitude);
                params.put("lon",Longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);

            }



            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Log.d("Clima", "onProviderDisabled() callback received");
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    private void letsDoSomeNetworking(RequestParams params) {

        AsyncHttpClient client =new AsyncHttpClient();
        client.get(WEATHER_URL,params,new JsonHttpResponseHandler()
        {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                responseOnPause = response;
                Log.d("Clima","Success! Json: "+response.toString());

                WeatherDataModel weatherData = new WeatherDataModel().fromJson(response);
                updateUI(weatherData);
            }
            @Override
            public void onFailure (int statusCode, Header[] headers,Throwable e, JSONObject response)
            {
                Log.d("Clima","Fail: "+e.toString() );
                Log.d("Clima","statusCode: "+statusCode);
                Toast.makeText(WeatherController.this,"Request Failed",Toast.LENGTH_SHORT).show();


            }

        });
    }

    private void updateUI (WeatherDataModel weather)
    {


        mTemperatre.setText(weather.getTemperature());
        mCity.setText(weather.getCity());

        int resourceID = getResources().getIdentifier(weather.getIconName(),"drawable",getPackageName());
        mCondition.setImageResource(resourceID);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==REQUEST_CODE)
        {
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                Log.d("Clima","Permission Granted");
                getWeatherForCurrentLocation();
            }
            else
            {
                Log.d("Clima","Permission Denied");
            }

        }

    }
}
