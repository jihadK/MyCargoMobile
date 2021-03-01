package com.example.mycargo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycargo.R;
import com.example.mycargo.activity.driver.DetailOrderActivity;
import com.example.mycargo.activity.driver.OrderListActivity;
import com.example.mycargo.app.Config;
import com.example.mycargo.client.OrderClient;
import com.example.mycargo.util.Utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.ViewHolder> {
    private static final String TAG = "OrderListAdapter";
    JSONArray mData;
    String mDrv_id;
    String mTrk_no;

    public OrderListAdapter(JSONArray data, String drv_id, String trk_no){
        this.mData = data;
        this.mDrv_id = drv_id;
        this.mTrk_no = trk_no;

        System.out.println("--- data adapter : " + data);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item_order_list_with_cancel, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject object = (JSONObject) mData.get(position);
        Context context = holder.tWrap_status.getContext();
        String status = "";

        //count active vehicle; STATUS = S;

        //maping data
        if (object.get("STATUS").equals("Y")) {
            System.out.println("status 1" + object.get("STATUS"));
            holder.tWrap_status.setBackground(context.getDrawable(R.drawable.bg_btn_complete));
        } else if (object.get("STATUS").equals("S")) {
            Config.jumlah_actv_vhcl++;
            System.out.println("status 2" + object.get("STATUS"));
            holder.tWrap_status.setBackground(context.getDrawable(R.drawable.bg_btn_active));
        } else {
            System.out.println("status 3" + object.get("STATUS"));
            holder.tWrap_status.setBackground(context.getDrawable(R.drawable.bg_btn_white));
        }

        //CHECK JOB STATUS MSG
        if (Utility.trim(object.get("STATUS")).equals("Y")){
            status = "COMPLETE";
        } else if(Utility.trim(object.get("STATUS")).equals("S")) {
            status = "ACTIVE";
        } else {
            status = "WAITING";
        }

        //MAPING VARIABLE CONFIG GLOBAL
        if (Utility.trim(object.get("ORDER_TYPE")).equals("EXP")) {
            Config.first_slct_order_type = "R";
        } else {
            Config.first_slct_order_type = "D";
        }
        Config.jumlah_slct_vhcl++;

        holder.tOrder_no.setText(Utility.trim(object.get("JOB_NO")));
        holder.tOrder_det.setText(Utility.trim(object.get("MERK_VHE")) + " | "+Utility.trim(object.get("TYPE_VHE"))+" | "+Utility.trim(object.get("COLOUR_VHE")));
        //holder.tOrder_det.setText(Utility.trim(object.get("MERK_VHE")) +"-"+Utility.trim(object.get("JENIS_VHE"))+"-"+Utility.trim(object.get("COLOUR_VHE"))+"-"+Utility.trim(object.get("TYPE_VHE")));
        holder.tVin_no.setText(Utility.trim(object.get("VIN_NUMBER")));
        holder.tStatus.setText(status);
        holder.tDate.setText(Utility.trim(object.get("CREATED_TIME")));

        holder.tWrap_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Config.jumlah_actv_vhcl > 0){
                    Utility.showErrorDialog(context, "Notification", "You have an active order");
                } else {
                    String message ="Are you sure to delete this vehicle ?";
                    showConfirmDialog(context, message, Utility.trim(object.get("VIN_NUMBER")));
                }
            }
        });

        holder.tWrap_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailOrderActivity.class);
                intent.putExtra("vin_no", Utility.trim(object.get("VIN_NUMBER")));
                intent.putExtra("show_button", "N");
                intent.putExtra("service_type", Config.first_slct_order_type);
                context.startActivity(intent);
            }
        });

        System.out.println("--- jumlah active vehicle : " + Config.jumlah_actv_vhcl);

    }

    @Override
    public int getItemCount() {
        Config.numb_of_list = mData.size();
        if ( mData.size() < 1) {
            Config.first_slct_order_type = "";
        }
        return mData.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tDate;
        TextView tOrder_no;
        TextView tOrder_det;
        TextView tVin_no;
        TextView tStatus;
        RelativeLayout tWrap_status;
        LinearLayout tWrap_content;
        ImageView tBtn_delete;
        LinearLayout tWrap_delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tDate = itemView.findViewById(R.id.date);
            tOrder_no = itemView.findViewById(R.id.job_number);
            tOrder_det = itemView.findViewById(R.id.job_type);
            tVin_no = itemView.findViewById(R.id.ctr_no);
            tStatus = itemView.findViewById(R.id.status);
            tWrap_status = itemView.findViewById(R.id.wrap_status);
            tWrap_content = itemView.findViewById(R.id.tv_wrap_content);
            tBtn_delete = itemView.findViewById(R.id.tv_btn_delete);
            tWrap_delete = itemView.findViewById(R.id.tv_wrap_delete);
        }
    }

    //confirm dialog
    public void showConfirmDialog(Context context, String message, String vin_no) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        alertDialogBuilder.setTitle("Confirmation");
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        System.out.println("--- No");
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println("--- Yes : " + vin_no);
                        try {
                            JSONObject object = new JSONObject();
                            object.put("ID_DRIVER", mDrv_id);
                            object.put("VIN_NUMBER", vin_no);
                            object.put("TRK_NO", mTrk_no);
                            OrderClient.DeleteCarTask task = new OrderClient.DeleteCarTask(context, object);
                            JSONObject returnDeleteCarTask = task.execute().get();

                            System.out.println("--- returnDeleteCarTask : " + returnDeleteCarTask);

                            if (returnDeleteCarTask == null || !returnDeleteCarTask.get("stsCode").equals("00")) {
                                Utility.showErrorDialog(context, "Notification", returnDeleteCarTask.get("stsMessage").toString());
                                return;
                            } else {
                                System.out.println("--- go to intent");
                                ((Activity) context).finish();
                                context.startActivity(((Activity) context).getIntent());
                            }
                        } catch (Exception e){
                            Toast.makeText(context, "Connection Refused, Please Try Again ",Toast.LENGTH_LONG);
                        }

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
