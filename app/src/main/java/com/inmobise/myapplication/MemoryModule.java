package com.inmobise.myapplication;

import android.os.Debug;
import android.util.Log;

import java.util.ArrayList;



class MemoryModule {

    // Singleton Methods
    private static String tag = "MemoryModule";
    private static final MemoryModule ourInstance = new MemoryModule();

    static MemoryModule getInstance() {
        return ourInstance;
    }

    private boolean sessionActive;                      // Track whether the session is active or not
    private long startingMemory;                        // Stores the initial starting heap state
    private long currentPeakDiff;                       // Temporary variable to store the current peak diff between currentMemory and starting Memory

    ArrayList peakMemCaptureList;


    // Constructor
    private MemoryModule() {
        peakMemCaptureList = new ArrayList();
    }


    public String getCurrentAppHeapUsage(){
        return String.valueOf((util_getCurrentAppHeapUsage()));
    }

    public String getStartingPeakHeapUsage(){
        return String.valueOf(startingMemory);
    }

    public String getCurrentPeakHeapUsage(){
        return String.valueOf(currentPeakDiff);
    }


    public void captureStartingMemory(){

        if (sessionActive){
            Log.e(tag, "Session is active; cannot begin new capture");
            return;
        } else {
            sessionActive = true;
            startingMemory = util_getCurrentAppHeapUsage();
            currentPeakDiff = 0;
            Log.d(tag, "startingMemory: " + String.valueOf(startingMemory));
        }

    }

    public void endMemoryCaptureAndStore(){

        if (!sessionActive){
            Log.e(tag, "Session is not active; cannot end");
        } else {
            peakMemCaptureList.add(currentPeakDiff);
            Log.d(tag, "Newest entry: " + getCurrentPeakHeapUsage());
            sessionActive = false;
            debug_printAllMemoryCapture();
        }

    }


    // Get the current difference
    public void calculateAndUpdateCurrentDiff(){

        if (!sessionActive){
            Log.e(tag, "Session is not active; cannot perform update");
        } else {
            long currentDiff = util_getCurrentAppHeapUsage() - startingMemory;

            if (currentDiff > currentPeakDiff) {
                currentPeakDiff = currentDiff;
                Log.d(tag, "updated peak diff: " + getCurrentPeakHeapUsage());
            } else {
                Log.d(tag, "peak diff is still: " + getCurrentPeakHeapUsage());

            }
        }


    }



    // This method retrieves the device's current app usage size
    private long util_getCurrentAppHeapUsage(){
        return Debug.getNativeHeapSize() - Debug.getNativeHeapFreeSize();
    }



    public void debug_printAllMemoryCapture(){

        Log.d(tag, "printAllMemoryCapture");

        for (int i = 0; i < peakMemCaptureList.size(); ++i) {
            Log.d(tag, "Entry number: " + String.valueOf(i) + " |  " + String.valueOf(peakMemCaptureList.get(i)));
        }

    }



    // to remove if we don't need

    // This method retrieves the device's total heap size (not the app)
    public String getTotalHeapSizeString(){
        return String.valueOf((util_getTotalHeapSize()));
    }


    private long util_getTotalHeapSize(){
        return Debug.getNativeHeapSize();
    }

}


// TODO
class MemorySessionObject {

    private long startingMemory;                        // Stores the initial starting heap state
    private long currentPeakDiff;                       // Temporary variable to store the current peak diff between currentMemory and starting Memory

}