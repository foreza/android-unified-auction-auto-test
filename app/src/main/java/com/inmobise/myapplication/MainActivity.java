package com.inmobise.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.aerserv.sdk.AerServSdk;
import com.inmobi.sdk.InMobiSdk;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    boolean isInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isInitialized) {
            initializeSDK();
            Log.v(TestConstants.LOG_TAG, "Initializing SDK");

        }

    }

    private void initializeSDK(){
        AerServSdk.init(this, TestConstants.default_test_app_id);

        JSONObject consentObject = new JSONObject();
        try {
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
            consentObject.put("gdpr", "0");
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB, "??");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void startLatencyTest(View view){
        startActivity(new Intent(this, LatencyTestActivity.class));
    }

    public void startBannerTest(View view){
        startActivity(new Intent(this, BannerActivity.class));
    }

    public void startInterstitialTest(View view){
        startActivity(new Intent(this, InterstitialActivity.class));
    }




}
