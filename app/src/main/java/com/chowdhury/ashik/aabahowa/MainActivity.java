package com.chowdhury.ashik.aabahowa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient client;
//    private TextView latlonTV;
    private TabLayout tabLayout;
    private Geocoder geocoder;
    private ViewPager viewPager;
    private CurrentWeatherFragment currentWeatherFragment;
    private ForecastWeatherFragment forecastWeatherFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        latlonTV = findViewById(R.id.latlonTV);
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);
        currentWeatherFragment = new CurrentWeatherFragment();
        forecastWeatherFragment = new ForecastWeatherFragment();
        tabLayout.addTab(tabLayout.newTab().setText("Current"));
        tabLayout.addTab(tabLayout.newTab().setText("Forecast"));
        tabLayout.setTabTextColors(Color.GRAY,Color.WHITE);
        tabLayout.setSelectedTabIndicatorColor(Color.GREEN);
        geocoder = new Geocoder(this, Locale.getDefault());
        client = LocationServices.getFusedLocationProviderClient(this);
        WeatherPagerAdapter pagerAdapter = new WeatherPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
//        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()){
            getDeviceLastLocation();
        }
    }

    private boolean checkLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
       if (ActivityCompat.checkSelfPermission(this,
               Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
           ActivityCompat.requestPermissions(this, permissions, 111);
           return false;
       }
       return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getDeviceLastLocation();
        }else{
            //explaion why this permission is important
        }
    }
    private class WeatherPagerAdapter extends FragmentPagerAdapter {
        public WeatherPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return currentWeatherFragment;
                case 1:
                    return forecastWeatherFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private void convertGeolocationToStreet(double lat, double lon) throws IOException{
        List<Address> addressList = geocoder.getFromLocation(lat, lon, 1);
        final Address address = addressList.get(0);
        String addressLine = address.getAddressLine(0);
        currentWeatherFragment.onLocationRecived(lat,lon,addressLine);
        forecastWeatherFragment.onLocationRecived(lat,lon,addressLine);
//        latlonTV.setText(lat+","+lon+"\n"+addressLine);
    }

    private void getDeviceLastLocation(){
        if (checkLocationPermission()){
            client.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        try {
                            convertGeolocationToStreet(lat, lon);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        latlonTV.setText(lat+","+lon);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("failed", e.getLocalizedMessage() );
                }
            });
        }
    }
}
