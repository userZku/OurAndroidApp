package com.example.ourandroidapp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private volatile boolean stopThread = true;
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
    private AudioTrack short_track;
    private AudioTrack long_track;

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

        short_track = genTone(0.1);
        long_track = genTone(0.5);

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
            //button to convert the text to morse
            @Override
            public void onClick(View view) {
                String txtToConvert = textToConvert.getText().toString();
                convertedTxt = MorseCode.alphaToMorse(txtToConvert);
                morseResult.setText(convertedTxt);
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            //button to clear the converted text
            @Override
            public void onClick(View view) {
                textToConvert.setText("");
                morseResult.setText("");
            }
        });

        morseToLSBtn.setOnClickListener(new View.OnClickListener() {
            //button to start threads
            @Override
            public void onClick(View view) {
                if(convertedTxt!=null && stopThread) {
                    if (switchLight.isChecked()) {
                        startThread(convertedTxt, "light");
                    }
                    if (switchSound.isChecked()) {
                        startThread(convertedTxt, "sound");
                    }
                }
            }
        });

        stopLSBtn.setOnClickListener(new View.OnClickListener() {
            //button to stop threads
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

    public void timeDelay(long t) { //method to call during threads
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {}
    }

    public void startThread(String msg, String light_or_sound){
        //method called when morseToLSButton is pressed
        //to start the sound/light thread
        stopThread = false;

        if(light_or_sound=="sound"){
            SoundRunnable sound_runnable = new SoundRunnable(msg);
            new Thread(sound_runnable).start();
        }
        else if(light_or_sound=="light"){
            LightRunnable light_runnable = new LightRunnable(msg);
            new Thread(light_runnable).start();
        }
    }

    public void stopThread(){
        //method called when the stopLSButton is pressed
        stopThread = true;
    }

    class LightRunnable implements Runnable{
    //light thread
        String morseMsg;

        LightRunnable(String morseMsg){
            this.morseMsg = morseMsg;
        }

        @Override
        public void run(){
            for (int i=0; i < morseMsg.length(); i++) {
                if(stopThread){return;}
                if(morseMsg.charAt(i) == '.') {
                    switchFlashLight(true);
                    timeDelay(105);
                }
                else if(morseMsg.charAt(i) == '-') {
                    switchFlashLight(true);
                    timeDelay(505);
                }
                switchFlashLight(false);
                timeDelay(310);
            }
            stopThread = true;
        }
    }


//SOUND

    public AudioTrack genTone(double duration) {
    //to generate signal using AudioTrack class

        int sampleRate = 8000;
        double freqOfTone = 1000;
        double dnumSamples = Math.ceil(duration * sampleRate);
        int numSamples = (int) dnumSamples;
        double[] sample = new double[numSamples];
        byte[] generatedSnd = new byte[2 * numSamples];


        for (int i = 0; i < numSamples; ++i) {    // Fill the sample array
            sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalized.
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        int i = 0 ;
        int ramp = numSamples / 20 ;                                     // Amplitude ramp as a percent of sample count


        for (i = 0; i< ramp; ++i) {                                      // Ramp amplitude up (to avoid clicks)
            double dVal = sample[i];
            // Ramp up to maximum
            final short val = (short) ((dVal * 32767 * i/ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (i = i; i< numSamples - ramp; ++i) {                         // Max amplitude for most of the samples
            double dVal = sample[i];
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (i = i; i< numSamples; ++i) {                                // Ramp amplitude down
            double dVal = sample[i];
            // Ramp down to zero
            final short val = (short) ((dVal * 32767 * (numSamples-i)/ramp ));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);

        audioTrack.write(generatedSnd, 0, generatedSnd.length);

        return audioTrack;
    }


    class SoundRunnable implements Runnable {
    //sound thread
        String morseMsg;

        SoundRunnable(String morseMsg) {
            this.morseMsg = morseMsg;
        }

        @Override
        public void run() {
            for (int i = 0; i < morseMsg.length(); i++) {
                if (stopThread) {
                    return;
                }
                if (morseMsg.charAt(i) == '.') {
                    short_track.play();
                    timeDelay(100);
                    short_track.stop();
                    short_track.reloadStaticData();
                } else if (morseMsg.charAt(i) == '-') {
                    long_track.play();
                    timeDelay(500);
                    long_track.stop();
                    long_track.reloadStaticData();
                }
                timeDelay(300);
            }
            stopThread=true;
        }
    }

}