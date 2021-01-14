package com.example.mycargo.dialog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mycargo.R;
import com.example.mycargo.util.Utility;
import com.google.zxing.WriterException;

import org.json.simple.JSONObject;

public class JobQRDialog extends DialogFragment {

    private JSONObject jsonObject;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.dialog_job_qr, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        getDialog().setTitle("BARCODE NUMBER");
        // Do something else
        TextView terminal = (TextView) view.findViewById(R.id.text);
        TextView barcode_no = (TextView) view.findViewById(R.id.text1);
//        terminal.setText(Utility.trim(jsonObject.get("TERMINAL_ID")));
        barcode_no.setText(Utility.trim(jsonObject.get("BARCODE_NO")));
        try {
            ImageView imageView = (ImageView) view.findViewById(R.id.imgview);
            Bitmap bitmap = Utility.encodeAsBitmap(Utility.trim(jsonObject.get("BARCODE_NO")), 500, 500);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public void onClickConfirmOrder(View v) {
        dismiss();
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
}
