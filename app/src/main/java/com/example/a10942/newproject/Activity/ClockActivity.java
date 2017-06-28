package com.example.a10942.newproject.Activity;

/**
 * Created by 10942 on 2017/6/23 0023.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.a10942.newproject.R;


/**
 * Created by 10942 on 2017/6/19 0019.
 * 借伞计时
 */

public class ClockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        TextView clockbutton = (TextView) findViewById(R.id.clock_button);
        //最后的参数一定要和发送方的相同，否则得到空值

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String rString = bundle.getString("Result");
        clockbutton.setText(rString);
        Log.i("sss", rString);

    }


}
