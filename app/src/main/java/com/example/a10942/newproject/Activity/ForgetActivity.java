package com.example.a10942.newproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.example.a10942.newproject.R;
import com.example.a10942.newproject.Utils.ExitApplication;


/**
 * Created by 10942 on 2017/6/23 0023.
 * 忘记密码
 */

public class ForgetActivity extends AppCompatActivity {
    private Button reg_auth_code, zhuce;//获取验证码
    private EditText reg_phone, auth_code, reg_password, reg_passwords;
    String myphone, my_code, mypassword, mypasswordtoo;
    private Handler mHandler = new Handler();
    public int T = 60; //倒计时时长

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        ExitApplication.getInstance().addActivity(this);
        reg_phone = (EditText) findViewById(R.id.reg_phone);
        auth_code = (EditText) findViewById(R.id.auth_code);
        reg_password = (EditText) findViewById(R.id.reg_password);
        reg_passwords = (EditText) findViewById(R.id.reg_passwords);
        reg_auth_code = (Button) findViewById(R.id.reg_auth_code);
        zhuce = (Button) findViewById(R.id.zhuce);


        zhuce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myphone = reg_phone.getText().toString();
                my_code = auth_code.getText().toString();
                mypassword = reg_password.getText().toString();
                mypasswordtoo = reg_passwords.getText().toString();
                if (myphone.isEmpty() || my_code.isEmpty() || mypassword.isEmpty() || mypasswordtoo.isEmpty()) {
                    Toast.makeText(ForgetActivity.this, "还有未填写", Toast.LENGTH_SHORT).show();
                } else {
                    if (my_code.length() == 6) {
                        if (mypasswordtoo.equals(mypassword)) {
                            AVUser.resetPasswordBySmsCodeInBackground(my_code, mypassword, new UpdatePasswordCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        startActivity(new Intent(ForgetActivity.this, LoginActivity.class));
                                        ExitApplication.getInstance().exit();
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(ForgetActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(ForgetActivity.this, "验证码长度不对", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        reg_auth_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = reg_phone.getText().toString();
                AVUser.requestPasswordResetBySmsCodeInBackground(str, new RequestMobileCodeCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            new Thread(new MyCountDownTimer()).start();//开始执行
                        } else {
                            e.printStackTrace();
                            Toast.makeText(ForgetActivity.this, "e:" + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    /**
     * 自定义倒计时类，实现Runnable接口
     */
    class MyCountDownTimer implements Runnable {

        @Override
        public void run() {

            //倒计时开始，循环
            while (T > 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        reg_auth_code.setClickable(false);
                        reg_auth_code.setText(T + "秒");
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
                    reg_auth_code.setClickable(true);
                    reg_auth_code.setText("获取验证码");
                }
            });
            T = 60; //最后再恢复倒计时时长
        }
    }


}
