package com.example.mycargo.activity.driver;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycargo.R;
import com.example.mycargo.app.Config;
import com.example.mycargo.dialog.OrderDetailDialog;
import com.example.mycargo.util.CallWS;
import com.example.mycargo.util.SendAndReceiveJSON;
import com.example.mycargo.util.Utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class OrderActivity extends AppCompatActivity {
    private Context context = this;
    String username;
    String id_number;
    String truck_no;
    int ctr_active;
    private JSONArray array_order_list;

    LinearLayout mWrap_content;
    LinearLayout mIl_no_data;

    String[] DATE;
    String[] VIN_NO;
    String[] STATUSMSG;
    String[] STATUSCD;
    String[] ORDER_NO;
    String[] ORDER_TYPE;
    String[] VIN_KEY;
    String[] CREATED_TIME;
    String[] ORDER_DET;

    private FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_order);

        mWrap_content = findViewById(R.id.tv_wrap_content);
        mIl_no_data = findViewById(R.id.tv_wrap_noData);

        ImageView btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderActivity.this, MainDriverActivity.class);
                startActivity(intent);
            }
        });

        getDataIntent();

    }

    private void getDataIntent(){
        try {
            //get session data
            SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
            username =settings.getString("username","");
            id_number =settings.getString("idNumber","");
            truck_no =settings.getString("truckNo","");
            System.out.println("data session = " + username + " " + id_number +" " + truck_no);

            getDataOrder();
        } catch (Exception e) {
            Utility.showErrorDialog(context, "Notification", getString(R.string.error_connection));
        }
    }

    private void getDataOrder() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("TYPE", "A");
            jsonObject.put("DRV_ID", id_number);
            OrderTask task=new OrderTask(username,"D", jsonObject);
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
                Toast.makeText(context, "No Active Order",
                        Toast.LENGTH_LONG).show();
                return;
            }

            //MAPING DATA
            int i = 0;
            String orderType = "";
            String status = "";
            JSONArray array = array_order_list;
            DATE = new String[array.size()];
            VIN_NO = new String[array.size()];
            ORDER_NO = new String[array.size()];
            ORDER_TYPE = new String[array.size()];
            STATUSMSG = new String[array.size()];
            VIN_KEY = new String[array.size()];
            STATUSCD = new String[array.size()];
            CREATED_TIME = new String[array.size()];
            ORDER_DET = new String[array.size()];
            for (Object obj : array) {
                JSONObject object = (JSONObject) obj;
                if (object.get("STATUS").equals("S")){
                    ctr_active++;
                }
                VIN_NO[i] = Utility.trim(object.get("VIN_NUMBER"));
                VIN_KEY[i] = Utility.trim(object.get("BARCODE_NO"));
                ORDER_NO[i] = Utility.trim(object.get("JOB_NO"));
                CREATED_TIME[i] = Utility.trim(object.get("CREATED_TIME"));
                //CHECK ORDER TYPE
//                if (Utility.trim(object.get("ORDER_TYPE")).equals("STY")){
//                    orderType = "EXPORT";
//                } else {
//                    orderType = "IMPORT";
//                }

                ORDER_DET[i] = Utility.trim(object.get("MERK_VHE")) +" | "+Utility.trim(object.get("TYPE_VHE"))+" | "+Utility.trim(object.get("COLOUR_VHE"));
                //ORDER_DET[i] = Utility.trim(object.get("MERK_VHE")) +"-"+Utility.trim(object.get("JENIS_VHE"))+"-"+Utility.trim(object.get("COLOUR_VHE"))+"-"+Utility.trim(object.get("TYPE_VHE"));
                ORDER_TYPE[i] = Utility.trim(object.get("ORDER_TYPE"));
                STATUSCD[i] = Utility.trim(object.get("STATUS"));
                //CHECK JOB STATUS MSG
                if (Utility.trim(object.get("STATUS")).equals("Y")){
                    status = "COMPLETE";
                } else if(Utility.trim(object.get("STATUS")).equals("S")) {
                    status = "ACTIVE";
                } else {
                    status = "WAITING";
                }
                STATUSMSG[i] = Utility.trim(status);
                i++;
            }


            //listview
            ListView listView = (ListView)findViewById(R.id.list_view_order);

            //panggil isi content list
            CustomAdapter customAdapter1= new CustomAdapter();
            listView.setAdapter(customAdapter1);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println("position = " + position);
                    System.out.println("data from position pointer = " + ORDER_NO[position] + " _ " + VIN_NO[position] + " _ " + STATUSMSG[position] + " _ " + ORDER_TYPE[position]);
                    if (ctr_active == 0) {
                        Intent intent = new Intent(OrderActivity.this, DetailOrderActivity.class);
                        intent.putExtra("vin_no", VIN_NO[position]);
                        intent.putExtra("show_button", "Y");
                        startActivity(intent);
                    } else {
                        popupMessage();
                    }
                }
            });

        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return array_order_list.size();
        }

        @Override
        public Object getItem(int position) {
//            System.out.println("position = " + position);
//            System.out.println("data from position pointer = " + ORDER_NO[position] + " _ " + CTR_NO[position] + " _ " + STATUS[position] + " _ " + ORDER_TYPE[position]);
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup parent) {
            view = getLayoutInflater().inflate(R.layout.content_order_list, null);

            TextView tDate = (TextView)view.findViewById(R.id.date);
            TextView tOrder_no = (TextView)view.findViewById(R.id.job_number);
            TextView tOrder_det = (TextView)view.findViewById(R.id.job_type);
            TextView tCtr_no = (TextView)view.findViewById(R.id.ctr_no);
            TextView tStatus = (TextView)view.findViewById(R.id.status);
            RelativeLayout tWrap_status = (RelativeLayout) view.findViewById(R.id.wrap_status);

//            date.setText(DATE[i]);
                if (STATUSCD[i].equals("Y")) {
                    System.out.println("status 1" + STATUSCD[i]);
                    tWrap_status.setBackground(getResources().getDrawable(R.drawable.bg_btn_complete));
                } else if (STATUSCD[i].equals("S")) {
                    System.out.println("status 2" + STATUSCD[i]);
                    tWrap_status.setBackground(getResources().getDrawable(R.drawable.bg_btn_active));
                } else {
                    System.out.println("status 3" + STATUSCD[i]);
                    tWrap_status.setBackground(getResources().getDrawable(R.drawable.bg_btn_white));
                }
            tOrder_no.setText(ORDER_NO[i]);
            tOrder_det.setText(ORDER_DET[i]);
            tCtr_no.setText(VIN_NO[i]);
            tStatus.setText(STATUSMSG[i]);
            tDate.setText(CREATED_TIME[i]);
            return view;
        }
    }

    private void popupMessage(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OrderActivity.this, R.style.AlertDialogStyle);
        alertDialogBuilder.setTitle("Notification");
        alertDialogBuilder
                .setMessage("You have active order, please complete your order first.")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
