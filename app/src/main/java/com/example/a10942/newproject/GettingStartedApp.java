package com.example.a10942.newproject;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by BinaryHB on 16/9/13.
 */
public class GettingStartedApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "qlXAha6uLkazHrTkeLA6EMnh-gzGzoHsz", "L6MHLcv9erNMx92xsYpmL5Vr");
        AVOSCloud.setDebugLogEnabled(true);
    }
}
