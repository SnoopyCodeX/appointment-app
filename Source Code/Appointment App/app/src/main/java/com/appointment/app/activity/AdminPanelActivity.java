package com.appointment.app.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.adapter.DoctorListAdapter;
import com.appointment.app.api.AdminAPI;
import com.appointment.app.api.SpecialtyAPI;
import com.appointment.app.fragment.dialog.ChangePasswordDialogFragment;
import com.appointment.app.fragment.dialog.DoctorCreateDialogFragment;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.model.SpecialtyModel;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.Constants;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPanelActivity extends AppCompatActivity implements WaveSwipeRefreshLayout.OnRefreshListener
{
    private DoctorListAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout noRecords;
    private WaveSwipeRefreshLayout refreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        findViewById(R.id.fab).setOnClickListener(view -> showAddNewDoctor());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setWaveColor(getColor(R.color.successColor));
        recyclerView = findViewById(R.id.admin_doctorList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        noRecords = findViewById(R.id.parent_no_records);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        PreferenceUtil.bindWith(this);
        InternetReceiver.initiateSelf(this)
                .setOnConnectivityChangedListener(isConnected -> {
                    if(!isConnected)
                        DialogUtil.warningDialog(this, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Cancel",
                                dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                                dlg -> {
                                    dlg.dismissWithAnimation();
                                    AdminPanelActivity.this.finish();
                                }, false);
                    else if(DialogUtil.isDialogShown())
                        DialogUtil.dismissDialog();

                    if(isConnected)
                    {
                        refreshLayout.setRefreshing(true);
                        fetchAllDoctors();
                        AppInstance.getFCMToken(this);
                    }
                });

        if(!PreferenceUtil.getBoolean(Constants.PREF_KEY_LOGGED_IN, false))
        {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_patient_doctor_admin_panel, menu);
        return true;
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

    @Override
    public void onRefresh()
    {
        fetchAllDoctors();
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE})
    private void fetchAllDoctors()
    {
        if(!InternetReceiver.isConnected(this))
        {
            DialogUtil.warningDialog(this, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Cancel",
                    dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                    dlg -> {
                        dlg.dismissWithAnimation();
                        AdminPanelActivity.this.finish();
                    }, false);

            refreshLayout.setRefreshing(false);
            return;
        }

        if(!refreshLayout.isRefreshing())
            refreshLayout.setRefreshing(true);

        List<DoctorModel> doctors = new ArrayList<>();
        int id = PreferenceUtil.getInt("user_id", 0);

        if(adapter == null)
        {
            adapter = new DoctorListAdapter(this, doctors);
            recyclerView.setAdapter(adapter);
        }
        else
            adapter.clearList();
        recyclerView.scheduleLayoutAnimation();

        AdminAPI api = AppInstance.retrofit().create(AdminAPI.class);
        Call<ServerResponse<DoctorModel>> call = api.getAllDoctors(id);
        call.enqueue(new Callback<ServerResponse<DoctorModel>>()
        {
            @Override
            public void onResponse(@NonNull Call<ServerResponse<DoctorModel>> call, @NonNull Response<ServerResponse<DoctorModel>> response)
            {
                ServerResponse<DoctorModel> server = response.body();
                refreshLayout.setRefreshing(false);
                call.cancel();

                if(server != null && !server.hasError)
                    if(!server.data.isEmpty())
                    {
                        adapter.addAllDoctors(server.data);
                        recyclerView.setVisibility(View.VISIBLE);
                        noRecords.setVisibility(View.GONE);
                    }
                    else
                    {
                        recyclerView.setVisibility(View.GONE);
                        noRecords.setVisibility(View.VISIBLE);
                    }
                else if(server != null)
                    if(server.message.toLowerCase().contains("no records"))
                    {
                        recyclerView.setVisibility(View.GONE);
                        noRecords.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        DialogUtil.errorDialog(AdminPanelActivity.this, "Request Failed",  server.message, "Okay", false);
                        recyclerView.setVisibility(View.GONE);
                        noRecords.setVisibility(View.VISIBLE);
                    }
                else
                    DialogUtil.errorDialog(AdminPanelActivity.this, "Server Error", "Server failed to respond to your request, try again.", "Okay", false);
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse<DoctorModel>> call, @NonNull Throwable t)
            {
                DialogUtil.errorDialog(AdminPanelActivity.this, "Server Error", t.getMessage(), "Okay", false);
                refreshLayout.setRefreshing(false);
                call.cancel();
            }
        });
    }

    private void showAddNewDoctor()
    {
        if(!InternetReceiver.isConnected(this))
        {
            DialogUtil.warningDialog(this, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Cancel",
                    dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                    dlg -> {
                        dlg.dismissWithAnimation();
                        AdminPanelActivity.this.finish();
                    }, false);

            refreshLayout.setRefreshing(false);
            return;
        }

        DialogUtil.progressDialog(this, "Fetching medical fields...", this.getColor(R.color.warningColor), false);
        SpecialtyAPI api = AppInstance.retrofit().create(SpecialtyAPI.class);
        Call<ServerResponse<SpecialtyModel>> call = api.fetchMedicalFields();
        call.enqueue(new Callback<ServerResponse<SpecialtyModel>>()
        {
            @Override
            public void onResponse(@NonNull Call<ServerResponse<SpecialtyModel>> call, @NonNull Response<ServerResponse<SpecialtyModel>> response)
            {
                ServerResponse<SpecialtyModel> server = response.body();
                DialogUtil.dismissDialog();
                call.cancel();

                if(server != null && !server.hasError)
                {
                    List<SpecialtyModel> data = server.data;
                    String[] names = new String[data.size()];

                    for(int i = 0; i < data.size(); i++)
                        names[i] = data.get(i).name;

                    DoctorCreateDialogFragment.getInstance(names)
                                              .show(getSupportFragmentManager(), AdminPanelActivity.class.getName());
                }
                else if(server != null)
                    Toasty.error(AdminPanelActivity.this, server.message, Toasty.LENGTH_LONG).show();
                else
                    Toasty.warning(AdminPanelActivity.this, "An unexpected event has occured in the server", Toasty.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse<SpecialtyModel>> call, @NonNull Throwable t)
            {
                DialogUtil.errorDialog(AdminPanelActivity.this, "Server Error", t.getMessage(), "Okay", false);
                refreshLayout.setRefreshing(false);
                call.cancel();
            }
        });
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
                        AdminPanelActivity.this.finish();
                    }, false);

            refreshLayout.setRefreshing(false);
            return;
        }

        ChangePasswordDialogFragment.getInstance()
                .setRole(ChangePasswordDialogFragment.Role.ADMIN)
                .setOwnerId(PreferenceUtil.getInt("user_id", 0))
                .show(getSupportFragmentManager(), AdminPanelActivity.class.getName());
    }
}
