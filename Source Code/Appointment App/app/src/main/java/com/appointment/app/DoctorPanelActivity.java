package com.appointment.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import com.appointment.app.adapter.SectionsPagerAdapter;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.Constants;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

public class DoctorPanelActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_panel);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        InternetReceiver.initiateSelf(this)
                .setOnConnectivityChangedListener(isConnected -> {
                    if(!isConnected)
                    {
                        DialogUtil.warningDialog(this, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Exit",
                                dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                                dlg -> {
                                    dlg.dismissWithAnimation();
                                    DoctorPanelActivity.this.finish();
                                }, false);
                    }
                    else
                        DialogUtil.dismissDialog();

                    if(isConnected)
                        AppInstance.getFCMToken(this);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_patient_doctor_panel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        DialogUtil.warningDialog(this, "Confirm Log Out", "Do you really want to log out of this account?", "Yes", "No",
                dlg -> {
                    AppInstance.logoutFCMToken(this, ((message, success) -> {
                        if(!success)
                        {
                            Toasty.error(this, message, Toasty.LENGTH_LONG).show();
                            dlg.dismissWithAnimation();
                            return;
                        }

                        PreferenceUtil.getPreference().edit().clear().apply();
                        dlg.dismissWithAnimation();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }));
                },
                SweetAlertDialog::dismissWithAnimation,
                false);
        return true;
    }
}