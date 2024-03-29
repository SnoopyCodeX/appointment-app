package com.appointment.app.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.adapter.AppointmentListAdapter;
import com.appointment.app.api.DoctorAPI;
import com.appointment.app.model.AppointmentModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.Constants;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;
import com.google.gson.Gson;

import java.util.ArrayList;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class DoctorPendingFragment extends Fragment implements WaveSwipeRefreshLayout.OnRefreshListener
{
    private WaveSwipeRefreshLayout refreshLayout;
    private AppointmentListAdapter adapter;
    private RecyclerView appointmentList;
    private LinearLayout noSchedule;

    private DoctorPendingFragment()
    {}

    public static DoctorPendingFragment newInstance()
    {
        return new DoctorPendingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_doctor_pending, container, false);
        PreferenceUtil.bindWith(getContext());

        refreshLayout = root.findViewById(R.id.refresh_layout);
        appointmentList = root.findViewById(R.id.appointment_list);
        noSchedule = root.findViewById(R.id.parent_no_schedule);

        refreshLayout.setOnRefreshListener(this::onRefresh);
        refreshLayout.setWaveColor(getContext().getResources().getColor(R.color.successColor));
        appointmentList.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentList.setHasFixedSize(false);

        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_APPOINTMENT_CANCEL);
        filter.addAction(Constants.ACTION_APPOINTMENT_NEW);
        filter.addAction(Constants.ACTION_APPOINTMENT_UPDATE);
        getContext().registerReceiver(appointmentStatusReceiver, filter);

        fetchPendingAppointments();
        refreshLayout.setRefreshing(true);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        getContext().unregisterReceiver(appointmentStatusReceiver);
    }

    @Override
    public void onRefresh()
    {
        fetchPendingAppointments();
        refreshLayout.setRefreshing(true);
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE})
    private void fetchPendingAppointments()
    {
        if(!InternetReceiver.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Disconnected", "You are not connected to an active network", "Wifi Settings",
                    dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                    false);

            refreshLayout.setRefreshing(false);
            return;
        }

        if(adapter == null)
        {
            adapter = new AppointmentListAdapter(((AppCompatActivity) getActivity()), new ArrayList<>());
            appointmentList.setAdapter(adapter);
        }
        else
            adapter.clearList();
        appointmentList.scheduleLayoutAnimation();

        DoctorAPI api = AppInstance.retrofit().create(DoctorAPI.class);
        Call<ServerResponse<AppointmentModel>> call = api.fetchPendingAppointments(PreferenceUtil.getInt("user_id", 0));
        call.enqueue(new Callback<ServerResponse<AppointmentModel>>() {
            @Override
            public void onResponse(Call<ServerResponse<AppointmentModel>> call, Response<ServerResponse<AppointmentModel>> response)
            {
                ServerResponse<AppointmentModel> server = response.body();
                refreshLayout.setRefreshing(false);
                call.cancel();

                if(server != null && !server.hasError)
                {
                    if(server.data != null && !server.data.isEmpty())
                    {
                        adapter.addAppointments(server.data);
                        noSchedule.setVisibility(View.GONE);
                        appointmentList.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        noSchedule.setVisibility(View.VISIBLE);
                        appointmentList.setVisibility(View.GONE);
                    }
                }
                else if(server != null && server.hasError)
                {
                    if(server.message.toLowerCase().contains("no scheduled") || server.message.toLowerCase().contains("no records"))
                    {
                        noSchedule.setVisibility(View.VISIBLE);
                        appointmentList.setVisibility(View.GONE);
                    }
                    else
                    {
                        DialogUtil.errorDialog(getContext(), "Server Error", server.message, "Okay", false);
                        noSchedule.setVisibility(View.VISIBLE);
                        appointmentList.setVisibility(View.GONE);
                    }
                }
                else
                    DialogUtil.errorDialog(getContext(), "Process Unsuccessful", "Server failed to respond to your request", "Okay", false);
            }

            @Override
            public void onFailure(Call<ServerResponse<AppointmentModel>> call, Throwable t)
            {
                DialogUtil.errorDialog(getContext(), "Request Failed", t.getLocalizedMessage(), "Okay", false);
                refreshLayout.setRefreshing(false);
                call.cancel();
            }
        });
    }

    private BroadcastReceiver appointmentStatusReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if(action != null && !action.isEmpty())
            {
                String jsonData = intent.getExtras().getString("data");
                AppointmentModel appointment = new Gson().fromJson(jsonData, AppointmentModel.class);
                Log.i(DoctorPendingFragment.class.getSimpleName(), action + " -> " + jsonData);

                switch(action)
                {
                    case Constants.ACTION_APPOINTMENT_CANCEL:
                        adapter.removeAppointment(adapter.getIndexOf(appointment.id));
                    break;

                    case Constants.ACTION_APPOINTMENT_UPDATE:
                        adapter.moveAppointmentToTop(appointment);
                    break;

                    case Constants.ACTION_APPOINTMENT_NEW:
                        adapter.addAppointment(appointment);
                    break;
                }
            }
        }
    };
}