package com.example.mycargo.Locationbackground;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mycargo.R;
import com.example.mycargo.activity.driver.MainDriverActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

public class MyBackgroundService extends Service {

    private static final String CHANNEL_ID = "my_channel";
    private static final String EXTRA_START_FROM_NOTIFICATION = "com.example.locationbackground.started_from_notification" ;

    private final IBinder mBinder = new LocalBinder();
    private static final long UPDATE_INTERVAL_IN_MIL = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MUL = UPDATE_INTERVAL_IN_MIL/2;
    private static final int NOTI_ID = 1223;
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;

    private LocationRequest locationRequest;
    private LocationResult locationResult;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler mServiceHandler;
    private Location mLocation;

    public MyBackgroundService(){

    }

    @Override
    public void onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocationResult(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread("KAMIL");
        handlerThread.start();

        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_START_FROM_NOTIFICATION, false);
        if (startedFromNotification) {
            removeLocationUpdate();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    public void removeLocationUpdate() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Common.setRequstingLocationUpdate(this, false);
            stopSelf();
        } catch (SecurityException ex) {
            Common.setRequstingLocationUpdate(this, true);
            Log.e("ERROR 03", "Loc location permission.Clould not remove updates. " + ex);
        }
    }

    private void getLastLocation() {
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.e("ERROR 01","Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException ex) {
            Log.e("ERROR 02", "Lost location permission. " + ex);
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MIL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MUL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void onNewLocationResult(Location lastLocation) {
        mLocation = lastLocation;
        EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));

        //Update notification content if running as a foreground service
        if (serviceRunningInForeGround(this)){
            mNotificationManager.notify(NOTI_ID, getNotification());
        }
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, MyBackgroundService.class);
        String text = Common.getLocationText(mLocation);

        intent.putExtra(EXTRA_START_FROM_NOTIFICATION, true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainDriverActivity.class),0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .addAction(R.drawable.ic_access_alarms_24dp, "Launch", activityPendingIntent)
                .addAction(R.drawable.ic_remove_circle_outline_24dp, "Remove", servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Common.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MIN)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setVibrate(null)
                .setSound(null)
                .setWhen(System.currentTimeMillis());

        //set the channel id for android o
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setPriority(NotificationManager.IMPORTANCE_LOW);
            builder.setPriority(Notification.PRIORITY_MIN);
            builder.setChannelId(CHANNEL_ID).setSound(null).setVibrate(null);
        }
        return builder.build();
    }

    private boolean serviceRunningInForeGround(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service:manager.getRunningServices(Integer.MAX_VALUE)){
            if (getClass().getName().equals(service.service.getClassName())){
                if (service.foreground){
                    return true;
                }
                //return false;
            }
        }
        return false;
    }

    public void requestLocationUpdates() {
        Common.setRequstingLocationUpdate(this, true);
        startService(new Intent(getApplicationContext(), MyBackgroundService.class));
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        } catch (SecurityException ex){
            Log.e("ERROR 9999" , "Lost location permission.Could not request it " + ex);
        }
    }

    public class LocalBinder extends Binder {
        public MyBackgroundService getService(){
            return MyBackgroundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!mChangingConfiguration && Common.requstingLocationUpdate(this))
            startForeground(NOTI_ID, getNotification());
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacks(null);
        super.onDestroy();
    }
}
