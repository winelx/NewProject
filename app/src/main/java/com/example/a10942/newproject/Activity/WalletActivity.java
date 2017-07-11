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
//                PayResult payResult = new PayResult((Map<String, String>) msg.obj);
//                /**
//                 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
//                 */
//                String resultInfo = payResult.getResult();// 同步返回需要验证的信息
//                String resultStatus = payResult.getResultStatus();
//                // 判断resultStatus 为9000则代表支付成功
//                if (TextUtils.equals(resultStatus, "9000")) {
//                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
//                    Toast.makeText(WalletActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
//                } else {
//                    // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
//                    Toast.makeText(WalletActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
//                }
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
