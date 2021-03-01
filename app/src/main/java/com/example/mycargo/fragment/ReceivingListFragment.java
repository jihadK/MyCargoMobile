package com.example.mycargo.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mycargo.R;
import com.example.mycargo.adapter.OrderAdapter;
import com.example.mycargo.adapter.OrderHistoryAdapter;
import com.example.mycargo.app.Config;
import com.example.mycargo.util.CallWS;
import com.example.mycargo.util.SendAndReceiveJSON;
import com.example.mycargo.util.Utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ReceivingListFragment extends Fragment {
    View view;
    TextView mTitle;

    Context context;
    private String username;
    private String id_number;
    private String truck_no;
    private JSONArray array_order_list;


    //RECYCLER VIEW
    RecyclerView mRecyclerView;
    OrderAdapter mOrderAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayout mIl_no_data;
    private LinearLayout mWrap_content;

    public ReceivingListFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.component_fragment_assgn_list, container,false);
        context = view.getContext();

        getDataIntent();

        mIl_no_data = view.findViewById(R.id.tv_wrap_noData);
        mWrap_content = view.findViewById(R.id.tv_wrap_content);

        //recyclerview
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_list_order);

        //swipe refresh layout
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //get list data kind
                getDataOrder();
            }
        });
        return view;
    }

    private void getDataIntent(){
        try {
            //get session data
            SharedPreferences settings = context.getSharedPreferences(Config.SHARED_PREF, 0);
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
        jsonObject.put("TYPE", "A");
        jsonObject.put("SERVICE_TYPE", "R");
        jsonObject.put("DRV_ID", id_number);
        OrderTask task=new OrderTask(username,"D", jsonObject);
        task.execute();
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
                returnjson= SendAndReceiveJSON.downloadUrlAndGetJSONPOST(CallWS.URLMyCargo+"mycargomobile/getOrderDriver", jsonSet);
                System.out.println(returnjson);
                return returnjson;
            } catch (Exception e) {
                Toast.makeText(context, "Notification : failed order receiving",Toast.LENGTH_LONG);
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

            mOrderAdapter = new OrderAdapter(array_order_list);
            mRecyclerView.setAdapter(mOrderAdapter);
            //mOrphanageHistoryAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }
    }
}
