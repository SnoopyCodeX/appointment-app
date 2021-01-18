package com.appointment.app.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.appointment.app.R;
import com.appointment.app.activity.LoginActivity;

public class SplashActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        (new Thread(() -> {
            try {
                Thread.sleep(4000L);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }
}