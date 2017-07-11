package com.example.a10942.newproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
        TextView textView13 = (TextView) findViewById(R.id.textView13);
        TextView textView12 = (TextView) findViewById(R.id.textView12);
        TextView textView11 = (TextView) findViewById(R.id.textView11);
        TextView textView10 = (TextView) findViewById(R.id.textView10);
        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        TextView textView9 = (TextView) findViewById(R.id.textView9);
        ExitApplication.getInstance().addActivity(this);
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
