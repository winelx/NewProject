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
        AVOSCloud.initialize(this, "pd1VBE9G7gQcWt06uA3HKXQA-gzGzoHsz","BT2dmb5GqQz0IqwrwewOodjS");
        AVOSCloud.setDebugLogEnabled(true);

    }
}
