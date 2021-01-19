package com.appointment.app.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.api.AdminAPI;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.model.SpecialtyModel;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class DoctorEditDialogFragment extends BottomSheetDialogFragment
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
    private List<SpecialtyModel> specialtyModels;
    private DoctorModel doctorModel;

    private DoctorEditDialogFragment(DoctorModel doctorModel, List<SpecialtyModel> specialtyModels)
    {
        this.specialtyModels = specialtyModels;
        this.doctorModel = doctorModel;
    }

    public static DoctorEditDialogFragment getInstance(DoctorModel doctorModel, List<SpecialtyModel> specialtyModels)
    {
        return new DoctorEditDialogFragment(doctorModel, specialtyModels);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_doctor_edit_details, container, false);
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

        String[] names = new String[specialtyModels.size()];
        int tempIndex = 0;
        for(SpecialtyModel specialtyModel : specialtyModels)
            names[tempIndex++] = specialtyModel.name;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, names);
        specialty.setAdapter(adapter);
        specialty.setSelection(Arrays.asList(names).indexOf(doctorModel.specialty));

        int tempGenderIndex = -1;
        tempGenderIndex = doctorModel.gender.toLowerCase().equals("male") ? 0 : tempGenderIndex;
        tempGenderIndex = doctorModel.gender.toLowerCase().equals("female") ? 1 : tempGenderIndex;
        ((RadioButton) doctorGender.getChildAt(tempGenderIndex)).setChecked(true);

        doctorName.getEditText().setText(doctorModel.fullname);
        doctorNumber.getEditText().setText(doctorModel.contactNumber);
        doctorEmail.getEditText().setText(doctorModel.emailAddress);
        doctorRoom.getEditText().setText(doctorModel.roomNumber);
        doctorSchedule.getEditText().setText(doctorModel.schedule);
        doctorTime.getEditText().setText(doctorModel.time);

        initListener();
    }

    private void initListener()
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

            boolean hasChanges = !strSpecialty.equalsIgnoreCase(doctorModel.specialty) ||
                                 !strGender.equalsIgnoreCase(doctorModel.gender) ||
                                 !strName.equalsIgnoreCase(doctorModel.fullname) ||
                                 !strNumber.equalsIgnoreCase(doctorModel.contactNumber) ||
                                 !strEmail.equalsIgnoreCase(doctorModel.emailAddress) ||
                                 !strRoom.equalsIgnoreCase(doctorModel.roomNumber) ||
                                 !strSchedule.equalsIgnoreCase(doctorModel.schedule) ||
                                 !strTime.equalsIgnoreCase(doctorModel.time);

            if(hasChanges)
            {
                doctorModel.specialty = strSpecialty;
                doctorModel.gender = strGender;
                doctorModel.fullname = strName;
                doctorModel.contactNumber = strNumber;
                doctorModel.emailAddress = strEmail;
                doctorModel.roomNumber = strRoom;
                doctorModel.schedule = strSchedule;
                doctorModel.time = strTime;

                DialogUtil.progressDialog(getActivity(), "Saving changes...", getActivity().getColor(R.color.warningColor), false);
                AdminAPI api = AppInstance.retrofit().create(AdminAPI.class);
                Call<ServerResponse<DoctorModel>> call = api.updateDoctor(PreferenceUtil.getInt("user_id", 0), doctorModel.id, doctorModel);
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
            else
                dismiss();
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
