package com.example.mycargo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import com.example.mycargo.activity.driver.MainDriverActivity;
import com.example.mycargo.app.Config;
import com.example.mycargo.util.CallWS;
import com.example.mycargo.util.SendAndReceiveJSON;
import com.example.mycargo.util.Utility;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SplashScreenActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null;

    String mId_number;
    String mPassword;
    String mtruck_number;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);

        //version
        TextView appVersion = findViewById(R.id.tv_apps_version);
        appVersion.setText(getText(R.string.apps_version));

        //auto login yn
        SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
        String autoLogin_yn =settings.getString("username","");
        String aId_number =settings.getString("idNumber","");
        String aPassword =settings.getString("password","");
        String aTruck_no =settings.getString("truckNo","");

        if (autoLogin_yn != "" && aId_number != "" && aPassword != ""&& aTruck_no != "" ) {
            mId_number = Utility.trim(aId_number);
            mPassword = Utility.trim(aPassword);
            mtruck_number = Utility.trim(aTruck_no);
            attemptLogin();
        } else {
            doTimerHendler();
        }
    }

    private void doTimerHendler(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                finish();
            }
        },1500L);
    }

    private void attemptLogin() {

        JSONObject object=new JSONObject();
//        object.put("username",mEmailView.getText().toString());
        object.put("idNumber",mId_number);
        object.put("password",mPassword);
        object.put("truckNo",mtruck_number);

        mAuthTask = new UserLoginTask(object);
        mAuthTask.execute((Void) null);
    }

    public class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {

        private final JSONObject objectSet;

        UserLoginTask(JSONObject objectSet) {
            this.objectSet = objectSet;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            String token= FirebaseInstanceId.getInstance().getToken();
            System.out.println("token "+token);
            String appVersion = "1.00";
            // TODO: attempt authentication against a network service.
            objectSet.put("token",token);
            objectSet.put("version",appVersion);
            System.out.println("data send = " + objectSet);
            JSONObject returnjson = SendAndReceiveJSON.downloadUrlAndGetJSONPOST(
                    CallWS.URLMyCargo + "mycargomobile/loginTruck", objectSet);
//            JSONObject returnjson = SendAndReceiveJSON.downloadUrlAndGetJSONPOST(
//                    CallWS.URL + "login", objectSet);
            System.out.println("return login = " + returnjson);
            return returnjson;
        }

        @Override
        protected void onPostExecute(JSONObject returnjson) {
            mAuthTask = null;
            if (returnjson == null || !returnjson.get("stsCode").equals("00")) {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
                return;
            }

            //CREATE SESSION
            SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
            SharedPreferences.Editor editor = settings.edit();

            if (returnjson.get("data_port") != null) {
                JSONArray dataPort = (JSONArray) returnjson.get("data_port");
                for (Object object : dataPort) {
                    JSONObject obj = (JSONObject) object;
                    editor.putString("portName",Utility.trim(obj.get("LOC_NAME")));
                    editor.putString("portLat",Utility.trim(obj.get("LATITUDE")));
                    editor.putString("portLong",Utility.trim(obj.get("LONGITUDE")));

                    System.out.println("--- port data : " + Utility.trim(obj.get("LOC_NAME")) + " _ "
                            + Utility.trim(obj.get("LATITUDE")) + " _ " + Utility.trim(obj.get("LONGITUDE")));
                }
            } else {
                editor.putString("portName","");
                editor.putString("portLat","");
                editor.putString("portLong","");
            }

            String username=Utility.trim(returnjson.get("name"));
            String idNumber=Utility.trim(mId_number);
            String truckNo=Utility.trim(mtruck_number);
            editor.putString("username",Utility.trim(returnjson.get("name")));
//            editor.putString("regType",regType);
            editor.putString("idNumber",idNumber);
            editor.putString("password",Utility.trim(mPassword));
            editor.putString("truckNo",truckNo);
            editor.putString("truckCapacity", Utility.trim(returnjson.get("trk_capacity")));
            editor.commit();

            System.out.println("--- turck no : " + truckNo);

            String data=returnjson.toJSONString();
//            TrobossFirebaseInstanceIdService.username=username;
            Intent intent = new Intent(SplashScreenActivity.this, MainDriverActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("idNumber", idNumber);
            intent.putExtra("data", data);
            startActivity(intent);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}
