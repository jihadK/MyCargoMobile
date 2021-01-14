package com.example.mycargo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Utility {

    public static DecimalFormat noDecimalFormat = new DecimalFormat("#");
    public static DecimalFormat currencyFormat = new java.text.DecimalFormat("#,###");
    public static SimpleDateFormat yyyy_mm_dd = new SimpleDateFormat("yyyy-MMM-ddd");
    public static SimpleDateFormat dd_mm_yyyy = new SimpleDateFormat("dd/MM/yyyy");
    public static SimpleDateFormat dd_mm_yyyy1 = new SimpleDateFormat("dd-MM-yyyy");
    public static SimpleDateFormat hh_mm = new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat dd_mm_yyyy_hh_mm = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
    public static SimpleDateFormat mm_yyyy = new SimpleDateFormat("MM/yyyy");

    public static String trim(Object o){
        if(o==null) return "";
        else return o.toString();
    }

    public static int toInt(Object o)
    {
        if(o==null)return 0;
        else return Integer.parseInt(o.toString());
    }

    public static double toDouble(Object o)
    {
        if(o==null)return 0;
        else return Double.parseDouble(o.toString());
    }

    public static String doubleToString (double d)
    {
        return noDecimalFormat.format(d);
    }

    public static String doubleToStringCurrency (double d)
    {
        return currencyFormat.format(d);
    }

    public static void showErrorDialog(Context context, String title, String message)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public static String parseHTMLError(String message, String parse) {
        return message.replaceAll(parse,"<br />- ");
    }

    //example
    public void showConfirmDialog(Context context, String title, String message,final String flg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //do your job here
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public static boolean checkText(Context context, TextView textView, int length)
    {
        if(textView.getText().toString().equalsIgnoreCase(""))
        {
            textView.requestFocus();
            Utility.showErrorDialog(context,"Error","must be filled");
            return false;
        }
        else if(textView.getText().toString().length()<length)
        {
            textView.requestFocus();
            Utility.showErrorDialog(context,"Error","data length must be more than "+(length-1));
            return false;
        }
        return true;
    }

    public static String convertStringToDateString(String date, int i) {
        String temp = "";
        try {
            String tanggal, tahun, bulan, jam, menit, detik;
            tahun = date.substring(0, 4);
            bulan = date.substring(5, 7);
            tanggal = date.substring(8, 10);
            jam = date.substring(11, 13);
            menit = date.substring(14, 16);
            detik = date.substring(17, 19);
            if (i == 0) {
                temp = tanggal + "/" + bulan + "/" + tahun + " " + jam + ":" + menit;
            }
            if (i == 1) {
                temp = tahun + bulan + tanggal + jam + menit;
            }
            if (i == 2) {
                temp = tahun + bulan + tanggal + "_" + jam + menit + detik;
            }
            if (i == 3) {
                temp = tahun + bulan + tanggal;
            }
            if (i == 4) {
                temp = tahun + bulan + tanggal + jam + menit + detik;
            }
            if (i == 5) {
                temp = tanggal + "/"+ bulan + "/" + tahun + " "+ jam + ":" +menit ;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("format exception");
            temp = "";
        }
        return temp;
    }

    public static Bitmap encodeAsBitmap(String str, int width, int height) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, width, height, null);
        } catch (Exception iae) {
            // Unsupported format
            return null;
        }

        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;//black : white
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public static String fromDatePeriod(int future,int now, int last) {
        String fromDate = "" ;

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");
        SimpleDateFormat dateFormatMont = new SimpleDateFormat("MM");
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        String dateNow =  dateFormatGmt.format(new Date());
        System.out.println("data format gmt " + dateNow+"");

        Date myDate = null;
        try {
            myDate = dateFormatGmt.parse(dateNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar tCalendar = Calendar.getInstance();
        tCalendar.setTime(myDate);
        tCalendar.add(Calendar.MONTH, now);

        Calendar fCalendar = Calendar.getInstance();
        fCalendar.setTime(myDate);
        fCalendar.add(Calendar.MONTH, future);

        Calendar lCalendar = Calendar.getInstance();
        lCalendar.setTime(myDate);
        lCalendar.add(Calendar.MONTH, last);

        if (Utility.toInt(dateFormatDay.format(tCalendar.getTime())) <= 25 ) {
            fromDate = Utility.trim(dateFormatYear.format(lCalendar.getTime()) +"/"+dateFormatMont.format(lCalendar.getTime())+"/26");
            System.out.println("v2 from date = " + fromDate);
        } else if ( Utility.toInt(dateFormatDay.format(tCalendar.getTime())) > 25 ) {
            fromDate = Utility.trim(dateFormatYear.format(tCalendar.getTime())+"/"+dateFormatMont.format(tCalendar.getTime())+"/26");
            System.out.println("v2 from date = " + fromDate );
        }

        return fromDate;
    }

    public static String toDatePeriod(int future,int now, int last) {
        String toDate="";
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");
        SimpleDateFormat dateFormatMont = new SimpleDateFormat("MM");
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        String dateNow =  dateFormatGmt.format(new Date());
        System.out.println("data format gmt " + dateNow+"");

        Date myDate = null;
        try {
            myDate = dateFormatGmt.parse(dateNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar tCalendar = Calendar.getInstance();
        tCalendar.setTime(myDate);
        tCalendar.add(Calendar.MONTH, now);

        Calendar fCalendar = Calendar.getInstance();
        fCalendar.setTime(myDate);
        fCalendar.add(Calendar.MONTH, future);

        Calendar lCalendar = Calendar.getInstance();
        lCalendar.setTime(myDate);
        lCalendar.add(Calendar.MONTH, last);

        if (Utility.toInt(dateFormatDay.format(tCalendar.getTime())) <= 25 ) {
            toDate = Utility.trim(dateFormatYear.format(tCalendar.getTime()) +"/"+ dateFormatMont.format(tCalendar.getTime()) + "/25");
            System.out.println("to date = "+ toDate);
        } else if (Utility.toInt(dateFormatDay.format(tCalendar.getTime())) > 25 ) {
            toDate = Utility.trim(dateFormatYear.format(fCalendar.getTime()) +"/"+ dateFormatMont.format(fCalendar.getTime()) + "/25");
            System.out.println("to date = "+ toDate);
        }

        return toDate;
    }

    public static String datePrev(int future,int now, int last, String from_to) {
        String data="";
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");
//        SimpleDateFormat dateFormatMont = new SimpleDateFormat("MM");
//        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        String dateNow =  dateFormatGmt.format(new Date());
//        System.out.println("data format gmt " + dateNow+"");

        Date myDate = null;
        try {
            myDate = dateFormatGmt.parse(dateNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar tCalendar = Calendar.getInstance();
        tCalendar.setTime(myDate);
        tCalendar.add(Calendar.MONTH, now);

        if (from_to.equalsIgnoreCase("F")) {
            if (Utility.toInt(dateFormatDay.format(tCalendar.getTime())) <= 25 ) {
                data = Utility.trim(year(-2) +"/"+ month(-2) + "/26");
                System.out.println("from date = "+ data);
            } else if (Utility.toInt(dateFormatDay.format(tCalendar.getTime())) > 25 ) {
                data = Utility.trim(year(-1) +"/"+ month(-1) + "/26");
                System.out.println("to date = "+ data);
            }
        } else if (from_to.equalsIgnoreCase("T")) {
            if (Utility.toInt(dateFormatDay.format(tCalendar.getTime())) <= 25 ) {
                data = Utility.trim(year(-1) +"/"+ month(-1) + "/25");
                System.out.println("from date = "+ data);
            } else if (Utility.toInt(dateFormatDay.format(tCalendar.getTime())) > 25 ) {
                data = Utility.trim(year(0) +"/"+ month(0) + "/25");
                System.out.println("to date = "+ data);
            }
        }

        return data;
    }

    public static String month(int now) {
        String month="";
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormatMont = new SimpleDateFormat("MM");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        String dateNow =  dateFormatGmt.format(new Date());
//        System.out.println("data format gmt " + dateNow+"");

        Date myDate = null;
        try {
            myDate = dateFormatGmt.parse(dateNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar tCalendar = Calendar.getInstance();
        tCalendar.setTime(myDate);
        tCalendar.add(Calendar.MONTH, now);

        month = dateFormatMont.format(tCalendar.getTime());

        return month;
    }

    public static String year(int now) {
        String year="";
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        String dateNow =  dateFormatGmt.format(new Date());

        Date myDate = null;
        try {
            myDate = dateFormatGmt.parse(dateNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar tCalendar = Calendar.getInstance();
        tCalendar.setTime(myDate);
        tCalendar.add(Calendar.MONTH, now);

        year = dateFormatYear.format(tCalendar.getTime());
        System.out.println("data format gmt year " + dateNow+" " + year);

        return year;
    }

    public static String monthString(int now) {
        String month="";
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM");
        SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        String dateNow =  dateFormatGmt.format(new Date());

        Date myDate = null;
        try {
            myDate = dateFormatGmt.parse(dateNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar tCalendar = Calendar.getInstance();
        tCalendar.setTime(myDate);

        if (Utility.toInt(dateFormatDay.format(tCalendar.getTime())) <= 25 ) {
            tCalendar.add(Calendar.MONTH, now);
        } else if (Utility.toInt(dateFormatDay.format(tCalendar.getTime())) > 25 ) {
            tCalendar.add(Calendar.MONTH, 0);
        }

        month = dateFormatMonth.format(tCalendar.getTime());
        System.out.println("data format gmt year " + dateNow+" " + month);

        return month;
    }

    public static int converInDP(int value, Context context)
    {
        int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());

        return dimensionInDp;
    }

    public static void openGoogleMapsLocation(Context context,  LatLng fromLatLng, LatLng toLatLng) {
        if (fromLatLng == null || toLatLng == null) {
            Toast.makeText(context,"Current location not found please activate GPS",Toast.LENGTH_LONG).show();
            return;
        }

        double dest_lon = toLatLng.longitude;
        double dest_lat = toLatLng.latitude;
//        if (flg.equalsIgnoreCase("A")) {
//            dest_lat = mDestLocationMarker.getPosition().latitude;
//            dest_lon = mDestLocationMarker.getPosition().longitude;
//        }
        final Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?"
                        + "saddr=" + fromLatLng.latitude + "," + fromLatLng.longitude
                        + "&daddr=" + dest_lat + "," + dest_lon));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        context.startActivity(intent);
    }
}
