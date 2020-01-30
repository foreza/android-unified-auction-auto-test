package com.inmobise.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aerserv.sdk.AerServConfig;
import com.aerserv.sdk.AerServEvent;
import com.aerserv.sdk.AerServEventListener;
import com.aerserv.sdk.AerServInterstitial;
import com.aerserv.sdk.AerServSdk;
import com.aerserv.sdk.AerServTransactionInformation;
import com.aerserv.sdk.AerServVirtualCurrency;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity {

    boolean isInitialized;
    private AerServInterstitial interstitial;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isInitialized) {
            initializeSDK();
            Log.v(TestConstants.LOG_TAG, "Initializing SDK");

//            setupMetricAPI();
//            Log.v(TestConstants.LOG_TAG, "Initializing Retrofit");


        }

    }

    // Init the InMobi Mediation SDK
    void initializeSDK(){
        AerServSdk.init(this, TestConstants.default_test_app_id);
    }


    // View events


    public void beginAutomatedTest(View view){

        Log.v(TestConstants.LOG_TAG, "Beginning test at " + getCurrentTimeInMS().toString());
        createInterstitial();


    }


    void createInterstitial(){

        // Null out the previous interstitial
        if (interstitial != null){
            interstitial.kill(); // Doesn't technically do anything
            interstitial = null;
            Log.v(TestConstants.LOG_TAG, "Nulling out previous interstitial");

        }


        // Make sure all ad calls are done from the main thread
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMetricStartTime();
                interstitial = new AerServInterstitial(createInterstitialConfigWithPlacement(TestConstants.default_test_interstitial_id, createInterstitialListener()));
            }

            });


    }


    void showInterstitial() {

        if (interstitial != null){

            // Also show the interstitial from the main thread
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v(TestConstants.LOG_TAG, "Showing loaded interstitial");

                    interstitial.show();
                }

            });

        }

        Log.e(TestConstants.LOG_TAG, "Failed, interstitial not loaded yet");


    }


    // Create the config with the listener and return it
    AerServConfig createInterstitialConfigWithPlacement(String plc, AerServEventListener listener){

        AerServConfig config = new AerServConfig(MainActivity.this, plc)
                .setAPSAdResponses(null)
                .setEventListener(listener)
                .setPreload(true);

        return config;
    }

    // Create the listener and return it
     AerServEventListener createInterstitialListener(){
          AerServEventListener interstitialListener = new AerServEventListener(){
             @Override
             public void onAerServEvent(final AerServEvent event, final List<Object> args){

                 MainActivity.this.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         String msg = null;
                         AerServVirtualCurrency vc = null;
                         AerServTransactionInformation ti = null;
                         switch (event) {
                             case PRELOAD_READY:
                                 msg = "PRELOAD_READY event fired with args: " + args.toString();
                                 setMetricEndTime();
                                 calculateElapsedTime();
                                 PostSuccessMetricTask task = new PostSuccessMetricTask();
                                 task.execute();
                                 showInterstitial();        // Show the interstitial as soon as Main thread is hit
                                 break;
                             case AD_FAILED:
                                 if (args.size() > 1) {
                                     Integer adFailedCode =
                                             (Integer) args.get(AerServEventListener.AD_FAILED_CODE);
                                     String adFailedReason =
                                             (String) args.get(AerServEventListener.AD_FAILED_REASON);
                                     msg = "Ad failed with code=" + adFailedCode + ", reason=" + adFailedReason;
                                 } else {
                                     msg = "Ad Failed with message: " + args.get(0).toString();
                                     setMetricEndTime();
                                     calculateElapsedTime();
                                      PostFailMetricTask failTask = new PostFailMetricTask();
                                     failTask.execute();

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
                             default:
                                 msg = event.toString() + " event fired with args: " + args.toString();
                         }
                         Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                         Log.d(TestConstants.LOG_TAG, msg);
                     }
                 });
             }
         };

          return interstitialListener;
     }



     // Metric calculation

    Long metricStartTime;
    Long metricEndTime;
    Long metricElapsedTime;


    public Long getCurrentTimeInMS() {
        return System.currentTimeMillis();
    }

    public void setMetricStartTime(){
        metricStartTime = getCurrentTimeInMS();
    }

    public void setMetricEndTime(){
        metricEndTime = getCurrentTimeInMS();
    }

    public void calculateElapsedTime(){
        metricElapsedTime = metricEndTime - metricStartTime;
    }

    public void postMetricResultsToRemoteWithStatus(boolean status){


        try {
            URL url = new URL(TestConstants.test_metric_endpoint);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject postBody = new JSONObject();

            postBody.put("request_startTime", metricStartTime);
            postBody.put("request_endTime", metricEndTime);
            postBody.put("request_totalTimeElapsed", metricElapsedTime);
            postBody.put("device_name", "Android Device");
            postBody.put("device_ip", "TBD");
            postBody.put("device_platform", "Android");
            postBody.put("ad_request_placement", TestConstants.default_test_interstitial_id);
            postBody.put("ad_request_geo", "USA");
            postBody.put("ad_delivery_status", status);

            Log.i("JSON", postBody.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(postBody.toString());

            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG" , conn.getResponseMessage());

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public class PostSuccessMetricTask extends AsyncTask<Boolean, Void, Void> {

        private Exception exception;

        protected Void doInBackground(Boolean... params) {
            try {
                postMetricResultsToRemoteWithStatus(true);
            } catch (Exception e) {
                this.exception = e;
            } finally {
            }
            return null;
        }

    }

    public class PostFailMetricTask extends AsyncTask<Boolean, Void, Void> {

        private Exception exception;

        protected Void doInBackground(Boolean... params) {
            try {
                postMetricResultsToRemoteWithStatus(false);
            } catch (Exception e) {
                this.exception = e;
            } finally {
            }
            return null;
        }

    }


}
