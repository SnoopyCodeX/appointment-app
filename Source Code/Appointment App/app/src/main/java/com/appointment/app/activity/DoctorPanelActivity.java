package com.appointment.app.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.adapter.SectionsPagerAdapter;
import com.appointment.app.fragment.dialog.ChangePasswordDialogFragment;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.widget.Toolbar;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(InternetReceiver.isConnected(this))
            AppInstance.getFCMToken(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        PreferenceUtil.bindWith(this);
        InternetReceiver.initiateSelf(this)
                .setOnConnectivityChangedListener(isConnected -> {
                    if(!isConnected)
                        DialogUtil.warningDialog(this, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Exit",
                                dlg -> {
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                },
                                dlg -> {
                                    dlg.dismissWithAnimation();
                                    DoctorPanelActivity.this.finish();
                                }, false);
                    else
                        DialogUtil.dismissDialog();
                });
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        InternetReceiver.unBindWith(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_patient_doctor_admin_panel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_logout:
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

            case R.id.action_change_password:
                changePassword();
                return true;
        }

        return false;
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE})
    private void changePassword()
    {
        if(!InternetReceiver.isConnected(this))
        {
            DialogUtil.warningDialog(this, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Cancel",
                    dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                    dlg -> {
                        dlg.dismissWithAnimation();
                        DoctorPanelActivity.this.finish();
                    }, false);

            return;
        }

        ChangePasswordDialogFragment.getInstance()
                .setRole(ChangePasswordDialogFragment.Role.DOCTOR)
                .setOwnerId(PreferenceUtil.getInt("user_id", 0))
                .show(getSupportFragmentManager(), DoctorPanelActivity.class.getName());
    }
}