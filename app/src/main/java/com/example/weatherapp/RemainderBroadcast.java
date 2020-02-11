package com.example.weatherapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;


public class RemainderBroadcast extends BroadcastReceiver {
    private MainActivity mainActivity;

    public void setData(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Notification.Builder builder = new Notification.Builder(context,"notifyWeather");
//        String extraTempInfo = intent.getStringExtra("tempINfo");
        builder.setSmallIcon(R.drawable.icon)
                .setContentTitle("CurrentTemp")
                .setContentText(intent.getStringExtra("tempINfo"))
                .setChannelId("ID1")
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(200,builder.build());
    }
}
