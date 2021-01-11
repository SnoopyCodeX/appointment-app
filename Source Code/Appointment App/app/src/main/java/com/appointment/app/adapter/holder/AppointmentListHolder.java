package com.appointment.app.adapter.holder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Intent;
import android.provider.Settings;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.api.DoctorAPI;
import com.appointment.app.api.PatientAPI;
import com.appointment.app.api.SpecialtyAPI;
import com.appointment.app.fragment.dialog.PatientEditAppointmentFragment;
import com.appointment.app.model.AppointmentModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.model.SpecialtyModel;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.AlarmManagerUtil;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;
import com.robertlevonyan.views.chip.Chip;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import info.androidhive.fontawesome.FontTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings({"FieldMayBeFinal", "ALL"})
public class AppointmentListHolder extends BaseListHolder
{
    private List<AppointmentModel> items;
    private View itemView;

    // Parent Layout of the appointment and it's buttons
    private LinearLayout appointmentParent;
    private LinearLayout appointmentButtonParent;

    // Buttons of the appointment (decline, approve, edit, cancel)
    private FontTextView appointmentPositiveButton;
    private FontTextView appointmentNegativeButton;
    private FontTextView appointmentNeutralButton;

    // Text fields of the appointment
    private TextView patientName;
    private TextView patientGender;
    private Chip appointmentStatus;
    private TextView appointmentSchedule;

    // Profile Icon based on gender
    private ImageView patientIcon;

    // Parent Activity for Push Notification
    private AppCompatActivity activity;

    public AppointmentListHolder(@NonNull View itemView, List<AppointmentModel> items, @NonNull AppCompatActivity activity)
    {
        super(itemView);

        this.items = items;
        this.itemView = itemView;
        this.activity = activity;
        this.initViews();
    }

    @Override
    protected void clear()
    {}

    @SuppressLint("SetTextI18n")
    @Override
    public void onBind(int position)
    {
        super.onBind(position);

        try {
            PreferenceUtil.bindWith(activity);
            AppointmentModel appointment = items.get(position);
            patientName.setText("Name: " + appointment.name);
            patientGender.setText("Gender: " + appointment.gender);
            appointmentStatus.setText(appointment.status.toUpperCase());

            String schedule = String.format("%s @ %s", parseDate(appointment.date), parseTime(appointment.time));
            appointmentSchedule.setText("Schedule: " + schedule);

            appointmentButtonParent.setVisibility(appointment.isDoctor ? View.VISIBLE : (appointment.status.toUpperCase().equals(AppointmentModel.Status.APPROVED.name().toUpperCase()) ? View.GONE : View.VISIBLE));
            appointmentNeutralButton.setVisibility(appointment.isDoctor ? View.GONE : View.VISIBLE);

            if(appointment.status.toLowerCase().equals("approved"))
                appointmentStatus.setChipBackgroundColor(activity.getColor(R.color.successColor));
            if(appointment.status.toLowerCase().equals("declined"))
                appointmentStatus.setChipBackgroundColor(activity.getColor(R.color.errorColor));
            if(appointment.status.toLowerCase().equals("cancelled"))
                appointmentStatus.setChipBackgroundColor(activity.getColor(R.color.errorColor));
            if(appointment.status.toLowerCase().equals("pending"))
                appointmentStatus.setChipBackgroundColor(activity.getColor(R.color.warningColor));

            patientIcon.setImageResource(appointment.gender.toLowerCase().equals("male") ? R.drawable.person_male : R.drawable.person_female);

            if(appointment.isDoctor && appointment.status.toUpperCase().equals(AppointmentModel.Status.APPROVED.name().toUpperCase()))
            {
                appointmentNegativeButton.setText(R.string.fa_window_close_solid);
                appointmentNegativeButton.setVisibility(View.VISIBLE);
                appointmentPositiveButton.setVisibility(View.GONE);
                appointmentNeutralButton.setVisibility(View.GONE);
            }
            else if(appointment.isDoctor && appointment.status.toUpperCase().equals(AppointmentModel.Status.PENDING.name().toUpperCase()))
            {
                appointmentNegativeButton.setVisibility(View.VISIBLE);
                appointmentPositiveButton.setVisibility(View.VISIBLE);
                appointmentNeutralButton.setVisibility(View.GONE);
            }
            else if(!appointment.isDoctor && appointment.status.toUpperCase().equals(AppointmentModel.Status.PENDING.name().toUpperCase()))
            {
                appointmentNegativeButton.setVisibility(View.VISIBLE);
                appointmentPositiveButton.setVisibility(View.GONE);
                appointmentNeutralButton.setVisibility(View.VISIBLE);
            }
            else if(!appointment.isDoctor && appointment.status.toUpperCase().equals(AppointmentModel.Status.CANCELLED.name().toUpperCase()))
            {
                appointmentNegativeButton.setText(R.string.fa_trash_alt_solid);
                appointmentNegativeButton.setVisibility(View.VISIBLE);
                appointmentPositiveButton.setVisibility(View.GONE);
                appointmentNeutralButton.setVisibility(View.GONE);
            }
            else if(!appointment.isDoctor && appointment.status.toUpperCase().equals(AppointmentModel.Status.DECLINED.name().toUpperCase()))
            {
                appointmentNegativeButton.setText(R.string.fa_trash_alt_solid);
                appointmentNegativeButton.setVisibility(View.VISIBLE);
                appointmentPositiveButton.setVisibility(View.GONE);
                appointmentNeutralButton.setVisibility(View.GONE);
            }

            appointmentParent.setOnClickListener(view -> showAppointmentDetails(appointment));

            appointmentPositiveButton.setOnClickListener(
                    view -> DialogUtil.warningDialog(activity, "Confirm Action", "Are you sure you want to approve this appointment?", "Yes", "No",
                    dlg -> approveAppointment(appointment.id),
                    SweetAlertDialog::cancel,
                    false));

            appointmentNegativeButton.setOnClickListener(
                    view -> DialogUtil.warningDialog(activity, "Confirm Action", String.format("Do you really want to %s this appointment? This action can not be reverted.", appointment.isDoctor ? (appointment.status.toLowerCase().equals("approved") ? "cancel" : "decline") : (appointment.status.toLowerCase().equals("cancelled") || appointment.status.toLowerCase().equals("declined")) ? "delete" : "cancel"), "Yes", "No",
                    dlg -> {
                        if(appointment.isDoctor)
                            doctorCancelDeclineAppointment(appointment);
                        else
                            patientCancelAppointment(appointment.id);
                    },
                    SweetAlertDialog::cancel,
                    false));

            appointmentNeutralButton.setOnClickListener(view -> patientEditAppointment(appointment));
        } catch(Exception e) {
            e.printStackTrace();

            DialogUtil.dismissDialog();
            DialogUtil.errorDialog(activity, "Application Error", e.getMessage(), "Okay", false);
        }
    }

    private void initViews()
    {
        appointmentParent = itemView.findViewById(R.id.appointment_parent);
        appointmentButtonParent = itemView.findViewById(R.id.appointment_btn_parent);

        appointmentPositiveButton = itemView.findViewById(R.id.appointment_btn_positive);
        appointmentNegativeButton = itemView.findViewById(R.id.appointment_btn_negative);
        appointmentNeutralButton = itemView.findViewById(R.id.appointment_btn_neutral);

        patientName = itemView.findViewById(R.id.appointment_name);
        patientGender = itemView.findViewById(R.id.appointment_gender);
        appointmentStatus = itemView.findViewById(R.id.appointment_status);
        appointmentSchedule = itemView.findViewById(R.id.appointment_schedule);

        patientIcon = itemView.findViewById(R.id.appointment_profile);
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE})
    private void approveAppointment(int appointmentId)
    {
        if(!InternetReceiver.isConnected(activity))
        {
            DialogUtil.warningDialog(activity, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Cancel",
                    dlg -> activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                    dlg -> {
                        dlg.dismissWithAnimation();
                        activity.finish();
                    }, false);

            return;
        }

        DialogUtil.progressDialog(activity, "Approving appointment...", activity.getColor(R.color.successColor), false);
        DoctorAPI api = AppInstance.retrofit().create(DoctorAPI.class);
        Call<ServerResponse<AppointmentModel>> call = api.approveAppointment(PreferenceUtil.getInt("user_id", 0), appointmentId);
        call.enqueue(new Callback<ServerResponse<AppointmentModel>>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Response<ServerResponse<AppointmentModel>> response)
            {
                ServerResponse<AppointmentModel> server = response.body();
                DialogUtil.dismissDialog();

                if(server != null && !server.hasError)
                {
                    AppointmentModel appointment = server.data.get(0);
                    String datetime = String.format("%s %s", appointment.date, appointment.time);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d H:m:s");
                    Date date = null;
                    try {
                        date = sdf.parse(datetime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    AlarmManagerUtil.getInstance(activity).scheduleAlarm(calendar, appointment.id, "Appointment Reminder", "You have an scheduled appointment today with your patient!");

                    Toasty.success(activity, "Appointment has been approved successfully, refresh list now!", Toasty.LENGTH_LONG).show();
                }
                else if(server != null)
                    DialogUtil.warningDialog(activity, "Process Unsuccessful", server.message, "Okay", false);
                else
                    DialogUtil.errorDialog(activity, "Process Unsuccessful", "Server failed to respond to your request", "Okay", false);

                call.cancel();
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Throwable t)
            {
                DialogUtil.errorDialog(activity, "Request Failed", t.getLocalizedMessage(), "Okay", false);
                call.cancel();
            }
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE})
    private void doctorCancelDeclineAppointment(AppointmentModel appointment)
    {
        if(!InternetReceiver.isConnected(activity))
        {
            DialogUtil.warningDialog(activity, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Cancel",
                    dlg -> activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                    dlg -> {
                        dlg.dismissWithAnimation();
                        activity.finish();
                    }, false);

            return;
        }

        if(appointment.status.toLowerCase().equals("approved"))
        {
            DialogUtil.dismissDialog();

            new LovelyTextInputDialog(activity, R.style.TintTheme)
                    .setTopColorRes(R.color.white)
                    .setTitle("Cancel Appointment")
                    .setMessage("Type reason of cancellation below...")
                    .setIcon(appointment.gender.toLowerCase().equals("male") ? R.drawable.person_male : R.drawable.person_female)
                    .setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE|InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                    .setInputFilter("Reason of cancellation must not exceed 255 characters nor less than 10 characters", s -> s.length() >= 10 && s.length() <= 255)
                    .setConfirmButton("Submit", s -> {
                        appointment.reason = s;

                        DialogUtil.progressDialog(activity, "Cancelling appointment...", activity.getColor(R.color.successColor), false);
                        DoctorAPI api = AppInstance.retrofit().create(DoctorAPI.class);
                        Call<ServerResponse<AppointmentModel>> call = api.cancelAppointment(PreferenceUtil.getInt("user_id", 0), appointment.id);
                        call.enqueue(new Callback<ServerResponse<AppointmentModel>>() {
                            @Override
                            public void onResponse(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Response<ServerResponse<AppointmentModel>> response)
                            {
                                ServerResponse<AppointmentModel> server = response.body();
                                DialogUtil.dismissDialog();

                                if(server != null && !server.hasError) {
                                    AppointmentModel appointment = server.data.get(0);
                                    AlarmManagerUtil.getInstance(activity).cancelAlarm(appointment.id);
                                    Toasty.success(activity, "Appointment has been cancelled successfully, refresh list now!", Toasty.LENGTH_LONG).show();
                                }
                                else if(server != null)
                                    DialogUtil.warningDialog(activity, "Process Unsuccessful", server.message, "Okay", false);
                                else
                                    DialogUtil.errorDialog(activity, "Process Unsuccessful", "Server failed to respond to your request", "Okay", false);

                                call.cancel();
                            }

                            @Override
                            public void onFailure(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Throwable t)
                            {
                                DialogUtil.errorDialog(activity, "Request Failed", t.getLocalizedMessage(), "Okay", false);
                                call.cancel();
                            }
                        });
                    })
                    .setCancelable(false)
                    .setNegativeButton("Cancel", v -> {})
                    .show();

            return;
        }

        DialogUtil.progressDialog(activity, "Declining appointment...", activity.getColor(R.color.successColor), false);
        DoctorAPI api = AppInstance.retrofit().create(DoctorAPI.class);
        Call<ServerResponse<AppointmentModel>> call = api.declineAppointment(PreferenceUtil.getInt("user_id", 0), appointment.id);
        call.enqueue(new Callback<ServerResponse<AppointmentModel>>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Response<ServerResponse<AppointmentModel>> response)
            {
                ServerResponse<AppointmentModel> server = response.body();
                DialogUtil.dismissDialog();

                if(server != null && !server.hasError)
                    Toasty.success(activity, "Appointment has been declined, refresh list now!", Toasty.LENGTH_LONG).show();
                else if(server != null)
                    DialogUtil.warningDialog(activity, "Process Unsuccessful", server.message, "Okay", false);
                else
                    DialogUtil.errorDialog(activity, "Process Unsuccessful", "Server failed to respond to your request", "Okay", false);

                call.cancel();
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Throwable t)
            {
                DialogUtil.errorDialog(activity, "Request Failed", t.getLocalizedMessage(), "Okay", false);
                call.cancel();
            }
        });
    }

    private void showAppointmentDetails(AppointmentModel appointment)
    {
        SweetAlertDialog sweet = new SweetAlertDialog(activity, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        sweet.setTitleText(appointment.name);
        sweet.setContentText("");
        sweet.setConfirmText("Okay");
        sweet.setCancelable(false);
        sweet.setCanceledOnTouchOutside(false);

        StringBuilder str = new StringBuilder();
        str.append("<strong>").append("Schedule: ").append("</strong>").append(parseDate(appointment.date)).append(" @ ").append(parseTime(appointment.time)).append("<br/>")
                .append("<strong>").append("Gender: ").append("</strong>").append(appointment.gender.substring(0,1).toUpperCase()).append(appointment.gender.substring(1, appointment.gender.length()).toLowerCase()).append("<br/>")
                .append("<strong>").append("Status: ").append("</strong>").append(appointment.status.substring(0,1).toUpperCase()).append(appointment.status.substring(1, appointment.status.length()).toLowerCase()).append("<br/>")
                .append("<strong>").append("Age: ").append("</strong>").append(appointment.age).append(" yrs old").append("<br/>")
                .append("<strong>").append("Address: ").append("</strong>").append(appointment.address).append("<br/>");

        if(!appointment.isDoctor && appointment.status.toLowerCase().equals(AppointmentModel.Status.CANCELLED.name().toLowerCase()))
            str.append("<strong>").append("Reason of cancellation: ").append("</strong><br/><center>").append(appointment.reason).append("</center><br/>");
        else
            str.append("<strong>").append("Reason: ").append("</strong>").append(appointment.reason).append("<br/>");

        sweet.setCustomImage(appointment.gender.toLowerCase().equals("male") ? R.drawable.person_male : R.drawable.person_female);
        sweet.show();

        TextView content = sweet.findViewById(R.id.content_text);
        content.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        content.setText(Html.fromHtml(str.toString()));
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE})
    private void patientCancelAppointment(int appointmentId)
    {
        if(!InternetReceiver.isConnected(activity))
        {
            DialogUtil.warningDialog(activity, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Cancel",
                    dlg -> activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)),
                    dlg -> {
                        dlg.dismissWithAnimation();
                        activity.finish();
                    }, false);

            return;
        }

        DialogUtil.progressDialog(activity, "Cancelling appointment...", activity.getColor(R.color.successColor), false);
        PatientAPI api = AppInstance.retrofit().create(PatientAPI.class);
        Call<ServerResponse<AppointmentModel>> call = api.deleteAppointment(PreferenceUtil.getInt("user_id", 0), appointmentId);
        call.enqueue(new Callback<ServerResponse<AppointmentModel>>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Response<ServerResponse<AppointmentModel>> response)
            {
                ServerResponse<AppointmentModel> server = response.body();
                DialogUtil.dismissDialog();

                if(server != null && !server.hasError)
                    Toasty.success(activity, "Appointment has been canceled successfully, refresh list now!", Toasty.LENGTH_LONG).show();
                else if(server != null)
                    DialogUtil.warningDialog(activity, "Process Unsuccessful", server.message, "Okay", false);
                else
                    DialogUtil.errorDialog(activity, "Process Unsuccessful", "Server failed to respond to your request", "Okay", false);

                call.cancel();
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Throwable t)
            {
                DialogUtil.errorDialog(activity, "Request Failed", t.getLocalizedMessage(), "Okay", false);
                call.cancel();
            }
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE})
    private void patientEditAppointment(final AppointmentModel appointment)
    {
        DialogUtil.progressDialog(activity, "Loading details...", activity.getColor(R.color.successColor), false);
        SpecialtyAPI api = AppInstance.retrofit().create(SpecialtyAPI.class);
        Call<ServerResponse<SpecialtyModel>> call = api.fetchMedicalFields();
        call.enqueue(new Callback<ServerResponse<SpecialtyModel>>() {
            @Override
            public void onResponse(Call<ServerResponse<SpecialtyModel>> call, Response<ServerResponse<SpecialtyModel>> response)
            {
                ServerResponse<SpecialtyModel> server = response.body();
                DialogUtil.dismissDialog();

                if(server != null && !server.hasError)
                    PatientEditAppointmentFragment.getInstance(appointment, server.data).show(activity.getSupportFragmentManager(), AppointmentListHolder.class.getSimpleName());
                else if(server != null && server.hasError)
                    Toasty.error(activity, server.message, Toasty.LENGTH_LONG).show();
                else
                    Toasty.warning(activity, "An unexpected event has occured in the server", Toasty.LENGTH_LONG).show();

                call.cancel();
            }

            @Override
            public void onFailure(Call<ServerResponse<SpecialtyModel>> call, Throwable t)
            {
                Toasty.warning(activity, "[Server]: " + t.getLocalizedMessage(), Toasty.LENGTH_LONG).show();
                DialogUtil.dismissDialog();
                call.cancel();
            }
        });
    }

    private String parseDate(String date)
    {
        String[] vals = date.split("-");
        String parsed;

        String year = vals[0];
        String month = "";
        int m = Integer.parseInt(vals[1]);
        String day = vals[2];

        switch(m)
        {
            case 1:
                month = "Jan";
                break;
            case 2:
                month = "Feb";
                break;
            case 3:
                month = "Mar";
                break;
            case 4:
                month = "Apr";
                break;
            case 5:
                month = "May";
                break;
            case 6:
                month = "Jun";
                break;
            case 7:
                month = "Jul";
                break;
            case 8:
                month = "Aug";
                break;
            case 9:
                month = "Sep";
                break;
            case 10:
                month = "Oct";
                break;
            case 11:
                month = "Nov";
                break;
            case 12:
                month = "Dec";
                break;
        }

        parsed = String.format("%s %s, %s", month, day, year);
        return parsed;
    }

    private String parseTime(String time)
    {
        String[] vals = time.split(":");
        String meridiem = "";
        String parsed;

        String hour = vals[0];
        String minute = vals[1];

        int hrs = Integer.parseInt(hour);
        int min = Integer.parseInt(minute);

        if(hrs >= 12 && hrs <= 23)
            meridiem = "PM";

        if(hrs >= 0 && hrs <= 11)
            meridiem = "AM";

        if(hrs > 12 && hrs <= 23)
            hrs = hrs - 12;
        else if(hrs == 0)
            hrs = 12;

        if(min < 10)
            minute = "0" + min;
        else
            minute = String.valueOf(min);

        if(hrs < 10)
            hour = "0" + hrs;
        else
            hour = String.valueOf(hrs);

        parsed = String.format("%s:%s %s", hour, minute, meridiem);
        return parsed;
    }
}
