package com.example.mycargo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mycargo.activity.driver.MainDriverActivity;
import com.example.mycargo.app.Config;
import com.example.mycargo.util.CallWS;
import com.example.mycargo.util.SendAndReceiveJSON;
import com.example.mycargo.util.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.simple.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private Context context = this;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mTruckNoView;
    private EditText mIdNumberView;
    private TextView lTotal;
    private TextView register;
    private ImageButton btRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        askPermission();

        // Set up the login form.
        mIdNumberView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mTruckNoView = (EditText) findViewById(R.id.truck_no);

        //Go to Register activity
        btRegister = (ImageButton) findViewById(R.id.btRegister);
        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //auto login yn
        SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
        String autoLogin_yn =settings.getString("username","");
        String aId_number =settings.getString("idNumber","");
        String aPassword =settings.getString("password","");
        String aTruck_no =settings.getString("truckNo","");

        if (autoLogin_yn != "") {
            mIdNumberView.setText(aId_number);
            mPasswordView.setText(aPassword);
            mTruckNoView.setText(aTruck_no);
        }


        //Btn Sign In
        Button mEmailSignInButton = (Button) findViewById(R.id.blogin);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mAuth = FirebaseAuth.getInstance();

    }

    private void askPermission() {
        Dexter.withActivity(this).withPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("onPermissionGranted 1");
                    return;
                }
                System.out.println("onPermissionGranted 2");
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();
    }

    private void attemptLogin() {
        // Reset errors.
        mIdNumberView.setError(null);
        mPasswordView.setError(null);
        mTruckNoView.setError(null);

        // Store values at the time of the login attempt.
        JSONObject object=new JSONObject();
//        object.put("username",mEmailView.getText().toString());
        object.put("idNumber",mIdNumberView.getText().toString());
        object.put("password",mPasswordView.getText().toString());
        object.put("truckNo",mTruckNoView.getText().toString());
        String email = mIdNumberView.getText().toString();
        String password = mPasswordView.getText().toString();
        String truckNo = mTruckNoView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mIdNumberView.setError(getString(R.string.error_field_required));
            focusView = mIdNumberView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(truckNo)) {
            mTruckNoView.setError(getString(R.string.error_field_required));
            focusView = mTruckNoView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(object);
            mAuthTask.execute((Void) null);
        }

    }

    public class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {

        private final JSONObject objectSet;
        private ProgressDialog dialog;

        UserLoginTask(JSONObject objectSet) {
            this.objectSet = objectSet;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, "Login", "Please wait...", true);
            dialog.show();
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
            dialog.dismiss();
            mAuthTask = null;
            String message;
            if (returnjson == null || !returnjson.get("stsCode").equals("00")) {
                if (returnjson != null)
                {
                    message=Utility.trim(returnjson.get("stsMessage"));
                }
                else
                {
                    message="Cannot Connect Server, Please Try Again Later";
                }
                Utility.showErrorDialog(context, "Notification", message);
//                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null,null);
                return;
            }
            SharedPreferences settings = getSharedPreferences(Config.SHARED_PREF, 0);
            SharedPreferences.Editor editor = settings.edit();
            String username=Utility.trim(returnjson.get("name"));
            String idNumber=Utility.trim(mIdNumberView.getText().toString());
            String truckNo=Utility.trim(mTruckNoView.getText().toString());
            editor.putString("username",Utility.trim(returnjson.get("name")));
//            editor.putString("regType",regType);
            editor.putString("idNumber",idNumber);
            editor.putString("password",Utility.trim(objectSet.get("password")));
            editor.putString("oldpassword",Utility.trim(objectSet.get("password")));
            editor.putString("truckNo",truckNo);
            editor.commit();
            returnjson.put("oldpassword",Utility.trim(objectSet.get("password")));
            String data=returnjson.toJSONString();
            System.out.println("--- turck no : " + truckNo);

//            TrobossFirebaseInstanceIdService.username=username;
            Intent intent = new Intent(LoginActivity.this, MainDriverActivity.class);
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
