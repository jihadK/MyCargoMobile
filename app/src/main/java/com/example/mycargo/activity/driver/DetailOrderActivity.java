package com.example.mycargo.activity.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycargo.R;
import com.example.mycargo.app.Config;
import com.example.mycargo.client.OrderClient;
import com.example.mycargo.dialog.JobQRDialog;
import com.example.mycargo.util.CallWS;
import com.example.mycargo.util.SendAndReceiveJSON;
import com.example.mycargo.util.Utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class DetailOrderActivity extends AppCompatActivity {

    private Button startOrder;
    private Button btn_add_car;
    private TextView tBarcode;

    private JSONArray array_order_list;
    private JSONObject localData;
    private Context context = this;

    private String data;
    private String mVin_no;
    private String mBarcode_no;
    private String mUsername;
    private String mId_number;
    private String mTruckNo;
    private String show_button;
    private String mService_type;

    private FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);

        tBarcode = findViewById(R.id.tv_barcode_no);
        tBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQR();
            }
        });

        startOrder = (Button) findViewById(R.id.startOrder);
        startOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                prosesOrder();
            }
        });

        ImageView btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DetailOrderActivity.super.onBackPressed();
//                Intent intent = new Intent(DetailOrderActivity.this, OrderActivity.class);
//                startActivity(intent);
                finish();
            }
        });

        Button direction = (Button) findViewById(R.id.direction);
        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailOrderActivity.this, DirectionActivity.class);
                startActivity(intent);
            }
        });

        btn_add_car = (Button) findViewById(R.id.tv_btn_add_car);
        btn_add_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCar();
            }
        });

        getDataIntent();
    }

    private void addCar(){
        try {
            JSONObject obj = new JSONObject();
            obj.put("ID_DRIVER", mId_number);
            obj.put("VIN_NUMBER", mVin_no);
            obj.put("TRK_NO", mTruckNo);

            AddCar task = new AddCar(obj);
            task.execute();
        } catch (Exception e) {
            Toast.makeText(DetailOrderActivity.this, "Connection Refused, Please Try Again ",Toast.LENGTH_LONG);
        }
    }

    private void getDataIntent() {
        try {

            //get session data
            SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
            mUsername =settings.getString("username","");
            mId_number =settings.getString("idNumber","");
            mTruckNo =settings.getString("truckNo","");

            System.out.println("--- truck no : " + mTruckNo);

            //data from intent
            mVin_no = getIntent().getSerializableExtra("vin_no").toString();
            show_button = getIntent().getSerializableExtra("show_button").toString();
            mService_type = getIntent().getSerializableExtra("service_type").toString();

            System.out.println("--- service_type : " + mService_type + " _ " +  Config.first_slct_order_type);

            if (show_button.equals("Y")) {
                if (Config.first_slct_order_type.equals(mService_type) || Config.first_slct_order_type.equals("")) {
                    btn_add_car.setVisibility(View.VISIBLE);
                } else {
                    btn_add_car.setVisibility(View.GONE);
                }
            } else {
                btn_add_car.setVisibility(View.GONE);
            }

            getOrderDriverDetail();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(DetailOrderActivity.this, "data intent not found ",Toast.LENGTH_LONG);
        }
    }

    private void getOrderDriverDetail(){
        try {
            JSONObject obj = new JSONObject();
            if (show_button.equals("Y")){
                obj.put("MODE", "N");
            } else {
                obj.put("MODE", "S");
            }
            obj.put("TYPE", "D");
            obj.put("SERVICE_TYPE", Utility.trim(mService_type));
            obj.put("VIN_NO", Utility.trim(mVin_no));
            obj.put("DRV_ID", Utility.trim(mId_number));

            System.out.println("--- data send : " + obj);

            //get data
            OrderClient.getOrderDriverTask task = new OrderClient.getOrderDriverTask(context, obj);
            JSONObject dataOrderDriverDetail = task.execute().get();

            //maping data
            JSONArray dataOrderArr = (JSONArray) dataOrderDriverDetail.get("DATA");

            TextView tVin_no = findViewById(R.id.tv_vin_no);
            TextView tOrderTime = findViewById(R.id.tv_order_time);
            TextView tMerk = findViewById(R.id.tv_merk);
            TextView tChassis = findViewById(R.id.tv_chassis_no);
            TextView tMechine = findViewById(R.id.tv_mechine_no);
            TextView tVhc_color = findViewById(R.id.tv_vehicle_color);
            TextView tVhc_type = findViewById(R.id.tv_vehicle_type);
            TextView tVhc_size = findViewById(R.id.tv_vehicle_size);

            for (Object object : dataOrderArr) {
                JSONObject dataObj = (JSONObject) object;

                mBarcode_no = Utility.trim(dataObj.get("BARCODE_NO"));

                tVin_no.setText(Utility.trim(dataObj.get("VIN_NUMBER")));
                tOrderTime.setText(Utility.trim(dataObj.get("CREATED_TIME")));
                tBarcode.setText(Utility.trim(dataObj.get("BARCODE_NO")));
                tMerk.setText(Utility.trim(dataObj.get("MERK_VHE")));
                tChassis.setText(Utility.trim(dataObj.get("CHASSIS_NUMBER")));
                tMechine.setText(Utility.trim(dataObj.get("MECHINE_NUMBER")));
                tVhc_color.setText(Utility.trim(dataObj.get("COLOUR_VHE")));
                tVhc_type.setText(Utility.trim(dataObj.get("JENIS_VHE")));
                tVhc_size.setText(Utility.trim(dataObj.get("TYPE_VHE")));

                System.out.println("--- CREATED_TIME : " + Utility.trim(dataObj.get("CREATED_TIME")));
            }
        } catch (Exception e) {
            Toast.makeText(DetailOrderActivity.this, "Connection Refused, Please Try Again ",Toast.LENGTH_LONG);
        }
    }

    private void showQR() {
        JSONObject qrJson = new JSONObject();
        qrJson.put("BARCODE_NO", mBarcode_no);

        JobQRDialog dialog=new JobQRDialog();
        dialog.setJsonObject(qrJson);
        dialog.show(fm,"Barcode");
    }

    public class AddCar extends AsyncTask<Void, Void, JSONObject> {
        private ProgressDialog dialog;
        private JSONObject jsonSet;

        public AddCar(JSONObject object) {
            this.jsonSet = object;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, "Add Vehicle", "Please wait...", true);
            dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject returnjson;
            System.out.println("--- data send addVehicle : " + jsonSet);
            returnjson = SendAndReceiveJSON.downloadUrlAndGetJSONPOST(
                    CallWS.URLMyCargo + "mycargomobile/addVehicle", jsonSet);
            System.out.println(returnjson);
            return returnjson;
        }

        @Override
        protected void onPostExecute(JSONObject returnjson) {
            super.onPreExecute();
            dialog.dismiss();
            if (returnjson == null || returnjson.get("stsCode") == null || !returnjson.get("stsCode").toString().equalsIgnoreCase("00")) {
                Utility.showErrorDialog(context, "Notification", returnjson.get("stsMessage").toString());
                return;
            }

            Toast.makeText(context, "Notification : " + returnjson.get("stsMessage").toString(), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(DetailOrderActivity.this, OrderListActivity.class);
            startActivity(intent);
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }
    }
}
