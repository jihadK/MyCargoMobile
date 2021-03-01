package com.example.mycargo.app;

import com.example.mycargo.util.Utility;
import com.google.android.gms.maps.model.LatLng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

public class Config {
    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "tobs";
    public static int jumlah_actv_vhcl = 0;
    public static int jumlah_slct_vhcl = 0;
    public static int numb_of_list = 0;
    public static String first_slct_order_type = "";

    public static ArrayList<String> mapCtrKey = new ArrayList<String>();
    public static ArrayList<String> mapCtrNo = new ArrayList<String>();
    public static ArrayList<String> mapCtrSize = new ArrayList<String>();
    public static ArrayList<String> mapComboYn = new ArrayList<String>();

    public static ArrayList<String> mapTerminal = new ArrayList<String>();
    public static ArrayList<String> mapTerminalName = new ArrayList<String>();
    public static HashMap<String, LatLng> mapTermLocation = new HashMap<String, LatLng>();

    public static ArrayList<String> mapTruckCompany = new ArrayList<String>();
    public static ArrayList<String> mapTruckCompanyName = new ArrayList<String>();

    //result value
    public static int REJECT = 10;

    public static void addTerminalTest() {
        mapTerminal.add(0, "TPM");
        mapTerminal.add(1, "MAL");
        mapTerminal.add(2, "TTL");
        mapTerminal.add(3, "TPS");
        mapTerminal.add(4, "TPKS");

        mapTermLocation.put("TPM", new LatLng(-5.128425, 119.405287));
        mapTermLocation.put("MAL", new LatLng(-6.097571, 106.889486));
        mapTermLocation.put("TTL", new LatLng(-7.204376, 112.669345));
        mapTermLocation.put("TPS", new LatLng(-7.212378, 112.720128));
        mapTermLocation.put("TPKS", new LatLng(-6.941896, 110.426079));
    }

    public static void setComboYN() {
        mapComboYn.add(0,"Please Select");
        mapComboYn.add(1,"YES");
        mapComboYn.add(2,"NO");
    }

    public static void setDataFromSharedCtrCombo(String dataFromShared, String select_ctr_key) {
        mapCtrKey.clear();
        mapCtrNo.clear();
        mapCtrSize.clear();
        JSONParser parser = new JSONParser();
        try {
            //JSONObject object = (JSONObject) parser.parse(dataFromShared);
            JSONArray arr = (JSONArray) parser.parse(dataFromShared);
            if (arr == null || arr.isEmpty()) return;
            int i = 0;
            for (Object obj : arr) {
                JSONObject object1 = (JSONObject) obj;
                if (!object1.get("CONTAINER_KEY").equals(select_ctr_key)) {
                    String code = Utility.trim(object1.get("CONTAINER_KEY"));
                    String ctr_no = Utility.trim(object1.get("CONTAINER_NO"));
                    //LatLng latLng = new LatLng(Utility.toDouble(object1.get("LATITUDE")), Utility.toDouble(object1.get("LONGITUDE")));
                    mapCtrKey.add(i, code);
                    if (i < 1){
                        mapCtrSize.add(i,"20");
                    } else {
                        mapCtrSize.add(i,"40");
                    }
                    mapCtrNo.add(i, Utility.trim(object1.get("CONTAINER_NO")));
                    //mapTermLocation.put(code, ctr_no);
                    i++;
                }
            }
            mapCtrKey.add(i, "0");
            mapCtrNo.add(i, "Please Select");
            mapCtrSize.add(i, "0");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void setDataFromShared(String dataFromShared) {
        mapTerminal.clear();
        mapTerminalName.clear();
        mapTermLocation.clear();
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(dataFromShared);
            JSONArray arr = (JSONArray) object.get("locations");
            if (arr == null || arr.isEmpty()) return;
            int i = 0;
            for (Object obj : arr) {
                JSONObject object1 = (JSONObject) obj;
                String code = Utility.trim(object1.get("LOC_CODE"));
                LatLng latLng = new LatLng(Utility.toDouble(object1.get("LATITUDE")), Utility.toDouble(object1.get("LONGITUDE")));
                mapTerminal.add(i, code);
                mapTerminalName.add(i, Utility.trim(object1.get("LOC_NAME")));
                mapTermLocation.put(code, latLng);
                i++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void setDataFromSharedTruck(String dataFromShared) {
        mapTruckCompany.clear();
        mapTruckCompanyName.clear();
        mapTruckCompany.add(0, "FREE");
        mapTruckCompanyName.add(0, "ALL AVAILABLE TRUCK");
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(dataFromShared);
            JSONArray arr = (JSONArray) object.get("truckCompanies");
            if (arr == null) return;
            int i = 1;
            for (Object obj : arr) {
                JSONObject object1 = (JSONObject) obj;
                String code = Utility.trim(object1.get("CUSTOMER"));
                mapTruckCompany.add(i, code);
                mapTruckCompanyName.add(i, Utility.trim(object1.get("FULL_NAME")));
                i++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void clearData() {
        mapTerminal.clear();
        mapTerminalName.clear();
        mapTermLocation.clear();
        mapTruckCompany.clear();
        mapTruckCompanyName.clear();
    }
}
