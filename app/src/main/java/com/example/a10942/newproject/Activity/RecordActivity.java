package com.example.a10942.newproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.a10942.newproject.Utils.ExitApplication;

/**
 * Created by 10942 on 2017/7/7 0007.
 * 记录
 */

public class RecordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExitApplication.getInstance().addActivity(this);

    }
}
