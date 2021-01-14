package com.example.mycargo.activity.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycargo.R;
import com.example.mycargo.adapter.OrderHistoryAdapter;
import com.example.mycargo.adapter.OrderListAdapter;
import com.example.mycargo.app.Config;
import com.example.mycargo.util.CallWS;
import com.example.mycargo.util.SendAndReceiveJSON;
import com.example.mycargo.util.Utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class OrderHistoryActivity extends AppCompatActivity {
    private Context context = this;
    String username;
    String id_number;
    String truck_no;
    private JSONArray array_order_list;

    int jumlah_data= 0;

    //RECYCLER VIEW
    RecyclerView mRecyclerView;
    OrderHistoryAdapter mOrderHistoryAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayout mIl_no_data;
    private LinearLayout mWrap_content;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        getDataIntent();

        ImageView btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderHistoryActivity.this, MainDriverActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mIl_no_data = findViewById(R.id.tv_wrap_noData);
        mWrap_content = findViewById(R.id.tv_wrap_content);

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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("DRV_ID", id_number);
        OrderHistoryTask task=new OrderHistoryTask(username,"D", jsonObject);
        task.execute();
    }

    public class OrderHistoryTask extends AsyncTask<Void, Void, JSONObject> {
        private final String typeOrder;
        private ProgressDialog dialog;
        private String username;
        private String idNumber;
        private JSONObject jsonSet;

        OrderHistoryTask(String username,String typeOrder, JSONObject jsonObject) {
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
                returnjson= SendAndReceiveJSON.downloadUrlAndGetJSONPOST(
                        CallWS.URLMyCargo+"mycargomobile/getOrderDriverHist", jsonSet);
                System.out.println(returnjson);
                return returnjson;
            } catch (Exception e) {
                Toast.makeText(context, "Notification : failed order history",Toast.LENGTH_LONG);
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
            }

            mWrap_content.setVisibility(View.VISIBLE);
            array_order_list= (JSONArray) returnjson.get("DATA");
            if(array_order_list==null||array_order_list.isEmpty())
            {
                Toast.makeText(context, "No Data",
                        Toast.LENGTH_LONG).show();
                return;
            }

            mOrderHistoryAdapter = new OrderHistoryAdapter(array_order_list);
            mRecyclerView.setAdapter(mOrderHistoryAdapter);
            //mOrphanageHistoryAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }
    }

}
