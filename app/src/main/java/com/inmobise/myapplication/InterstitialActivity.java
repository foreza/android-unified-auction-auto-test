package com.inmobise.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aerserv.sdk.AerServConfig;
import com.aerserv.sdk.AerServEvent;
import com.aerserv.sdk.AerServEventListener;
import com.aerserv.sdk.AerServInterstitial;
import com.aerserv.sdk.AerServTransactionInformation;

import java.util.ArrayList;
import java.util.List;

public class InterstitialActivity extends AppCompatActivity {

    boolean isLoaded;
    private AerServInterstitial interstitial;
    public MemoryModule memoryModule;

    // Click Buttons to create interstitials

    public void createDefault(View view){
        createInterstitialForPlacement(TestConstants.default_test_interstitial_id);
    }

    public void createAdMob(View view){
        createInterstitialForPlacement(TestConstants.admob_test_int);
    }

    public void createAdColony(View view){
        createInterstitialForPlacement(TestConstants.adcolony_test_int);
    }

    public void createFacebook(View view){
        createInterstitialForPlacement(TestConstants.fb_test_int);
    }

    public void createVungle(View view){
        createInterstitialForPlacement(TestConstants.vungle_test_int);
    }

    public void createFull(View view){
        createInterstitialForPlacement(TestConstants.full_test_interstitial_id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);



        memoryModule = MemoryModule.getInstance();
        memoryModule.calculateAndUpdateCurrentDiff();
        memoryModule.endMemoryCaptureAndStore();

        isLoaded = false;
        setShowState(false);
    }

    public void showInterstitial(View view){
        if (isLoaded && interstitial != null){
            interstitial.show();

            isLoaded = false;
            setShowState(false);
        } else {
            // Handle error
        }
    }

    public void setShowState(boolean state){

        if (state) {
            findViewById(R.id.button_show_int).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.button_show_int).setVisibility(View.INVISIBLE);
        }

    }

    private void createInterstitialForPlacement(String placement){
        isLoaded = false;
        setShowState(false);

        if (interstitial != null){
            interstitial = null;
            Log.v(TestConstants.LOG_TAG, "Nulling out previous interstitial");

        }

        memoryModule.captureStartingMemory();

        AerServConfig config = new AerServConfig(this, placement)
                .setAPSAdResponses(null)
                .setEventListener(createInterstitialListener())
                .setPreload(true);

        interstitial = new AerServInterstitial(config);

    }

    AerServEventListener createInterstitialListener(){
        AerServEventListener interstitialListener = new AerServEventListener(){
            @Override
            public void onAerServEvent(final AerServEvent event, final List<Object> args){

                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = null;
                        AerServTransactionInformation ti = null;
                        switch (event) {
                            case PRELOAD_READY:
                                setShowState(true);
                                isLoaded = true;
                                msg = "PRELOAD_READY event fired with args: " + args.toString();
                                break;
                            case AD_FAILED:
                                isLoaded = false;
                                setShowState(false);
                                memoryModule.calculateAndUpdateCurrentDiff();
                                memoryModule.endMemoryCaptureAndStore();

                                if (args.size() > 1) {
                                    Integer adFailedCode = (Integer) args.get(AerServEventListener.AD_FAILED_CODE);
                                    String adFailedReason = (String) args.get(AerServEventListener.AD_FAILED_REASON);
                                    msg = "Ad failed with code=" + adFailedCode + ", reason=" + adFailedReason;
                                } else {
                                    msg = "Ad Failed with message: " + args.get(0).toString();
                                }
                                break;
                            case LOAD_TRANSACTION:
                                ti = (AerServTransactionInformation) args.get(0);
                                msg = "Load Transaction Information PLC has:"
                                        + "\n buyerName=" + ti.getBuyerName()
                                        + "\n buyerPrice=" + ti.getBuyerPrice();
                                break;
                            case SHOW_TRANSACTION:
                                ti = (AerServTransactionInformation) args.get(0);
                                msg = "Show Transaction Information PLC has:"
                                        + "\n buyerName=" + ti.getBuyerName()
                                        + "\n buyerPrice=" + ti.getBuyerPrice();
                                break;
                            case AD_DISMISSED:
                                memoryModule.calculateAndUpdateCurrentDiff();
                                memoryModule.endMemoryCaptureAndStore();
                                msg = event.toString() + " event fired";
                                break;
                            default:
                                msg = event.toString() + " event fired with args: " + args.toString();
                                memoryModule.calculateAndUpdateCurrentDiff();
                        }

                        Toast.makeText(InterstitialActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.d(TestConstants.LOG_TAG, msg);
                    }
                });
            }
        };

        return interstitialListener;
    }


    TextView totalMemText;
    TextView peakMemText;
    TextView peakIdText;
    boolean memViewInit = false;





    public void initUsageDisplay() {
        totalMemText = findViewById(R.id.totalMem);
        peakMemText = findViewById(R.id.peakMem);
        peakIdText = findViewById(R.id.peakId);
        memViewInit = true;
//        totalMemText.setText(String.valueOf(getTotalHeapSize()));
    }

    public void updateUsageDisplay(String s) {

        if (memViewInit) {
//            peakMemText.setText(String.valueOf(getCurrentPeakHeap()));
//            peakIdText.setText(s);

        } else {
            initUsageDisplay();
        }

    }

}
