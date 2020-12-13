package com.appointment.app;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.appointment.app.adapter.AppointmentListAdapter;
import com.appointment.app.api.PatientAPI;
import com.appointment.app.fragment.dialog.PatientDialogFragment;
import com.appointment.app.model.AppointmentModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class PatientPanelActivity extends AppCompatActivity implements WaveSwipeRefreshLayout.OnRefreshListener
{
    private AppointmentListAdapter adapter;
    private RecyclerView appointmentList;
    private LinearLayout parentNoSchedule;
    WaveSwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_panel);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        // Floating Action Button
        findViewById(R.id.fab).setOnClickListener(view -> {
            showCreateNewAppointmentModal();
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(this::onRefresh);
        refreshLayout.setWaveColor(getColor(R.color.successColor));
        appointmentList = findViewById(R.id.patient_appointmentList);
        appointmentList.setLayoutManager(new LinearLayoutManager(this));
        appointmentList.setHasFixedSize(false);

        parentNoSchedule = findViewById(R.id.parent_no_schedule);

        PreferenceUtil.bindWith(this);
        InternetReceiver.initiateSelf(this)
                .setOnConnectivityChangedListener(isConnected -> {
                    if(!isConnected)
                        DialogUtil.warningDialog(this, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Cancel",
                                dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                                dlg -> {
                                    dlg.dismissWithAnimation();
                                    PatientPanelActivity.this.finish();
                                }, false);
                    else if(DialogUtil.isDialogShown())
                        DialogUtil.dismissDialog();


                    if(isConnected)
                    {
                        fetchAllAppointments();
                        AppInstance.getFCMToken(this);
                    }
                });
    }

    @Override
    public void onRefresh()
    {
        fetchAllAppointments();
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

    private void fetchAllAppointments()
    {
        if(!InternetReceiver.isConnected(this))
        {
            DialogUtil.warningDialog(this, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Cancel",
                    dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                    dlg -> {
                        dlg.dismissWithAnimation();
                        PatientPanelActivity.this.finish();
                    }, false);

            refreshLayout.setRefreshing(false);
            return;
        }

        if(!refreshLayout.isRefreshing())
            refreshLayout.setRefreshing(true);

        List<AppointmentModel> appointments = new ArrayList<>();
        int id = PreferenceUtil.getInt("user_id", 1);

        if(adapter == null)
        {
            adapter = new AppointmentListAdapter(this, appointments);
            appointmentList.setAdapter(adapter);
        }
        else
            adapter.clearList();
        appointmentList.scheduleLayoutAnimation();

        PatientAPI api = AppInstance.retrofit().create(PatientAPI.class);
        Call<ServerResponse<AppointmentModel>> call = api.fetchAllAppointments(id);
        call.enqueue(new Callback<ServerResponse<AppointmentModel>>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Response<ServerResponse<AppointmentModel>> response)
            {
                ServerResponse<AppointmentModel> server = response.body();
                DialogUtil.dismissDialog();
                refreshLayout.setRefreshing(false);

                if(server != null && !server.hasError)
                {
                    if (!server.data.isEmpty())
                    {
                        adapter.addAppointments(server.data);
                        parentNoSchedule.setVisibility(View.GONE);
                        appointmentList.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        parentNoSchedule.setVisibility(View.VISIBLE);
                        appointmentList.setVisibility(View.GONE);
                    }
                }
                else if(server != null && server.hasError)
                {
                    if(server.message.toLowerCase().contains("no scheduled") || server.message.toLowerCase().contains("no records"))
                    {
                        parentNoSchedule.setVisibility(View.VISIBLE);
                        appointmentList.setVisibility(View.GONE);
                    }
                    else
                    {
                        DialogUtil.errorDialog(PatientPanelActivity.this, "Server Error", server.message, "Okay", false);
                        parentNoSchedule.setVisibility(View.VISIBLE);
                        appointmentList.setVisibility(View.GONE);
                    }
                }
                else
                    DialogUtil.errorDialog(PatientPanelActivity.this, "Server Error", "Server failed to respond to your request", "Okay", false);

                call.cancel();
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Throwable t)
            {
                DialogUtil.errorDialog(PatientPanelActivity.this, "Server Error", t.getMessage(), "Okay", false);
                refreshLayout.setRefreshing(false);
                call.cancel();
            }
        });
    }

    private void showCreateNewAppointmentModal()
    {
        PatientDialogFragment createFragmentDialog = PatientDialogFragment.getInstance();
        createFragmentDialog.show(getSupportFragmentManager(), PatientDialogFragment.class.getSimpleName());
        refreshLayout.setRefreshing(false);
    }
}