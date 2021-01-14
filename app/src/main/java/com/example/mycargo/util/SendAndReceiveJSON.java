package com.example.mycargo.util;

import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendAndReceiveJSON {

    public static JSONObject downloadUrlAndGetJSONPOST(String strUrl, JSONObject jsonObject){
        JSONObject returnjson = null;
        String data = "";
        InputStream iStream = null;
        OutputStream out = null;
        HttpURLConnection urlConnection = null;

        try {
            System.out.println("connect " + strUrl);
            URL url = new URL(strUrl);

            // Creating an http connection to communicate withh url
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            out = urlConnection.getOutputStream();

            if(jsonObject !=null) {
                System.out.println("json obj input " + jsonObject.toJSONString());
                out.write(jsonObject.toJSONString().getBytes());
                out.flush();
            }

//            Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null ) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl",data.toString());
            br.close();
            JSONParser parser = new JSONParser();
            returnjson = (JSONObject) parser.parse(data);
        } catch (Exception e){
            Log.d("Exception", e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (iStream != null) {
                    iStream.close();
                }
                if (out != null) {
                    out.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return returnjson;
    }

    public static JSONObject downloadUrlAndGetJSONGET(String strUrl){
        JSONObject returnjson = null ;
        String data = "";
        InputStream iStream = null;
        OutputStream out = null;
        HttpURLConnection urlConnection = null;

        try {
            System.out.println("connect " + strUrl);
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json");
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
            JSONParser parser = new JSONParser();
            returnjson = (JSONObject) parser.parse(data);
        } catch (Exception e) {
            Log.d("Exception", e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (iStream !=null) {
                    iStream.close();
                }
                if (urlConnection !=null){
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return returnjson;
    }
}
