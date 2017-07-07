package com.example.a10942.newproject.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.a10942.newproject.R;
import com.example.a10942.newproject.Utils.ExitApplication;

/**
 * Created by 10942 on 2017/7/7 0007.
 * 我的钱包
 */

public class WalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ming);
        ExitApplication.getInstance().addActivity(this);
        Button zhifubo = (Button) findViewById(R.id.zhifubo);
        ImageView mingback = (ImageView) findViewById(R.id.ming_back);

        zhifubo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mingback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
               
            }
        });
    }
}
