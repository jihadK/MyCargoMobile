package com.example.mycargo.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.mycargo.util.CallWS;
import com.example.mycargo.util.SendAndReceiveJSON;

import org.json.simple.JSONObject;

public class OrderClient {

    public static class getOrderDriverTask extends AsyncTask<Void, Void, JSONObject> {
        private ProgressDialog dialog;
        private JSONObject jsonSet;
        private Context context;

        public getOrderDriverTask(Context context, JSONObject obj) {
            this.jsonSet=obj;
            this.context=context;
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
                dialog.dismiss();
                return returnjson;
            } catch (Exception e) {
                dialog.dismiss();
                Toast.makeText(context, "Notification failed getOrderTruck",Toast.LENGTH_LONG);
                return returnjson;
            }
        }
    }

    public static class DeleteCarTask extends AsyncTask<Void, Void, JSONObject> {
        private ProgressDialog dialog;
        private JSONObject jsonSet;
        private Context context;

        public DeleteCarTask(Context context, JSONObject jsonObject) {
            this.jsonSet=jsonObject;
            this.context = context;
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
                returnjson= SendAndReceiveJSON.downloadUrlAndGetJSONPOST(CallWS.URLMyCargo+"mycargomobile/deleteVehicle", jsonSet);
                dialog.dismiss();
                return returnjson;
            } catch (Exception e) {
                Toast.makeText(context, "Notification failed getOrderTruck",Toast.LENGTH_LONG);
                dialog.dismiss();
                return returnjson;
            }
        }
    }
}
