package com.example.mycargo.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mycargo.LoginActivity;
import com.example.mycargo.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static com.example.mycargo.activity.driver.MainDriverActivity.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            System.out.println("data send " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            System.out.println("data send " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        sendNotification(remoteMessage);

    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String content = data.get("content");


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), LoginActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "EDMTDev";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Only active on android Oreo or heigher because it need Notification channel
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "EDMT Notification", NotificationManager.IMPORTANCE_MAX);

            //configure the notification channel
            notificationChannel.setDescription("EDMT channel for app test FCM");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManagerCompat.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_logo_toboss_for_clique)
                .setTicker("Hearty365")
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentInfo("info");
        notificationManagerCompat.notify(1,notificationBuilder.build());
//        NotificationCompat.Builder builder = new NotificationCompat.Builder()

    }
}
