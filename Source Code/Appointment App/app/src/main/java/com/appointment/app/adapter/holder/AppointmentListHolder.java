package com.appointment.app.adapter.holder;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.appointment.app.fragment.dialog.PatientEditAppointmentDialogFragment;
import com.appointment.app.model.AppointmentModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.model.SpecialtyModel;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.AlarmManagerUtil;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;
import com.robertlevonyan.views.chip.Chip;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import info.androidhive.fontawesome.FontTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings({"FieldMayBeFinal", "ALL"})
public class AppointmentListHolder extends BaseListHolder implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
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

    // Appointment variable
    private AppointmentModel appointment;

    // String variables for rescheduling an appointment
    private String selectedDate;
    private String selectedTime;

    public AppointmentListHolder(@NonNull View itemView, @NonNull List<AppointmentModel> items, @NonNull AppCompatActivity activity)
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
            appointment = items.get(position);
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
            if(appointment.status.toLowerCase().equals("rescheduled"))
                appointmentStatus.setChipBackgroundColor(activity.getColor(R.color.warningColor));

            patientIcon.setImageResource(appointment.gender.toLowerCase().equals("male") ? R.drawable.person_male : R.drawable.person_female);

            if(appointment.isDoctor && appointment.status.toUpperCase().equals(AppointmentModel.Status.APPROVED.name().toUpperCase()))
            {
                appointmentNegativeButton.setText(R.string.fa_window_close_solid);
                appointmentNeutralButton.setText(R.string.fa_calendar_week_solid);

                appointmentPositiveButton.setVisibility(View.GONE);
                appointmentNegativeButton.setVisibility(View.VISIBLE);
                appointmentNeutralButton.setVisibility(View.VISIBLE);
            }
            else if(appointment.isDoctor && appointment.status.toUpperCase().equals(AppointmentModel.Status.PENDING.name().toUpperCase()))
            {
                appointmentNeutralButton.setText(R.string.fa_calendar_week_solid);

                appointmentNegativeButton.setVisibility(View.VISIBLE);
                appointmentPositiveButton.setVisibility(View.VISIBLE);
                appointmentNeutralButton.setVisibility(View.VISIBLE);
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
            else if(!appointment.isDoctor && appointment.status.toUpperCase().equals(AppointmentModel.Status.RESCHEDULED.name().toUpperCase()))
            {
                appointmentNegativeButton.setText(R.string.fa_window_close_solid);
                appointmentPositiveButton.setText(R.string.fa_calendar_day_solid);

                appointmentNegativeButton.setVisibility(View.VISIBLE);
                appointmentPositiveButton.setVisibility(View.VISIBLE);
                appointmentNeutralButton.setVisibility(View.GONE);
            }

            appointmentParent.setOnClickListener(view -> showAppointmentDetails(appointment));

            appointmentPositiveButton.setOnClickListener(
                view -> {
                    if(!appointment.isDoctor && appointment.status.equals("rescheduled"))
                    {
                        acceptRescheduleAppointment(appointment.id);
                        return;
                    }

                    DialogUtil.warningDialog(activity, "Confirm Action", String.format("Are you sure you want to %s this appointment?", appointment.status.toLowerCase().equals("approved") ? "reschedule" : "approve"), "Yes", "No",
                            dlg -> {
                                if(appointment.isDoctor && appointment.status.toLowerCase().equals("approved"))
                                    rescheduleAppointment(appointment.id);
                                else if(appointment.isDoctor && appointment.status.toLowerCase().equals("pending"))
                                    approveAppointment(appointment.id);
                            },
                            SweetAlertDialog::cancel,
                            false);
                });

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

            appointmentNeutralButton.setOnClickListener(view -> {
                if(appointment.isDoctor)
                    DialogUtil.warningDialog(activity, "Confirm Action", "Are you sure you want to reschedule this appointment?", "Yes", "No",
                        dlg -> rescheduleAppointment(appointment.id),
                        SweetAlertDialog::cancel,
                         false);
                else
                    patientEditAppointment(appointment);
            });
        } catch(Exception e) {
            e.printStackTrace();

            DialogUtil.dismissDialog();
            DialogUtil.errorDialog(activity, "Application Error", e.getMessage(), "Okay", false);
        }
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, second);
        selectedTime = DateFormat.getTimeInstance().format(calendar.getTime());

        StringBuilder str = new StringBuilder();
        str.append("<strong>").append("Old Schedule: ").append("</strong>").append(parseDate(appointment.date)).append(" @ ").append(parseTime(appointment.time)).append("<br/>")
           .append("<strong>").append("New Schedule: ").append("</strong>").append(selectedDate).append(" @ ").append(selectedTime).append("<br/>").append("<br/>")
           .append("<center>").append("Confirm new schedule?").append("</center>");

        SweetAlertDialog sweet = new SweetAlertDialog(activity, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        sweet.setTitleText("Preview Schedule");
        sweet.setContentText("");
        sweet.setConfirmText("Okay");
        sweet.setCancelText("Cancel");
        sweet.setCancelable(false);
        sweet.setCanceledOnTouchOutside(false);
        sweet.setCustomImage(appointment.gender.toLowerCase().equals("male") ? R.drawable.person_male : R.drawable.person_female);

        sweet.setConfirmClickListener(sweetAlertDialog -> {
            appointment.date = selectedDate;
            appointment.time = selectedTime;
            appointment.ownerId = Integer.parseInt(appointment.owner);
            appointment.doctorId = Integer.parseInt(appointment.doctor);

            DialogUtil.progressDialog(activity, "Updating appointment...", R.color.warningColor, false);
            DoctorAPI api = AppInstance.retrofit().create(DoctorAPI.class);
            Call<ServerResponse<AppointmentModel>> call = api.rescheduleAppointment(Integer.valueOf(appointment.doctor), appointment.id, appointment);
            call.enqueue(new Callback<ServerResponse<AppointmentModel>>() {
                @Override
                public void onResponse(Call<ServerResponse<AppointmentModel>> call, Response<ServerResponse<AppointmentModel>> response)
                {
                    ServerResponse<AppointmentModel> server = response.body();
                    DialogUtil.dismissDialog();
                    call.cancel();

                    if(server != null && !server.hasError)
                        DialogUtil.successDialog(activity, "Success", "Appointment has been successfully rescheduled!\nRefresh list now!", "Okay", false);
                    else if(server != null)
                        DialogUtil.errorDialog(activity, "Failed", server.message, "Okay", false);
                    else
                        DialogUtil.errorDialog(activity, "Server Error", "The server returned an unexpected result!", "Okay", false);
                }

                @Override
                public void onFailure(Call<ServerResponse<AppointmentModel>> call, Throwable t)
                {
                    DialogUtil.dismissDialog();
                    DialogUtil.errorDialog(activity, "Server Error", t.getMessage(), "Okay", false);
                    call.cancel();
                }
            });

            sweetAlertDialog.dismissWithAnimation();
        });
        sweet.setCancelClickListener(SweetAlertDialog::cancel);
        sweet.show();

        TextView content = sweet.findViewById(R.id.content_text);
        content.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        content.setText(Html.fromHtml(str.toString()));
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        selectedDate = DateFormat.getDateInstance().format(calendar.getTime());
        showTimePicker();
    }

    private void showDatePicker()
    {
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
        calendar.setLenient(true);

        DatePickerDialog dpd = DatePickerDialog.newInstance(this::onDateSet, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.setTitle("Pick appointment date");
        dpd.showYearPickerFirst(false);

        Calendar minDate = Calendar.getInstance();
        dpd.setMinDate(minDate);

        Calendar maxDate = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        maxDate.add(Calendar.YEAR, year + 10);
        dpd.setMaxDate(maxDate);

        dpd.show(activity.getSupportFragmentManager(), AppointmentListHolder.class.getSimpleName().toUpperCase());
    }

    private void showTimePicker()
    {
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
        calendar.setLenient(true);

        TimePickerDialog tpd = TimePickerDialog.newInstance(this::onTimeSet, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.setTitle("Pick appointment time");
        tpd.enableSeconds(false);
        tpd.show(activity.getSupportFragmentManager(), AppointmentListHolder.class.getSimpleName().toUpperCase());
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
    private void acceptRescheduleAppointment(int appointmentId)
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

        DialogUtil.warningDialog(activity, "Confirm new schedule", String.format("Your appointment has been rescheduled by your doctor to %s @ %s.\nWould you like to accept this new schedule?", parseDate(appointment.date), parseTime(appointment.time)), "Accept", "Dismiss",
                dlg -> {
                    appointment.status = AppointmentModel.Status.APPROVED.name().toLowerCase();
                    appointment.ownerId = Integer.parseInt(appointment.owner);
                    appointment.doctorId = Integer.parseInt(appointment.doctor);

                    DialogUtil.dismissDialog();
                    DialogUtil.progressDialog(activity, "Confirming new schedule...", activity.getColor(R.color.warningColor), false);
                    PatientAPI api = AppInstance.retrofit().create(PatientAPI.class);
                    Call<ServerResponse<AppointmentModel>> call = api.updateAppointment(Integer.parseInt(appointment.owner), appointment.id, appointment);
                    call.enqueue(new Callback<ServerResponse<AppointmentModel>>()
                    {
                        @Override
                        public void onResponse(Call<ServerResponse<AppointmentModel>> call, Response<ServerResponse<AppointmentModel>> response)
                        {
                            ServerResponse<AppointmentModel> server = response.body();
                            DialogUtil.dismissDialog();
                            call.cancel();

                            if(server != null && !server.hasError)
                                Toasty.success(activity, "You have accepted the new schedule of your appointment!", Toasty.LENGTH_LONG).show();
                            else if(server != null)
                                DialogUtil.errorDialog(activity, "Server Error", server.message, "Okay", false);
                            else
                                DialogUtil.errorDialog(activity, "Server Error", "Server returned an unexpected response to your request, try agaain later", "Okay", false);
                        }

                        @Override
                        public void onFailure(Call<ServerResponse<AppointmentModel>> call, Throwable t)
                        {
                            DialogUtil.dismissDialog();
                            DialogUtil.errorDialog(activity, "Request Failed", t.getMessage(), "Okay", false);
                            call.cancel();
                        }
                    });
                },
                SweetAlertDialog::cancel,
                false);
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE})
    private void rescheduleAppointment(int appointmentId)
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

        DialogUtil.dismissDialog();
        showDatePicker();
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
                    .setConfirmButton("Submit", reason -> {
                        appointment.reason = reason;

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

        if(!appointment.isDoctor)
            str.append("<strong>").append("Doctor: ").append("</strong>").append(appointment.doctorName).append("<br/>");

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

        DialogUtil.progressDialog(activity, "Loading details...", activity.getColor(R.color.warningColor), false);
        SpecialtyAPI api = AppInstance.retrofit().create(SpecialtyAPI.class);
        Call<ServerResponse<SpecialtyModel>> call = api.fetchMedicalFields();
        call.enqueue(new Callback<ServerResponse<SpecialtyModel>>() {
            @Override
            public void onResponse(Call<ServerResponse<SpecialtyModel>> call, Response<ServerResponse<SpecialtyModel>> response)
            {
                ServerResponse<SpecialtyModel> server = response.body();
                DialogUtil.dismissDialog();

                if(server != null && !server.hasError)
                    PatientEditAppointmentDialogFragment.getInstance(appointment, server.data).show(activity.getSupportFragmentManager(), AppointmentListHolder.class.getSimpleName());
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
