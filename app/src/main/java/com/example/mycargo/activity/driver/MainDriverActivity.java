package com.example.mycargo.activity.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andremion.counterfab.CounterFab;
import com.example.mycargo.LoginActivity;
import com.example.mycargo.R;
import com.example.mycargo.app.Config;
import com.example.mycargo.util.Utility;
import com.example.mycargo.util.SendLocationToAllActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.kamil.locationbackground.LoactionBackground;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;

public class MainDriverActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    BitmapDrawable icon ;
    Bitmap smallMarke;

    private Context context = this;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private LocationManager locationManager;
    private int index, next;
    private String latitude;
    private String longitude;
    private LatLng latLng;
    private Marker mCurrLocationMarker;
    public static final String TAG = "Fused";
    private GoogleMap mMap;
    private Handler handler;

    CounterFab order_list;
    String mUsername;
    String mId_number;
    String mTruck_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_driver);
        getDataIntent();
        askLocationPermission();
        LinearLayout btn_logOut = (LinearLayout) findViewById(R.id.wrap_drv_name);
        btn_logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmLogoutDialog(context,"Logout","Are you sure want to logout?");
            }
        });

        LinearLayout menu_oders = (LinearLayout) findViewById(R.id.menu_oders);
        menu_oders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainDriverActivity.this, OrderListActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout menu_history = (LinearLayout) findViewById(R.id.menu_history);
        menu_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainDriverActivity.this, OrderHistoryActivity.class);
                startActivity(intent);
            }
        });

        order_list = (CounterFab) findViewById(R.id.orderStart);
        order_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainDriverActivity.this, StartOrderActivity.class);
                startActivity(intent);
            }
        });

        //icon marker
        int width = 100;
        int height = 70;
        icon = (BitmapDrawable)getResources().getDrawable(R.drawable.logo_troboss);
        Bitmap b = icon.getBitmap();
        smallMarke = Bitmap.createScaledBitmap(b, width, height, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Lib test
        System.out.println("--- location background : " + LoactionBackground.test("D"));
    }

    private void getDataIntent() {
        try {
            //get session data
            SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
            mUsername =settings.getString("username","");
            mId_number =settings.getString("idNumber","");
            mTruck_no =settings.getString("truckNo","");

            //set name driver
            TextView drv_name = (TextView) findViewById(R.id.drv_name);
            drv_name.setText(mUsername);


            //truck license confirmation
            String login_yn = settings.getString("login_yn","");
            if (login_yn != "Y"){
                licensePlateConfirtmation();
            }
        } catch (Exception e) {
            Utility.showErrorDialog(context, "Notification", getString(R.string.error_connection));
        }
    }

    public void showConfirmLogoutDialog(Context context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do your job here
                        SharedPreferences.Editor editor = getSharedPreferences(Config.SHARED_PREF, 0).edit();
                        editor.clear().commit();
                        Intent intentLogin = new Intent(MainDriverActivity.this, LoginActivity.class);
                        startActivity(intentLogin);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private void licensePlateConfirtmation(){
        SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
        String truckNo=settings.getString("truckNo","");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainDriverActivity.this, R.style.AlertDialogStyle);
        alertDialogBuilder.setTitle("Confirmation");
        alertDialogBuilder
                .setMessage("Are you sure to using truck " + truckNo + " ?")
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = getSharedPreferences(Config.SHARED_PREF, 0).edit();
                        editor.clear().commit();

                        getIntent().removeExtra("data");
                        getIntent().removeExtra("username");

                        Intent intentLogin = new Intent(MainDriverActivity.this, LoginActivity.class);
                        startActivity(intentLogin);
                        finish();
                        dialog.cancel();
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("login_yn","Y");
                        editor.commit();
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    //map section
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        System.out.println("onMapReady 1");

//        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMinZoomPreference(18.0f);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            System.out.println("onMapReady 2");
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                System.out.println("onMapReady 3");
//                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            System.out.println("onMapReady 4");
//            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.clear();
    }

    private void buildGoogleApiClient() {
        System.out.println("buildGoogleApiClient 1");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void askLocationPermission() {
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                if (ActivityCompat.checkSelfPermission(getBaseContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("onPermissionGranted 1");
                    return;
                }
                buildGoogleApiClient();
                System.out.println("onPermissionGranted 2");
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();

            }
        }).check();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("map connect");
        Log.d(TAG, "Connected");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        System.out.println("ACCESS_FINE_LOCATION == " + ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION));
        System.out.println("PERMISSION_GRANTED == " + PackageManager.PERMISSION_GRANTED);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            System.out.println(" kesini gak yaa  == ");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            Log.e("DB", "PERMISSION GRANTED");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent intent = new Intent(this, LocationService.class);
        System.out.println("stop service");
//        stopService(intent);
//        if(mGoogleApiClient!=null) {
//            System.out.println("Start service");
//            mLocationRequest = new LocationRequest();
//            mLocationRequest.setInterval(10000);
//            mLocationRequest.setFastestInterval(10000);
//            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            if (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//            }
//        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());

        //for send location to all activity
        EventBus.getDefault().postSticky(new SendLocationToAllActivity(location));

        setLocation(latLng);
        System.out.println("--- Main activity Latitude == " + latitude + " Longitude ==="+ longitude);
    }

    private void setLocation(LatLng latLng) {
        if (mCurrLocationMarker == null) {
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(latLng);
//            markerOptions.title("Your Location");
//            mCurrLocationMarker = mMap.addMarker(markerOptions);
//            mCurrLocationMarker.showInfoWindow();
            //move map camera
            System.out.println("change loc 1 " + latLng.latitude + " " + latLng.longitude);
//            mCurrLocationMarker.setPosition(latLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            System.out.println("zoom level 16");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        } else {
            mCurrLocationMarker.setPosition(latLng);
            System.out.println("change loc 2 " + latLng.latitude + " " + latLng.longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }
    //map section end

}
