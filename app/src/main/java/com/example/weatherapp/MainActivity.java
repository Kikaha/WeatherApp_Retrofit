package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.weather_model.WeatherModel;
import com.example.weatherapp.weather_model.WeatherModelInterface;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public String getTempBroadcast;
    private boolean locationPermisionGranted = false;
    private LocationManager locationManager;
    private final int permission_location_request_code = 12345679;
    private final static int REQUEST_LOCATION = 1;
    private double latti;
    private double longi;
    private ActivityMainBinding binding;
    private String displayTime;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RemainderBroadcast remainder = new RemainderBroadcast();
        remainder.setData(MainActivity.this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        createNotificationChannel();
        setTime();
        getCoordinates();
        setUpRetrofit();
        setUpBackGround();
        setDaysOfWeek();
        binding.setNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"notifications activated",Toast.LENGTH_SHORT).show();
                setNotification();
            }
        });
        binding.dellNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                manager.cancel(pendingIntent);
                Toast.makeText(MainActivity.this,"Delete notifications",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setNotification() {
        Intent intent = new Intent(MainActivity.this,RemainderBroadcast.class);
        intent.putExtra("tempINfo",binding.third.getText().toString());
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int interval = 10;
//        alarmManager.set(AlarmManager.RTC_WAKEUP,currentTime+timeLapsNotification, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
            NotificationChannel channel = new NotificationChannel("ID1", "Chanel_Only", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("weatherForecast");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
    }

    private void setDaysOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        switch (currentDay) {
            case 1:
                binding.eight.setText("Sunday");
                binding.eleventh.setText("Monday");
                break;
            case 2:
                binding.eight.setText("Monday");
                binding.eleventh.setText("Tuesday");
                break;
            case 3:
                binding.eight.setText("Tuesday");
                binding.eleventh.setText("Wednesday");
                break;
            case 4:
                binding.eight.setText("Wednesday");
                binding.eleventh.setText("Thursday");
                break;
            case 5:
                binding.eight.setText("Thursday");
                binding.eleventh.setText("Friday");
                break;
            case 6:
                binding.eight.setText("Friday");
                binding.eleventh.setText("Saturday");
                break;
            case 7:
                binding.eight.setText("Saturday");
                binding.eleventh.setText("Sunday");
                break;
            default:
                System.out.println("Invalid");
                break;
        }
    }

    private void setUpBackGround() {
//        String temp1 = "10 10 AM";
//       List<String> temp = Arrays.stream(temp1.split("[^\\w]+")).collect(Collectors.toList());
        List<String> timeNow = Arrays.stream(displayTime.split("[^\\w]+")).collect(Collectors.toList());
        Integer hour = Integer.parseInt(timeNow.get(0));
        if ((Integer.parseInt(timeNow.get(0)) >= 8 && timeNow.get(2).equals("PM")) ||
                ((Integer.parseInt(timeNow.get(0)) <= 6 && timeNow.get(2).equals("AM")))) {
            binding.base.setBackgroundResource(R.drawable.gradien_evening);
            binding.fifth.setImageResource(R.drawable.moon1);
        } else {
            binding.base.setBackgroundResource(R.drawable.gradient_morning);
            binding.fifth.setImageResource(R.drawable.solar3);
        }

    }

    private void setTime() {
//        Calendar date = Calendar.getInstance();
//        Formatter ft = new Formatter();
//        String currentTime = ft.format("%tl:%tM",date,date).toString();
//        binding.shadow1.setText(ft.format("%tl:%tM",date,date).toString());
        LocalTime localTime = LocalTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        displayTime = localTime.format(dateTimeFormatter);
        LocalDate dateOfWeek = LocalDate.now();
        String dayOfTheWeek = dateOfWeek.toString();
        binding.shadow1.setText(dayOfTheWeek + " " + displayTime);
    }

    private void setUpRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        double lat = latti;
        double lon = longi;
        WeatherModelInterface forecastWeather = retrofit.create(WeatherModelInterface.class);
        Call<WeatherModel> call = forecastWeather.
                getWeatherForecast(lat, lon, 3, "metric", "1a9596f56ea1572fb28b937c185fce82");
        call.enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "We have a response!", Toast.LENGTH_SHORT).show();
                    WeatherModel model = response.body();
                    List<String> cityName = Arrays.asList(model.getCity().getName().split(" "));
                    String cityName0 = cityName.get(0);
                    binding.fourth.setText(cityName0);
                    float cnt = model.getCNT();
                    ArrayList<Object> lists = model.getList();
                    getBaseTemp(lists);
                    getTomorrowTemp(lists);
                    getDayAfterTommorowTemp(lists);
                }
            }

            private void getDayAfterTommorowTemp(ArrayList<Object> lists) {
                Map<String, LinkedHashMap> data = (Map<String, LinkedHashMap>) lists.get(2);
                Map.Entry<String, LinkedHashMap> currentTemp = data.entrySet().stream().filter(x -> x.getKey().equals("main")).findFirst().get();
                Map<String, LinkedTreeMap> currentTemp1 = currentTemp.getValue();
                Map.Entry<String, LinkedTreeMap> currentTemp2 = currentTemp1.entrySet().stream().filter(x -> x.getKey().equals("temp")).findFirst().get();
                String currentTemp3 = String.valueOf(currentTemp2.getValue());
                binding.twelfth.setText(String.format("%.1f %s", Double.parseDouble(currentTemp3), "\u2103"));
            }

            private void getTomorrowTemp(ArrayList<Object> lists) {
                Map<String, LinkedHashMap> data = (Map<String, LinkedHashMap>) lists.get(1);
                Map.Entry<String, LinkedHashMap> currentTemp = data.entrySet().stream().filter(x -> x.getKey().equals("main")).findFirst().get();
                Map<String, LinkedTreeMap> currentTemp1 = currentTemp.getValue();
                Map.Entry<String, LinkedTreeMap> currentTemp2 = currentTemp1.entrySet().stream().filter(x -> x.getKey().equals("temp")).findFirst().get();
                String currentTemp3 = String.valueOf(currentTemp2.getValue());
                binding.ninth.setText(String.format("%.1f %s", Double.parseDouble(currentTemp3), "\u2103"));
            }

            private void getBaseTemp(ArrayList<Object> lists) {
                Map<String, LinkedHashMap> data = (Map<String, LinkedHashMap>) lists.get(0);
                Map.Entry<String, LinkedHashMap> currentTemp = data.entrySet().stream().filter(x -> x.getKey().equals("main")).findFirst().get();
                Map<String, LinkedTreeMap> currentTemp1 = currentTemp.getValue();
                Map.Entry<String, LinkedTreeMap> currentTemp2 = currentTemp1.entrySet().stream().filter(x -> x.getKey().equals("feels_like")).findFirst().get();
                String currentTemp3 = String.format("%.1f", currentTemp2.getValue());
                binding.third.setText(currentTemp3 + " \u2103");
                getTempBroadcast = currentTemp3 + " \u2103";
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {

            }
        });

    }

    private void getCoordinates() {
        Log.d(TAG, "getLocationPermissions: Get permissions");
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED) {
                locationPermisionGranted = true;
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    getLatLon();
                }
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, permissions, permission_location_request_code);
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, permission_location_request_code);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == permission_location_request_code) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "grantResult permission failed");
                    return;
                }
            }
            locationPermisionGranted = true;
            Log.d(TAG, "permissions are OK");
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                getLatLon();
            }
        }
    }

    private void getLatLon() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    , REQUEST_LOCATION);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                latti = location.getLatitude();
                longi = location.getLongitude();
            } else if (location1 != null) {
                latti = location1.getLatitude();
                longi = location1.getLongitude();
            } else if (location2 != null) {
                latti = location2.getLatitude();
                longi = location2.getLongitude();
            } else {
                Toast.makeText(this, "Can't obtain coordinates", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
