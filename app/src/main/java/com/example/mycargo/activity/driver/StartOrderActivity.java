package com.example.mycargo.activity.driver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycargo.Locationbackground.Common;
import com.example.mycargo.Locationbackground.MyBackgroundService;
import com.example.mycargo.Locationbackground.SendLocationToActivity;
import com.example.mycargo.R;
import com.example.mycargo.adapter.StartOrderAdapter;
import com.example.mycargo.app.Config;
import com.example.mycargo.dialog.JobQRDialog;
import com.example.mycargo.util.CallWS;
import com.example.mycargo.util.SendAndReceiveJSON;
import com.example.mycargo.util.SendLocationToAllActivity;
import com.example.mycargo.util.Utility;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.zxing.WriterException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.List;

import static com.example.mycargo.util.Utility.openGoogleMapsLocation;

public class StartOrderActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        LocationListener {
    //RECYCLER VIEW
    RecyclerView mRecyclerView;
    StartOrderAdapter mStartOrderAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayout mIl_no_data;
    private LinearLayout mWrap_content;
    private TextView mJob_no;
    private TextView mJob_type;
    private TextView mAssign_at;
    private TextView mUser_type_from;
    private TextView mUser_type_to;
    private TextView mUser_from;
    private TextView mUser_to;
    private Button mConfirmOrder;
    private ImageView mQrCode;
    private Button mDirection;
    private String mPortName;
    private double mFromLatitude;
    private double mFromLongitude;
    private double mPortLatitude;
    private double mPortLongitude;
    private boolean mIsReqBgLocation = false; // if background location is on

    String[] DATE;
    String[] CTR_NO;
    String[] STATUSMSG;
    String[] STATUSCD;
    String[] ORDER_NO;
    String[] ORDER_TYPE;
    String[] CONTAINER_KEY;
    private JSONArray array_order_list;
    int jumlah_data= 0;

    String mUsername;
    String mId_number;
    String mTruck_no;
    String mData_job_no;
    Context context = this;


    private FragmentManager fm = getSupportFragmentManager();

    //location background variable
    MyBackgroundService mService = null;
    boolean mBound = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            MyBackgroundService.LocalBinder binder = (MyBackgroundService.LocalBinder)iBinder;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_order);
        //get data intent
        getDataIntent();
        //get data order
        getDataOrder();

        //inisialitaion
        mJob_no = findViewById(R.id.tv_job_no);
        mJob_type = findViewById(R.id.tv_job_type);
        mAssign_at = findViewById(R.id.tv_assign_at);
        mUser_type_from = findViewById(R.id.tv_user_type_from);
        mUser_type_to = findViewById(R.id.tv_user_type_to);
        mUser_from = findViewById(R.id.tv_user_from);
        mUser_to = findViewById(R.id.tv_user_to);
        mConfirmOrder = findViewById(R.id.btn_confirmOrder);
        mDirection = findViewById(R.id.direction);
        mQrCode = findViewById(R.id.tv_qr_code);
        mWrap_content = findViewById(R.id.tv_wrap_content);
        mIl_no_data = findViewById(R.id.tv_wrap_noData);

        ImageView btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartOrderActivity.this, MainDriverActivity.class);
                startActivity(intent);
                finish();
            }
        });

//        mDirection.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //IF THE DIRECTION AND TURN BY TURN MAP ARE OPENED ON GOOGLE MAPS
//                //openMapsLocation();
//                //IF THE DIRECTION MAP ARE INCLUDED ON MYCARGO APP
//                Intent intent = new Intent(StartOrderActivity.this, DirectionActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        mConfirmOrder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String msg = "Please confirm if you have completed this job";
//                showConfirmDialog(context, "Confirmation ", msg, "S");
//            }
//        });

        mQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQR();
            }
        });

        //recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_list_order);

        //permission checker for location background
        Dexter.withActivity(this)
                .withPermissions(Arrays.asList(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ))
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        mDirection.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(StartOrderActivity.this, "Start Location Background", Toast.LENGTH_LONG).show();
                                //direct to google maps

                                //IF THE DIRECTION AND TURN BY TURN MAP ARE OPENED ON GOOGLE MAPS
                                openMapsLocation();
                                //IF THE DIRECTION MAP ARE INCLUDED ON MYCARGO APP
                                //Intent intent = new Intent(StartOrderActivity.this, DirectionActivity.class);
                                //startActivity(intent);
                            }
                        });

                        mConfirmOrder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String msg = "Please confirm if you have completed this job";
                                showConfirmDialog(context, "Confirmation ", msg, "S");
                                //Toast.makeText(StartOrderActivity.this, "Remove Location Background", Toast.LENGTH_LONG).show();
                                //mService.removeLocationUpdate();
                            }
                        });

                        setButtonState(Common.requstingLocationUpdate(StartOrderActivity.this));
                        bindService(new Intent(StartOrderActivity.this,
                                        MyBackgroundService.class),
                                mServiceConnection,
                                Context.BIND_AUTO_CREATE);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();
    }

    private void getDataIntent(){
        try {
            //get session data
            SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
            mUsername =settings.getString("username","");
            mId_number =settings.getString("idNumber","");
            mTruck_no =settings.getString("truckNo","");
            mPortName =settings.getString("portName","");
            mPortLatitude = Utility.toDouble(settings.getString("portLat",""));
            mPortLongitude = Utility.toDouble(settings.getString("portLong",""));
            System.out.println("data session = " + mUsername + " " + mId_number +" " + mTruck_no);
        } catch (Exception e) {
            Utility.showErrorDialog(context, "Notification", getString(R.string.error_connection));
        }
    }

    private void openMapsLocation() {
        LatLng fromLatLng = new LatLng(mFromLatitude, mFromLongitude);
        LatLng toLatLng = new LatLng(mPortLatitude, mPortLongitude);

        if (fromLatLng == null || toLatLng == null) {
            Toast.makeText(context,"Current location not found please activate GPS",Toast.LENGTH_LONG).show();
            return;
        }

        /*
        if(!mIsReqBgLocation){ //if is Request backgroud is Enable
            mService.requestLocationUpdates();
        }*/

        openGoogleMapsLocation(context, fromLatLng, toLatLng);
    }

    private void getQrCode(){
        try {
            Bitmap bitmap = Utility.encodeAsBitmap(mData_job_no, 700, 700);
            mQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void showQR() {
        JSONObject qrJson = new JSONObject();
        qrJson.put("BARCODE_NO", mData_job_no);

        JobQRDialog dialog=new JobQRDialog();
        dialog.setJsonObject(qrJson);
        dialog.show(fm,"Barcode");
    }

    private void getDataOrder() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("DRV_ID", mId_number);
            jsonObject.put("TRK_NO", mTruck_no);
            OrderTask task=new OrderTask(mUsername,"D", jsonObject);
            task.execute();
        } catch (Exception e){
            Toast.makeText(this, "Notification : Connection Refused", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("--- Start Order activity Latitude == " + location.getLatitude() + " Longitude ==="+ location.getLongitude());
    }

    public class OrderTask extends AsyncTask<Void, Void, JSONObject> {
        private final String typeOrder;
        private ProgressDialog dialog;
        private String username;
        private String idNumber;
        private JSONObject jsonSet;

        OrderTask(String username,String typeOrder, JSONObject jsonObject) {
            this.username=username;
            this.typeOrder=typeOrder;
            this.jsonSet=jsonObject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, "Get Order", "Please wait...", true);
            dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject returnjson = null;
            try {
                returnjson= SendAndReceiveJSON.downloadUrlAndGetJSONPOST(CallWS.URLMyCargo+"mycargomobile/getStartOrderDriver", jsonSet);
                return returnjson;
            } catch (Exception e) {
                Toast.makeText(context, "Notification failed getOrderTruck",Toast.LENGTH_LONG);
                return returnjson;
            }
        }

        @Override
        protected void onPostExecute(JSONObject returnjson) {
            super.onPostExecute(returnjson);
            dialog.dismiss();

            if(returnjson==null || !returnjson.get("RESCD").equals("00"))
            {
                mIl_no_data.setVisibility(View.VISIBLE);
                Toast.makeText(context, returnjson.get("RESMSG").toString(),
                        Toast.LENGTH_LONG).show();
                return;
            }

            mWrap_content.setVisibility(View.VISIBLE);
            //Maping data header
            JSONArray array_order_header = (JSONArray) returnjson.get("DATA_HEADER");
            if(array_order_header==null||array_order_header.isEmpty())
            {
                System.out.println("--- data header null");
                Toast.makeText(context, "Notification : No Data",Toast.LENGTH_LONG);
                return;
            }

            System.out.println("--- data header : " +returnjson.get("DATA_HEADER"));
            for (Object obj : array_order_header) {
                JSONObject object = (JSONObject) obj;
                mAssign_at.setText(Utility.trim(object.get("ASSIGNMENT_DATE")));
                mJob_no.setText(Utility.trim(object.get("JOB_ASSIGNMENT_NO")));
                mData_job_no = Utility.trim(object.get("JOB_ASSIGNMENT_NO"));
                //service cd desc
                String service_cd = "";
                if (Utility.trim(object.get("SERVICE_CD")).equalsIgnoreCase("EXP")) {
                    service_cd = "EXPORT";
                    mUser_type_from.setText("From");
                    mUser_type_to.setText("To");
                } else {
                    mUser_type_from.setText("To");
                    mUser_type_to.setText("From");
                    service_cd = "IMPORT";
                }
                mJob_type.setText(service_cd);
                mUser_from.setText(Utility.trim(object.get("CUSTOMER_NAME")));
            }
            mUser_to.setText(mPortName);

            //mapaing data detail
            array_order_list= (JSONArray) returnjson.get("DATA_DETAIL");
            if(array_order_list==null||array_order_list.isEmpty())
            {
                System.out.println("--- data detail null");
                Toast.makeText(context, "Notification : No Data",Toast.LENGTH_LONG);
                return;
            }

            //getQrCode
            getQrCode();

            //setadapter
            mStartOrderAdapter = new StartOrderAdapter(array_order_list);
            mRecyclerView.setAdapter(mStartOrderAdapter);
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }
    }

    //confirm dialog
    public void showConfirmDialog(Context context, String title, String message, final String flg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StartOrderActivity.this, R.style.AlertDialogStyle);
        alertDialogBuilder.setTitle("Confirmation");
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        JSONObject object = new JSONObject();
                        object.put("DRV_ID", mId_number);
                        object.put("TRK_NO", mTruck_no);

                        ConfirmTask task = new ConfirmTask(object);
                        task.execute();
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

//
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//                context);
//
//        // set title
//        alertDialogBuilder.setTitle(title);
//        alertDialogBuilder
//                .setMessage(message)
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        ConfirmTask task = new ConfirmTask(flg);
//                        task.execute();
//                    }
//                })
//                .setNegativeButton(android.R.string.no, null);
//        // create alert dialog
//        AlertDialog alertDialog = alertDialogBuilder.create();
//        // show it
//        alertDialog.show();
    }
//
    //confirm process
    public class ConfirmTask extends AsyncTask<Void, Void, JSONObject> {
        private JSONObject jsonSet;
        private ProgressDialog dialog;

        ConfirmTask(JSONObject object) {
            this.jsonSet = object;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, "Confirm Order", "Please wait...", true);
            dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject returnjson =null;
            System.out.println("---- data send confirmWorkTimeDriver = " + jsonSet);
            returnjson = SendAndReceiveJSON.downloadUrlAndGetJSONPOST(
                    CallWS.URLMyCargo + "mycargomobile/confirmWorkTimeDriver", jsonSet);
            System.out.println(returnjson);
            return returnjson;
        }

        @Override
        protected void onPostExecute(JSONObject returnjson) {
            super.onPreExecute();
            dialog.dismiss();
            if (returnjson == null || returnjson.get("stsCode") == null || !returnjson.get("stsCode").toString().equalsIgnoreCase("00")) {
                Toast.makeText(context, returnjson.get("stsMessage").toString(), Toast.LENGTH_LONG).show();
                return;
            }

            //Remove Flags Vehicle
            Config.jumlah_actv_vhcl =0;
            Config.jumlah_slct_vhcl =0;

            //Remove background location services
            if (mIsReqBgLocation) {
                Toast.makeText(StartOrderActivity.this, "Remove Location Background", Toast.LENGTH_LONG).show();
                mService.removeLocationUpdate();
            }

            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(StartOrderActivity.this, R.style.AlertDialogStyle);
            alertDialogBuilder.setTitle("Notification");
            alertDialogBuilder
                    .setMessage("Success confirm complete job")
                    .setPositiveButton("OKE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Intent intent = new Intent(StartOrderActivity.this, MainDriverActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onListenLocationFromMain(SendLocationToAllActivity event) {
        if (event != null) {
            mFromLatitude = event.getLocation().getLatitude();
            mFromLongitude = event.getLocation().getLongitude();

            /*
            String data = new StringBuilder()
                    .append("Loaction From Main : ")
                    .append(event.getLocation().getLatitude())
                    .append("/")
                    .append(event.getLocation().getLongitude())
                    .toString();
             */
            //openMapsLocation(context, );
            //Toast.makeText(mService, data, Toast.LENGTH_SHORT).show();
        }
    }

    //START LOCATION BACKGROUND FUNCTION SECTION

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if (mBound){
            unbindService(mServiceConnection);
            mBound = false;
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Common.KEY_REQUSTING_LOCATION_UPDATES)){
            setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUSTING_LOCATION_UPDATES, false));
        }
    }

    private void setButtonState(boolean isRequestEnable) {
        mIsReqBgLocation = isRequestEnable;
//        if (isRequestEnable){
//            mDirection.setEnabled(false);
//            mConfirmOrder.setEnabled(true);
//        } else {
//            mDirection.setEnabled(true);
//            mConfirmOrder.setEnabled(false);
//        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event) {
        if (event != null) {
            String data = new StringBuilder()
                    .append(event.getLocation().getLatitude())
                    .append("/")
                    .append(event.getLocation().getLongitude())
                    .toString();

            //openMapsLocation(context, );

            Toast.makeText(mService, data, Toast.LENGTH_SHORT).show();
        }
    }

    //END LOCATION BACKGROUND FUNCTION SECTION
}
