package com.example.mycargo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycargo.R;
import com.example.mycargo.util.Utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLOutput;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {
    JSONArray mData;

    public OrderHistoryAdapter (JSONArray data){
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.content_order_list, parent, false);
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

        System.out.println("---- finish time : " +  Utility.trim(object.get("FINISH_TIME")));

        holder.tOrder_no.setText(Utility.trim(object.get("JOB_NO")) + " - " + Utility.trim(object.get("ORDER_TYPE")));
        holder.tOrder_det.setText(Utility.trim(object.get("MERK_VHE")) + " | "+Utility.trim(object.get("TYPE_VHE"))+" | "+Utility.trim(object.get("COLOUR_VHE")));
        //holder.tOrder_det.setText(Utility.trim(object.get("MERK_VHE")) +"-"+Utility.trim(object.get("JENIS_VHE"))+"-"+Utility.trim(object.get("COLOUR_VHE"))+"-"+Utility.trim(object.get("TYPE_VHE")));
        holder.tCtr_no.setText(Utility.trim(object.get("VIN_NUMBER")));
        holder.tStatus.setText(status);
        holder.tDate.setText(Utility.trim(object.get("FINISH_TIME")));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tDate;
        TextView tOrder_no;
        TextView tOrder_det;
        TextView tCtr_no;
        TextView tStatus;
        RelativeLayout tWrap_status;
        LinearLayout tWrap_content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tDate = itemView.findViewById(R.id.date);
            tOrder_no = itemView.findViewById(R.id.job_number);
            tOrder_det = itemView.findViewById(R.id.job_type);
            tCtr_no = itemView.findViewById(R.id.ctr_no);
            tStatus = itemView.findViewById(R.id.status);
            tWrap_status = itemView.findViewById(R.id.wrap_status);
            tWrap_content = itemView.findViewById(R.id.tv_wrap_content);
        }
    }
}
