package com.example.a10942.newproject.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.example.a10942.newproject.R;
import com.example.a10942.newproject.Utils.SPUtils;


/**
 * Created by 10942 on 2017/6/23 0023.
 * 登陆
 */

public class LoginActivity extends AppCompatActivity {
    ImageView Loginweixin, LoginQQ;
    TextView LoginForgotpassword, Loginregistered;
    EditText LoginPassword, LoginUsername;
    Button Login;
    String TGA = "LoginActivity";
    SPUtils spUtils;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //初始化
        init();
        //查找id
        findView();
        //设置点击事件
        Onclick();
    }

    //界面初始化准备
    private void init() {
        context = LoginActivity.this;
        spUtils = new SPUtils();
    }

    /**
     * id初始化
     */
    private void findView() {
        //微信
        Loginweixin = (ImageView) findViewById(R.id.Login_weixin);
        //QQ
        LoginQQ = (ImageView) findViewById(R.id.Login_QQ);
        //忘记密码
        LoginForgotpassword = (TextView) findViewById(R.id.Login_Forgot_password);
        //注册
        Loginregistered = (TextView) findViewById(R.id.Login_registered);
        //登陆
        Login = (Button) findViewById(R.id.Login);
        //密码
        LoginPassword = (EditText) findViewById(R.id.Login_Password);
        //用户名
        LoginUsername = (EditText) findViewById(R.id.Login_Username);

    }

    /**
     * 设置点击事件
     */
    private void Onclick() {
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String password = LoginPassword.getText().toString();
                final String username = LoginUsername.getText().toString();
                AVUser.logInInBackground(username, password, new LogInCallback<AVUser>() {
                    @Override
                    public void done(AVUser avUser, AVException e) {
                        if (e == null) {
                            spUtils.clear(context);
                            spUtils.put(context, "userName", username);
                            spUtils.put(context, "password", password);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            LoginActivity.this.finish();
                        } else {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        //忘记密码
        LoginForgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgetActivity.class));
            }
        });
        //注册账号
        Loginregistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisteredActivity.class));
            }
        });
    }
}
