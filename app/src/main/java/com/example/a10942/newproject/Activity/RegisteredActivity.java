package com.example.a10942.newproject.Activity;

/**
 * Created by 10942 on 2017/6/23 0023.
 */


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SignUpCallback;
import com.example.a10942.newproject.R;
import com.example.a10942.newproject.Utils.SPUtils;
import com.example.a10942.newproject.Utils.Utils;

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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);
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
        zhuce = (Button) findViewById(R.id.zhuce);//注册
        regpasswords = (EditText) findViewById(R.id.reg_passwords);//重复密码
        regpassword = (EditText) findViewById(R.id.reg_password);//密码
        authcode = (EditText) findViewById(R.id.auth_code);//验证码
        regauthcode = (Button) findViewById(R.id.reg_auth_code);//获取验证码
        regauthcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = regphone.getText().toString();
                try {
                    toast("成功");
                    AVOSCloud.requestSMSCode(phone, "测试平台Demo 默认签名", "winelx", 10);  // 10 分钟内有效

                } catch (AVException e) {
                    e.printStackTrace();
                }
            }
        });
        regphone = (EditText) findViewById(R.id.reg_phone);//我的手机号
    }

    /**
     * id的点击事件
     */
    private void Onclick() {
        //注册
        zhuce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passWorld = regpassword.getText().toString();
                String usrName = regphone.getText().toString();
                String auto = authcode.getText().toString();
                Judge(passWorld, usrName, auto);
            }
        });

    }

    //对输入框的数据进行判读
    private void Judge(final String password, final String usrname, final String auto) {
        if (password.isEmpty() || usrname.isEmpty() || auto.isEmpty()) {//判断是否为空
//            Toast.makeText(this, "手机号和密码不能为空", Toast.LENGTH_SHORT).show();
            toast("还有未填项");
        } else {
            if (utils.isInteger(auto) != true) {//判读验证码是否为整数
                toast("验证码只能为数字");
            } else {
                if (auto.length() == 6) {//判断验证码长度

                    if (password.length() >= 6) {   //判断密码长度

                        if (password.equals(password)) {//判断两次密码是否一致
                            AVOSCloud.verifySMSCodeInBackground(auto, usrname, new AVMobilePhoneVerifyCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        toast("成功");
                                        registered(usrname, password, auto);//注册并登陆
                                    } else {
                                        e.printStackTrace();
                                        toast("失败");
                                    }
                                }
                            });
                            //两次密码正确
                        } else {
                            //两次面不一致
                            toast("密码输入不一致");
                        }
                    } else {

                        toast("密码必须大于6位");
                    }


                } else {
                    toast("验证码长度不正确");
                }
            }

        }
    }

    void registered(final String usrname, final String password, final String auto) {
        AVUser user = new AVUser();// 新建 AVUser 对象实例
        user.setUsername(usrname);// 设置用户名
        user.setPassword(password);// 设置密码
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    // 注册成功，把用户对象赋值给当前用户 AVUser.getCurrentUser()
                    spUtils.clear(context);
                    spUtils.put(context, "userName", usrname);
                    spUtils.put(context, "password", password);
                    startActivity(new Intent(RegisteredActivity.this, IndexActivity.class));
                    toast("注册成功");
                } else {
                    // 失败的原因可能有多种，常见的是用户名已经存在。
                    toast(e.getMessage());
                }
            }
        });
    }

    void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }


}