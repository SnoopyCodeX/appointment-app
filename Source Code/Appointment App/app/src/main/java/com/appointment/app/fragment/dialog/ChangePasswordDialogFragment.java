package com.appointment.app.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.api.AdminAPI;
import com.appointment.app.api.DoctorAPI;
import com.appointment.app.api.PatientAPI;
import com.appointment.app.model.AdminModel;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.PatientModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.DialogUtil;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class ChangePasswordDialogFragment extends BottomSheetDialogFragment
{
    public static enum Role
    {
        ADMIN(0),
        DOCTOR(1),
        PATIENT(2);

        int value = 0;
        Role(int value)
        {
            this.value = value;
        }
    }

    private TextInputLayout oldPassword;
    private TextInputLayout newPassword;
    private MaterialRippleLayout btnSave;
    private MaterialRippleLayout btnCancel;
    private Role role;
    private int ownerId;

    private ChangePasswordDialogFragment()
    {}

    public static ChangePasswordDialogFragment getInstance()
    {
        return new ChangePasswordDialogFragment();
    }

    public ChangePasswordDialogFragment setRole(Role role)
    {
        this.role = role;
        return this;
    }

    public ChangePasswordDialogFragment setOwnerId(int ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(this::setupDialog);
        return dialog;
    }

    private void setupDialog(DialogInterface di)
    {
        BottomSheetDialog bsd = (BottomSheetDialog) di;
        View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        bottomSheet.setBackgroundColor(Color.TRANSPARENT);
    }

    private void initViews(@NonNull View parent)
    {
        oldPassword = parent.findViewById(R.id.input_old_password);
        newPassword = parent.findViewById(R.id.input_new_password);
        btnCancel = parent.findViewById(R.id.btn_cancel);
        btnSave = parent.findViewById(R.id.btn_save);
        initListeners();
    }

    private void initListeners()
    {
        btnCancel.setOnClickListener(view -> dismiss());
        btnSave.setOnClickListener(view -> {
            String strOldPassword = oldPassword.getEditText().getText().toString();
            String strNewPassword = newPassword.getEditText().getText().toString();
            boolean hasError = false;

            if(strOldPassword.isEmpty() || strOldPassword.length() < 8)
            {
                oldPassword.setError("Password must be atleast 8 characters long!");
                hasError = true;
            }
            else if(!strOldPassword.isEmpty() && !passwordHasAlphaNumericsAndSpecialChars(strOldPassword))
            {
                oldPassword.setError("Password must be a combination of special and alphanumeric characters!");
                hasError = true;
            }

            if(strNewPassword.isEmpty() || strNewPassword.length() < 8)
            {
                newPassword.setError("Password must be atleast 8 characters long!");
                hasError = true;
            }
            else if(!strNewPassword.isEmpty() && !passwordHasAlphaNumericsAndSpecialChars(strNewPassword))
            {
                newPassword.setError("Password must be a combination of special and alphanumeric characters!");
                hasError = true;
            }
            else if(!strNewPassword.isEmpty() && strNewPassword.equals(strOldPassword))
            {
                newPassword.setError("New password must not be the same as the old password!");
                hasError = true;
            }

            if(!hasError)
                switch(role)
                {
                    case PATIENT:
                        patient_changePassword(strOldPassword, strNewPassword);
                        break;

                    case DOCTOR:
                        doctor_changePassword(strOldPassword, strNewPassword);
                        break;

                    case ADMIN:
                        admin_changePassword(strOldPassword, strNewPassword);
                        break;
                }
        });
    }

    private boolean passwordHasAlphaNumericsAndSpecialChars(String password)
    {
        return (password.matches("[0-9]+") && password.matches("[a-z]+") && password.matches("[A-Z]+") && password.matches("[\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\-\\_\\+\\=]+"));
    }

    private void admin_changePassword(String oldPassword, String newPassword)
    {
        if(!InternetReceiver.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Disconnected", "You are not connected to an active network", "Wifi Settings",
                    dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)), false);
            return;
        }

        DialogUtil.progressDialog(getContext(), "Updating password...", getContext().getColor(R.color.warningColor), false);
        AdminAPI api = AppInstance.retrofit().create(AdminAPI.class);
        Call<ServerResponse<AdminModel>> call = api.changePassword(ownerId, AdminModel.changePassword(oldPassword, newPassword));
        call.enqueue(new Callback<ServerResponse<AdminModel>>()
        {
            @Override
            public void onResponse(Call<ServerResponse<AdminModel>> call, Response<ServerResponse<AdminModel>> response)
            {
                ServerResponse<AdminModel> server = response.body();
                DialogUtil.dismissDialog();
                call.cancel();

                if(server != null && !server.hasError)
                {
                    Toasty.success(getContext(), server.message, Toasty.LENGTH_LONG).show();
                    dismiss();
                }
                else if(server != null)
                    DialogUtil.errorDialog(getContext(), "Request Failed", server.message, "Okay", false);
                else
                    DialogUtil.errorDialog(getContext(), "Request Failed", "Server returned an unexpected response, try again.", "Okay", false);
            }

            @Override
            public void onFailure(Call<ServerResponse<AdminModel>> call, Throwable t)
            {
                DialogUtil.dismissDialog();
                DialogUtil.errorDialog(getContext(), "Server Error", t.getLocalizedMessage(), "Okay", false);
                call.cancel();
                dismiss();
            }
        });
    }

    private void doctor_changePassword(String oldPassword, String newPassword)
    {
        if(!InternetReceiver.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Disconnected", "You are not connected to an active network", "Wifi Settings",
                    dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)), false);
            return;
        }

        DialogUtil.progressDialog(getContext(), "Updating password...", getContext().getColor(R.color.warningColor), false);
        DoctorAPI api = AppInstance.retrofit().create(DoctorAPI.class);
        Call<ServerResponse<DoctorModel>> call = api.changePassword(ownerId, DoctorModel.changePassword(-1, oldPassword, newPassword));
        call.enqueue(new Callback<ServerResponse<DoctorModel>>()
        {
            @Override
            public void onResponse(Call<ServerResponse<DoctorModel>> call, Response<ServerResponse<DoctorModel>> response)
            {
                ServerResponse<DoctorModel> server = response.body();
                DialogUtil.dismissDialog();
                call.cancel();

                if(server != null && !server.hasError)
                {
                    Toasty.success(getContext(), server.message, Toasty.LENGTH_LONG).show();
                    dismiss();
                }
                else if(server != null)
                    DialogUtil.errorDialog(getContext(), "Request Failed", server.message, "Okay", false);
                else
                    DialogUtil.errorDialog(getContext(), "Request Failed", "Server returned an unexpected response, try again.", "Okay", false);
            }

            @Override
            public void onFailure(Call<ServerResponse<DoctorModel>> call, Throwable t)
            {
                DialogUtil.dismissDialog();
                DialogUtil.errorDialog(getContext(), "Server Error", t.getLocalizedMessage(), "Okay", false);
                call.cancel();
                dismiss();
            }
        });
    }

    private void patient_changePassword(String oldPassword, String newPassword)
    {
        if(!InternetReceiver.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Disconnected", "You are not connected to an active network", "Wifi Settings",
                    dlg -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)), false);
            return;
        }

        DialogUtil.progressDialog(getContext(), "Updating password...", getContext().getColor(R.color.warningColor), false);
        PatientAPI api = AppInstance.retrofit().create(PatientAPI.class);
        Call<ServerResponse<PatientModel>> call = api.changePassword(ownerId, PatientModel.changePassword(-1, oldPassword, newPassword));
        call.enqueue(new Callback<ServerResponse<PatientModel>>()
        {
            @Override
            public void onResponse(Call<ServerResponse<PatientModel>> call, Response<ServerResponse<PatientModel>> response)
            {
                ServerResponse<PatientModel> server = response.body();
                DialogUtil.dismissDialog();
                call.cancel();

                if(server != null && !server.hasError)
                {
                    Toasty.success(getContext(), server.message, Toasty.LENGTH_LONG).show();
                    dismiss();
                }
                else if(server != null)
                    DialogUtil.errorDialog(getContext(), "Request Failed", server.message, "Okay", false);
                else
                    DialogUtil.errorDialog(getContext(), "Request Failed", "Server returned an unexpected response, try again.", "Okay", false);
            }

            @Override
            public void onFailure(Call<ServerResponse<PatientModel>> call, Throwable t)
            {
                DialogUtil.dismissDialog();
                DialogUtil.errorDialog(getContext(), "Server Error", t.getLocalizedMessage(), "Okay", false);
                call.cancel();
                dismiss();
            }
        });
    }
}
