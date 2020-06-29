package com.example.crofo_app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Message;

import android.os.Handler;

public class SplashActivity extends Activity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // 세로모드고정
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.splash);

        initialize();
    }

    private void initialize()
    {
        Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                finish();    // 액티비티 종료
            }
        };

        handler.sendEmptyMessageDelayed(0, 5000);    // ms, 3초후 종료시킴
    }
}
