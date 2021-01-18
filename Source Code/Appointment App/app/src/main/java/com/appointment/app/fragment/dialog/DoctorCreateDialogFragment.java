package com.appointment.app.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.api.AdminAPI;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class DoctorCreateDialogFragment extends BottomSheetDialogFragment
{
    private AppCompatSpinner specialty;
    private TextInputLayout doctorName;
    private TextInputLayout doctorNumber;
    private TextInputLayout doctorEmail;
    private TextInputLayout doctorRoom;
    private TextInputLayout doctorSchedule;
    private TextInputLayout doctorTime;
    private RadioGroup doctorGender;
    private MaterialRippleLayout btnCancel;
    private MaterialRippleLayout btnSave;
    private String[] specialtyNames;

    private DoctorCreateDialogFragment(String[] specialtyNames)
    {
        this.specialtyNames = specialtyNames;
    }

    public static DoctorCreateDialogFragment getInstance(String[] specialtyNames)
    {
        return new DoctorCreateDialogFragment(specialtyNames);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_doctor_create_details, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(this::setupDialog);
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void setupDialog(DialogInterface di)
    {
        BottomSheetDialog bsd = (BottomSheetDialog) di;
        View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        bottomSheet.setBackgroundColor(Color.TRANSPARENT);
    }

    private void initViews(@NonNull View parent)
    {
        specialty = parent.findViewById(R.id.spinner_specialty);
        doctorGender = parent.findViewById(R.id.radio_gender);
        doctorName = parent.findViewById(R.id.input_doctor_full_name);
        doctorNumber = parent.findViewById(R.id.input_doctor_number);
        doctorEmail = parent.findViewById(R.id.input_doctor_email_address);
        doctorRoom = parent.findViewById(R.id.input_doctor_room_number);
        doctorSchedule = parent.findViewById(R.id.input_doctor_schedule);
        doctorTime = parent.findViewById(R.id.input_doctor_time);

        btnCancel = parent.findViewById(R.id.btn_cancel);
        btnSave = parent.findViewById(R.id.btn_save);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, specialtyNames);
        specialty.setAdapter(adapter);
        specialty.setSelection(0);

        initListeners();
    }

    private void initListeners()
    {
        btnCancel.setOnClickListener(view -> dismiss());
        btnSave.setOnClickListener(view -> {
            String strSpecialty = (String) specialty.getSelectedItem();
            String strGender = getSelectedRadioItem(doctorGender.getCheckedRadioButtonId());
            String strName = doctorName.getEditText().getText().toString();
            String strNumber = doctorNumber.getEditText().getText().toString();
            String strEmail = doctorEmail.getEditText().getText().toString();
            String strRoom = doctorRoom.getEditText().getText().toString();
            String strSchedule = doctorSchedule.getEditText().getText().toString();
            String strTime = doctorTime.getEditText().getText().toString();

            boolean hasError = false;

            if(strName.isEmpty() || strName.length() <= 4)
            {
                doctorName.setError("Name must be longer than 4 characters!");
                hasError = true;
            }

            if(strNumber.isEmpty() || strNumber.matches("[0-9]{12}"))
            {
                doctorNumber.setError("Contact number must be 11 or 12 characters long!");
                hasError = true;
            }

            if(strEmail.isEmpty() || !strEmail.matches(Patterns.EMAIL_ADDRESS.pattern()))
            {
                doctorEmail.setError("Please enter a valid email address!");
                hasError = true;
            }

            if(strRoom.isEmpty())
            {
                doctorRoom.setError("Please enter a valid room number!");
                hasError = true;
            }

            if(strSchedule.isEmpty())
            {
                doctorSchedule.setError("Please enter a valid schedule ranging from Monday - Friday!");
                hasError = true;
            }

            if(strTime.isEmpty() || !strTime.matches("([0-9]{2}\\:[0-9]{2})[\\s\\n\\-a-zA-Z]+"))
            {
                doctorTime.setError("Please enter a valid time slot!");
                hasError = true;
            }

            if(!hasError)
            {
                DoctorModel doctorModel = DoctorModel.newDoctor(strName, "", strGender, strEmail, strNumber, strRoom, strSpecialty, strSchedule, strTime);
                DialogUtil.progressDialog(getActivity(), "Creating account...", getActivity().getColor(R.color.warningColor), false);
                AdminAPI api = AppInstance.retrofit().create(AdminAPI.class);
                Call<ServerResponse<DoctorModel>> call = api.newDoctor(PreferenceUtil.getInt("user_id", 0), doctorModel);
                call.enqueue(new Callback<ServerResponse<DoctorModel>>()
                {
                    @Override
                    public void onResponse(Call<ServerResponse<DoctorModel>> call, Response<ServerResponse<DoctorModel>> response)
                    {
                        ServerResponse<DoctorModel> server = response.body();
                        DialogUtil.dismissDialog();
                        call.cancel();

                        if(server != null && !server.hasError)
                            Toasty.success(getContext(), server.message, Toasty.LENGTH_LONG).show();
                        else if(server != null)
                            DialogUtil.errorDialog(getContext(), "Request Failed", server.message, "Okay", false);
                        else
                            DialogUtil.errorDialog(getContext(), "Request Failed", "Server returned an unexpected response, please try again.", "Okay", false);
                    }

                    @Override
                    public void onFailure(Call<ServerResponse<DoctorModel>> call, Throwable t)
                    {
                        DialogUtil.dismissDialog();
                        DialogUtil.errorDialog(getContext(), "Server Error", t.getLocalizedMessage(), "Okay", false);
                        call.cancel();
                    }
                });
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private String getSelectedRadioItem(int radioButtonId)
    {
        String selection = "";

        switch(radioButtonId)
        {
            case R.id.radio_choice_male:
                selection = "Male";
                break;

            case R.id.radio_choice_female:
                selection = "Female";
                break;
        }

        return selection;
    }
}
