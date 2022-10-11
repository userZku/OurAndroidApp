package com.example.ourandroidapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private CameraManager mCameraManager;
    private String mCameraId;
    private TextView textToConvert;
    private TextView morseResult;
    private Button toMorseBtn;
    private Button clearBtn;
    private Button morseToLightBtn;
    private boolean statusLight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToConvert = (TextView) findViewById(R.id.textToConvert);
        morseResult = (TextView) findViewById(R.id.morseResult);
        toMorseBtn = (Button) findViewById(R.id.toMorseBtn);
        clearBtn = (Button) findViewById(R.id.clearBtn);
        morseToLightBtn = (Button) findViewById(R.id.morseToLightBtn);

        boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);

        if (!isFlashAvailable) {
            showNoFlashError();
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        toMorseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtToConvert = textToConvert.getText().toString();
                String convertedTxt = MorseCode.alphaToMorse(txtToConvert);
                morseResult.setText(convertedTxt);
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToConvert.setText("");
                morseResult.setText("");
            }
        });


        morseToLightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFlashLight(statusLight);
                statusLight = !statusLight;
            }
        });
    }

    public void showNoFlashError() {
        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Oops!");
        alert.setMessage("Flash not available in this device...");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();
    }

    public void switchFlashLight(boolean statusLight) {
        try {
            mCameraManager.setTorchMode(mCameraId, statusLight);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}