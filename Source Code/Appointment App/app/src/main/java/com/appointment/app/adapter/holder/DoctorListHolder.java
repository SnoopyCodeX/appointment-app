package com.appointment.app.adapter.holder;

import android.Manifest;
import android.content.Intent;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.api.AdminAPI;
import com.appointment.app.api.SpecialtyAPI;
import com.appointment.app.fragment.dialog.DoctorEditDialogFragment;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.model.SpecialtyModel;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import info.androidhive.fontawesome.FontTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings({"FieldMayBeFinal", "ALL"})
public class DoctorListHolder extends BaseListHolder
{
    private AppCompatActivity activity;
    private List<DoctorModel> items;
    private DoctorModel doctor;
    private View itemView;

    private LinearLayout parentLayout;
    private ImageView doctorProfile;
    private TextView doctorName;
    private TextView doctorGender;
    private TextView doctorSchedule;
    private TextView doctorSpecialty;

    private FontTextView neutralButton;
    private FontTextView negativeButton;

    public DoctorListHolder(@NonNull View itemView, @NonNull List<DoctorModel> items, @NonNull AppCompatActivity activity)
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

    @Override
    public void onBind(int position)
    {
        super.onBind(position);

        try {
            PreferenceUtil.bindWith(activity);
            doctor = items.get(position);

            doctorProfile.setImageResource((doctor.gender.toLowerCase().equals("male") ? R.drawable.doctor_male : R.drawable.doctor_female));
            doctorName.setText("Name: " + doctor.fullname);
            doctorGender.setText("Gender: " + doctor.gender);
            doctorSchedule.setText("Schedule: " + doctor.schedule);
            doctorSpecialty.setText("Specialty: " + doctor.specialty);

            parentLayout.setOnClickListener(view -> showDoctorDetails(doctor));
            neutralButton.setOnClickListener(view -> editAppointment(doctor));
            negativeButton.setOnClickListener(view -> deleteAppointment(doctor.id));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews()
    {
        parentLayout = itemView.findViewById(R.id.doctor_parent);
        doctorProfile = itemView.findViewById(R.id.doctor_profile);
        doctorName = itemView.findViewById(R.id.doctor_name);
        doctorGender = itemView.findViewById(R.id.doctor_gender);
        doctorSchedule = itemView.findViewById(R.id.doctor_schedule);
        doctorSpecialty = itemView.findViewById(R.id.doctor_specialty);

        neutralButton = itemView.findViewById(R.id.doctor_btn_neutral);
        negativeButton = itemView.findViewById(R.id.doctor_btn_negative);
    }

    private void showDoctorDetails(DoctorModel doctor)
    {
        SweetAlertDialog sweet = new SweetAlertDialog(activity, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        sweet.setTitleText(doctor.fullname);
        sweet.setContentText("");
        sweet.setConfirmText("Okay");
        sweet.setCancelable(false);
        sweet.setCanceledOnTouchOutside(false);

        StringBuilder str = new StringBuilder();
        str.append("<strong>").append("Gender: ").append("</strong>").append(doctor.gender).append("<br/>")
           .append("<strong>").append("Contact No.: ").append("</strong>").append(doctor.contactNumber.isEmpty() ? "N/A" : doctor.contactNumber).append("<br/>")
           .append("<strong>").append("Email Address: ").append("</strong>").append(doctor.emailAddress.isEmpty() ? "N/A" : doctor.emailAddress).append("<br/>")
           .append("<strong>").append("Room No.: ").append("</strong>").append(doctor.roomNumber).append("<br/>")
           .append("<strong>").append("Specialty: ").append("</strong>").append(doctor.specialty).append("<br/>")
           .append("<strong>").append("Schedule: ").append("</strong>").append(doctor.schedule).append("<br/>")
           .append("<strong>").append("Time: ").append("</strong>").append(doctor.time).append("<br/>");

        sweet.setCustomImage(doctor.gender.toLowerCase().equals("male") ? R.drawable.doctor_male : R.drawable.doctor_female);
        sweet.show();

        TextView content = sweet.findViewById(R.id.content_text);
        content.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        content.setText(Html.fromHtml(str.toString()));
    }

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE})
    private void editAppointment(final DoctorModel doctor)
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
        call.enqueue(new Callback<ServerResponse<SpecialtyModel>>()
        {
            @Override
            public void onResponse(Call<ServerResponse<SpecialtyModel>> call, Response<ServerResponse<SpecialtyModel>> response)
            {
                ServerResponse<SpecialtyModel> server = response.body();
                DialogUtil.dismissDialog();
                call.cancel();

                if(server != null && !server.hasError)
                    DoctorEditDialogFragment.getInstance(doctor, server.data).show(activity.getSupportFragmentManager(), DoctorListHolder.class.getName());
                else if(server != null)
                    DialogUtil.warningDialog(activity, "Request Failed", server.message, "Okay", false);
                else
                    DialogUtil.warningDialog(activity, "Request Failed", "Server returned an unexpected response, try again.", "Okay", false);
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

    @RequiresPermission(allOf = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE})
    private void deleteAppointment(int id)
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

        DialogUtil.warningDialog(activity, "Confirm Action", "Do you really want to delete this account?", "Yes", "No",
                dlg -> {
                    DialogUtil.progressDialog(activity, "Deleting account...", activity.getColor(R.color.warningColor), false);
                    AdminAPI api = AppInstance.retrofit().create(AdminAPI.class);
                    Call<ServerResponse<DoctorModel>> call = api.deleteDoctor(PreferenceUtil.getInt("user_id", 0), id);
                    call.enqueue(new Callback<ServerResponse<DoctorModel>>()
                    {
                        @Override
                        public void onResponse(Call<ServerResponse<DoctorModel>> call, Response<ServerResponse<DoctorModel>> response)
                        {
                            ServerResponse<DoctorModel> server = response.body();
                            DialogUtil.dismissDialog();
                            call.cancel();

                            if(server != null && !server.hasError)
                                Toasty.success(activity, "Successfully deleted the account!", Toasty.LENGTH_LONG).show();
                            else if(server != null)
                                DialogUtil.errorDialog(activity, "Request Failed", server.message, "Okay", false);
                            else
                                DialogUtil.errorDialog(activity, "Server Error", "Server returned an unexpected result, please try again.", "Okay", false);
                        }

                        @Override
                        public void onFailure(Call<ServerResponse<DoctorModel>> call, Throwable t)
                        {
                            DialogUtil.dismissDialog();
                            DialogUtil.errorDialog(activity, "Request Failed", t.getLocalizedMessage(), "Okay", false);
                            call.cancel();
                        }
                    });
                },
                SweetAlertDialog::cancel,
                false);
    }
}
