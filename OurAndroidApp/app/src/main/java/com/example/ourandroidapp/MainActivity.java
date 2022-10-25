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
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private volatile boolean stopThread = false;
    public static CameraManager mCameraManager;
    public static String mCameraId;
    private TextView textToConvert;
    private TextView morseResult;
    private String convertedTxt;
    private Button toMorseBtn;
    private Button clearBtn;
    private Button morseToLSBtn;
    private Button stopLSBtn;
    private Switch switchLight;
    private Switch switchSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToConvert = findViewById(R.id.textToConvert);
        morseResult = findViewById(R.id.morseResult);
        toMorseBtn = findViewById(R.id.toMorseBtn);
        clearBtn = findViewById(R.id.clearBtn);
        morseToLSBtn = findViewById(R.id.morseToLSBtn);
        stopLSBtn = findViewById(R.id.stopLSBtn);
        switchLight = findViewById(R.id.switchLight);
        switchSound = findViewById(R.id.switchSound);

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
                convertedTxt = MorseCode.alphaToMorse(txtToConvert);
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

        morseToLSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(switchLight.isChecked()){
                    startThread(convertedTxt);
                }
                if(switchSound.isChecked()){
                    // TODO
                }
            }
        });

        stopLSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopThread();
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

    public void switchFlashLight(boolean status) {
        try {
            mCameraManager.setTorchMode(mCameraId, status);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    //Multithread

    public void timeDelay(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {}
    }

    public void startThread(String msg){
        stopThread = false;
        MainRunnable runnable = new MainRunnable(msg);
        new Thread(runnable).start();
    }

    public void stopThread(){
        stopThread = true;
    }

    class MainRunnable implements Runnable{

        String morseMsg;

        MainRunnable(String morseMsg){
            this.morseMsg = morseMsg;
        }

        @Override
        public void run(){
            for (int i=0; i < morseMsg.length(); i++) {
                if(stopThread){return;}
                if(morseMsg.charAt(i) == '.') {
                    switchFlashLight(true);
                    timeDelay(100);
                }
                else if(morseMsg.charAt(i) == '-') {
                    switchFlashLight(true);
                    timeDelay(500);
                }
                switchFlashLight(false);
                timeDelay(300);
                //Log.d("tag", String.valueOf(test));
            }
        }
    }
}