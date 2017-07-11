package com.example.a10942.newproject.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SignUpCallback;
import com.example.a10942.newproject.R;
import com.example.a10942.newproject.Utils.ExitApplication;
import com.example.a10942.newproject.Utils.SPUtils;
import com.example.a10942.newproject.Utils.Utils;

import static com.example.a10942.newproject.R.id.reg_auth_code;

/**
 * Created by 10942 on 2017/6/22 0022.
 * 注册
 */

public class RegisteredActivity extends Activity {
    EditText regpasswords, regpassword, authcode, regphone;
    Button zhuce, regauthcode;
    Utils utils;
    SPUtils spUtils;
    private Context context;
    private CheckBox checkBox;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);
        ExitApplication.getInstance().addActivity(this);
        context = RegisteredActivity.this;

        /**
         * 初始化界面
         */

        init();
        /**
         *    控件初始化，
         */

        findview();
        /**
         *控件点击事件
         */

        Onclick();

    }

    /**
     * 初始化界面
     */
    private void init() {
        utils = new Utils();
        spUtils = new SPUtils();//初始化sp；
    }

    /**
     * id实例化
     */
    private void findview() {
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        zhuce = (Button) findViewById(R.id.zhuce);//注册
        regpasswords = (EditText) findViewById(R.id.reg_passwords);//重复密码
        regpassword = (EditText) findViewById(R.id.reg_password);//密码
        authcode = (EditText) findViewById(R.id.auth_code);//验证码
        regauthcode = (Button) findViewById(reg_auth_code);//获取验证码
        regphone = (EditText) findViewById(R.id.reg_phone);//我的手机号
        regauthcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("成功");
                String passWorld = regpassword.getText().toString();
                String usrName = regphone.getText().toString();
                AVUser user = new AVUser();
                user.setUsername(usrName);
                user.setPassword(passWorld);
                user.setEmail(usrName + "@qq.com");
                // 其他属性可以像其他AVObject对象一样使用put方法添加
                user.put("mobilePhoneNumber", usrName);
                user.signUpInBackground(new SignUpCallback() {
                    public void done(AVException e) {
                        if (e == null) {
                            Toast.makeText(RegisteredActivity.this, "调用成功", Toast.LENGTH_SHORT).show();
                        } else {
                            // failed
                        }
                    }
                });
            }
        });
    }


    /**
     * id的点击事件
     */
    private void Onclick() {
        //注册
        zhuce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String passWorld = regpassword.getText().toString();
                final String usrName = regphone.getText().toString();
                final String auto = authcode.getText().toString();
                if (checkBox.isChecked() == true) {
                    AVUser.verifyMobilePhoneInBackground(auto, new AVMobilePhoneVerifyCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                spUtils.clear(context);
                                spUtils.put(context, "UserName", usrName);
                                spUtils.put(context, "UserPass", passWorld);
                                startActivity(new Intent(RegisteredActivity.this, IndexActivity.class));
                                finish();
                            } else {
                                Log.d("SMS", "Verified failed!");
                            }
                        }
                    });
                    Toast.makeText(RegisteredActivity.this, "用户协议", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(RegisteredActivity.this, "勾选用户协议", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * 自定义倒计时类，实现Runnable接口
     */
    private Handler mHandler = new Handler();
    public int T = 60; //倒计时时长

    class MyCountDownTimer implements Runnable {
        @Override
        public void run() {
            //倒计时开始，循环
            while (T > 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        regauthcode.setClickable(false);
                        regauthcode.setText(T + "秒");
                    }
                });
                try {
                    Thread.sleep(1000); //强制线程休眠1秒，就是设置倒计时的间隔时间为1秒。
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                T--;
            }
            //倒计时结束，也就是循环结束
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    regauthcode.setClickable(true);
                    regauthcode.setText("获取验证码");
                }
            });
            T = 60; //最后再恢复倒计时时长
        }
    }
}