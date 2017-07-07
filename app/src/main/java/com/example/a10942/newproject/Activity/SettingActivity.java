package com.example.a10942.newproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.a10942.newproject.R;
import com.example.a10942.newproject.Utils.ExitApplication;

/**
 * Created by 10942 on 2017/7/7 0007.
 * 设置
 */

public class SettingActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceStat) {
        super.onCreate(savedInstanceStat);
        setContentView(R.layout.activity_aetting);
        ExitApplication.getInstance().addActivity(this);
    }
}
