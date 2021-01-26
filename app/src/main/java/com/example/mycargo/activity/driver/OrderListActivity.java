package com.example.mycargo.activity.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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

import com.andremion.counterfab.CounterFab;
import com.example.mycargo.Locationbackground.Common;
import com.example.mycargo.Locationbackground.MyBackgroundService;
import com.example.mycargo.Locationbackground.SendLocationToActivity;
import com.example.mycargo.R;
import com.example.mycargo.adapter.OrderListAdapter;
import com.example.mycargo.app.Config;
import com.example.mycargo.util.CallWS;
import com.example.mycargo.util.SendAndReceiveJSON;
import com.example.mycargo.util.SendLocationToAllActivity;
import com.example.mycargo.util.Utility;
import com.google.android.gms.maps.model.LatLng;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.mycargo.util.Utility.openGoogleMapsLocation;

public class OrderListActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    //RECYCLER VIEW
    RecyclerView mRecyclerView;
    OrderListAdapter mOrderListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    Context context = this;
    //List<String> OrphanageHistoryList;

    private String mUsername;
    String mId_number;
    String mTruck_no;
    Button btn_start_assgn;
    Button btn_direction;
    private LinearLayout mIl_no_data;
    private LinearLayout mWrap_content;

    private JSONArray array_order_list;
    private JSONArray array;
    private JSONObject returnJSONDist;
    private JSONObject orpData;

    private int mTruck_capacity;
    private String mPortName;
    private double mFromLatitude;
    private double mFromLongitude;
    private double mPortLatitude;
    private double mPortLongitude;
    private boolean mIsReqBgLocation = false; // if background location is on

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
        setContentView(R.layout.activity_order_list);
        //getDataIntent
        getDataIntent();

        //getListData
        getDataOrder();

        mIl_no_data = findViewById(R.id.tv_wrap_noData);
        mWrap_content = findViewById(R.id.tv_wrap_content);
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText("Oreder Resume");

        LinearLayout btn_back = (LinearLayout) findViewById(R.id.tv_wrap_title);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        CounterFab add_car = findViewById(R.id.tv_add_car);
        add_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--- numb_of_list : " + Config.numb_of_list + " _ " + mTruck_capacity);
                if (Config.numb_of_list >= mTruck_capacity) {
                    Utility.showErrorDialog(context, "Notification","Your truck capacity is full");
                } else if (Config.jumlah_actv_vhcl > 0) {
                    Utility.showErrorDialog(context, "Notification","You have an active order");
                } else {
                    Intent intent = new Intent(OrderListActivity.this, OrderActivity.class);
                    startActivity(intent);
                }
            }
        });

        btn_start_assgn = findViewById(R.id.tv_btn_start);
        /*
        btn_start_assgn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--- jumlah_actv_vhcl : " + Config.jumlah_actv_vhcl);
                if (Config.jumlah_actv_vhcl > 0) {
                    Utility.showErrorDialog(context, "Notification","You have an active order");
                } else if (Config.jumlah_slct_vhcl == 0) {
                    Utility.showErrorDialog(context, "Notification","Add vehicle first");
                } else {
                    startAssignment();
                }
            }
        });*/

        btn_direction = findViewById(R.id.tv_direction);


        //recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_list_order);

        //swipe refresh layout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //get list data kind
                getDataOrder();
            }
        });

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
                        btn_direction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(OrderListActivity.this, "Start Location Background", Toast.LENGTH_LONG).show();
                                //direct to google maps

                                //IF THE DIRECTION AND TURN BY TURN MAP ARE OPENED ON GOOGLE MAPS
                                if (Config.jumlah_slct_vhcl == 0) {
                                    Utility.showErrorDialog(context, "Notification","Add vehicle first");
                                } else {
                                    openMapsLocation();
                                }
                                //IF THE DIRECTION MAP ARE INCLUDED ON MYCARGO APP
                                //Intent intent = new Intent(StartOrderActivity.this, DirectionActivity.class);
                                //startActivity(intent);
                            }
                        });

                        btn_start_assgn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                System.out.println("--- jumlah_actv_vhcl : " + Config.jumlah_actv_vhcl);
                                if (Config.jumlah_actv_vhcl > 0) {
                                    Utility.showErrorDialog(context, "Notification","You have an active order");
                                } else if (Config.jumlah_slct_vhcl == 0) {
                                    Utility.showErrorDialog(context, "Notification","Add vehicle first");
                                } else {
                                    startAssignment();
                                }
                            }
                        });

                        setButtonState(Common.requstingLocationUpdate(OrderListActivity.this));
                        bindService(new Intent(OrderListActivity.this,
                                        MyBackgroundService.class),
                                mServiceConnection,
                                Context.BIND_AUTO_CREATE);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();
    }

    private void openMapsLocation() {
        LatLng fromLatLng = new LatLng(mFromLatitude, mFromLongitude);
        LatLng toLatLng = new LatLng(mPortLatitude, mPortLongitude);

        if (fromLatLng == null || toLatLng == null) {
            Toast.makeText(context,"Current location not found please activate GPS",Toast.LENGTH_LONG).show();
            return;
        }

        openGoogleMapsLocation(context, fromLatLng, toLatLng);
    }

    private void startAssignment(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("TRK_NO", mTruck_no);
            jsonObject.put("DRV_ID", mId_number);
            StartTask task=new StartTask(mUsername,"D", jsonObject);
            task.execute();
        } catch (Exception e){
            Toast.makeText(this, "Notification : Connection Refused", Toast.LENGTH_LONG);
        }
    }

    private void getDataIntent() {
        try {
            //get session data
            SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
            mUsername =settings.getString("username","");
            mId_number =settings.getString("idNumber","");
            mTruck_no =settings.getString("truckNo","");
            mTruck_capacity = Utility.toInt(settings.getString("truckCapacity",""));

            mPortName = Utility.trim(settings.getString("portName",""));
            mPortLatitude = Utility.toDouble(settings.getString("portLat",""));
            mPortLongitude = Utility.toDouble(settings.getString("portLong",""));

            System.out.println("--- data session = " + mUsername + " " + mId_number +" " + mTruck_no + "  " + mTruck_capacity + " _ " + mPortName + " _ " + mPortLatitude + " _ " + mPortLongitude);
        } catch (Exception e) {
            Utility.showErrorDialog(context, "Notification", getString(R.string.error_connection));
        }
    }

    private void getDataOrder() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("MODE", "S");
            jsonObject.put("TYPE", "A");
            jsonObject.put("DRV_ID", mId_number);
            OrderTask task=new OrderTask(mUsername,"D", jsonObject);
            task.execute();
        } catch (Exception e){
            Toast.makeText(this, "Notification : Connection Refused", Toast.LENGTH_LONG);
        }
    }

    public class OrderTask extends AsyncTask<Void, Void, JSONObject> {
        private ProgressDialog dialog;
        private JSONObject jsonSet;

        OrderTask(String username,String typeOrder, JSONObject jsonObject) {
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
                returnjson= SendAndReceiveJSON.downloadUrlAndGetJSONPOST(CallWS.URLMyCargo+"mycargomobile/getOrderDriver", jsonSet);
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
            array_order_list= (JSONArray) returnjson.get("DATA");
            if(array_order_list==null||array_order_list.isEmpty())
            {
                System.out.println("--- data null");
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, "Notification : No Data",Toast.LENGTH_LONG);
                return;
            }

            mOrderListAdapter = new OrderListAdapter(array_order_list, mId_number, mTruck_no);
            mRecyclerView.setAdapter(mOrderListAdapter);
            //mOrphanageHistoryAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }
    }

    public class StartTask extends AsyncTask<Void, Void, JSONObject> {
        private ProgressDialog dialog;
        private JSONObject jsonSet;

        StartTask(String username,String typeOrder, JSONObject jsonObject) {
            this.jsonSet=jsonObject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, "Start Order", "Please wait...", true);
            dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject returnjson = null;
            try {
                System.out.println("--- data send startWorkTimeDriver : " + jsonSet);
                returnjson= SendAndReceiveJSON.downloadUrlAndGetJSONPOST(CallWS.URLMyCargo+"mycargomobile/startWorkTimeDriver", jsonSet);
                System.out.println("--- response startWorkTimeDriver : " + returnjson);
                return returnjson;
            } catch (Exception e) {
                Toast.makeText(context, "Notification failed start assignment",Toast.LENGTH_LONG);
                return returnjson;
            }
        }

        @Override
        protected void onPostExecute(JSONObject returnjson) {
            super.onPostExecute(returnjson);
            dialog.dismiss();

            if(returnjson==null || !returnjson.get("stsCode").equals("00"))
            {
                Utility.showErrorDialog(context, "Notification", returnjson.get("stsMessage").toString());
                return;
            }

            if(!mIsReqBgLocation){ //if is Request backgroud is Enable
                mService.requestLocationUpdates();
            }

            Utility.showErrorDialog(context, "Notification", returnjson.get("stsMessage").toString());

            Intent intent = new Intent(OrderListActivity.this, StartOrderActivity.class);
            startActivity(intent);
            finish();
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
                    .toString();*/

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

            //Toast.makeText(mService, data, Toast.LENGTH_SHORT).show();
        }
    }

    //END LOCATION BACKGROUND FUNCTION SECTION
}
