package com.w9565277.thedailyherald;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // on below line we are configuring our window to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        try {
            this.getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // on below line we are calling handler to run a task
        // for specific time interval
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // on below line we are
                // creating a new intent

                SharedPreferences sh = getSharedPreferences("heraldNewsData", Context.MODE_PRIVATE);
                String Email = sh.getString("email", "");
                String name = sh.getString("name", "");
                Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);

                if (!TextUtils.isEmpty(Email)) {
                    i = new Intent(SplashScreenActivity.this, MainActivity.class);
                }

                // on below line we are
                // starting a new activity.
                startActivity(i);

                // on the below line we are finishing
                // our current activity.
                finish();
            }
        }, 2000);
    }
}