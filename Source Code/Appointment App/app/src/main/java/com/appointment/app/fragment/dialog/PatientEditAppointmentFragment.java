package com.appointment.app.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.api.PatientAPI;
import com.appointment.app.api.SpecialtyAPI;
import com.appointment.app.model.AppointmentModel;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.model.SpecialtyModel;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientEditAppointmentFragment extends BottomSheetDialogFragment
{
    private AppCompatSpinner medicalField;
    private AppCompatSpinner doctorName;
    private TextInputLayout patientName;
    private TextInputLayout patientAge;
    private TextInputLayout patientAddress;
    private TextInputLayout patientReason;
    private TextView patientDate;
    private TextView patientTime;
    private RadioGroup patientGender;
    private RadioGroup patientIdentity;
    private MaterialRippleLayout btnCancel;
    private MaterialRippleLayout btnSave;
    private AppointmentModel appointment;
    private List<SpecialtyModel> medicalFields;
    private ArrayAdapter<String> doctorNamesAdapter;

    private PatientEditAppointmentFragment(AppointmentModel appointment, List<SpecialtyModel> medicalFields)
    {
        this.appointment = appointment;
        this.medicalFields = medicalFields;
    }

    public static PatientEditAppointmentFragment getInstance(AppointmentModel appointment, List<SpecialtyModel> medicalFields)
    {
        return new PatientEditAppointmentFragment(appointment, medicalFields);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_patient_edit_appointment, container, false);
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
        initViews(view);
    }

    private void setupDialog(DialogInterface di)
    {
        BottomSheetDialog bsd = (BottomSheetDialog) di;
        View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        bottomSheet.setBackgroundColor(Color.TRANSPARENT);
    }

    private void initViews(View parent)
    {
        medicalField = parent.findViewById(R.id.spinner_medical_field);
        doctorName = parent.findViewById(R.id.spinner_doctor);
        patientName = parent.findViewById(R.id.input_patient_full_name);
        patientAge = parent.findViewById(R.id.input_patient_age);
        patientReason = parent.findViewById(R.id.input_patient_reason);
        patientAddress = parent.findViewById(R.id.input_patient_address);
        patientDate = parent.findViewById(R.id.input_date);
        patientTime = parent.findViewById(R.id.input_time);
        patientGender = parent.findViewById(R.id.radio_gender);
        patientIdentity = parent.findViewById(R.id.radio_patient);
        btnCancel = parent.findViewById(R.id.btn_cancel);
        btnSave = parent.findViewById(R.id.btn_save);

        patientName.getEditText().setText(appointment.name);
        patientAge.getEditText().setText(appointment.age);
        patientReason.getEditText().setText(appointment.reason);
        patientAddress.getEditText().setText(appointment.address);

        patientDate.setText(appointment.date);
        patientTime.setText(appointment.time);

        int defaultGenderIndex = -1;
        defaultGenderIndex = appointment.gender.toLowerCase().equals("male") ? 0 : defaultGenderIndex;
        defaultGenderIndex = appointment.gender.toLowerCase().equals("female") ? 1 : defaultGenderIndex;
        defaultGenderIndex = appointment.gender.toLowerCase().equals("lgbtq+") ? 2 : defaultGenderIndex;
        ((RadioButton) patientGender.getChildAt(defaultGenderIndex)).setChecked(true);

        int defaultIdentityIndex = -1;
        defaultIdentityIndex = appointment.identity.toLowerCase().equals("myself") ? 0 : defaultIdentityIndex;
        defaultIdentityIndex = appointment.identity.toLowerCase().equals("other") ? 1 : defaultIdentityIndex;
        ((RadioButton) patientIdentity.getChildAt(defaultIdentityIndex)).setChecked(true);

        String[] medicalNames = new String[medicalFields.size()];
        int tempIndex = 0;
        for(SpecialtyModel specialty : medicalFields)
            medicalNames[tempIndex++] = specialty.name;
        ArrayAdapter<String> medicalFieldsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, medicalNames);
        medicalField.setAdapter(medicalFieldsAdapter);

        initListeners();
        medicalField.setSelection(Arrays.asList(medicalNames).indexOf(appointment.medicalField));
    }

    private void initListeners()
    {
        List<TextInputLayout> fields = Arrays.asList(patientName, patientAge, patientAddress, patientReason);
        btnCancel.setOnClickListener(view -> dismiss());
        btnSave.setOnClickListener(view -> {
            if(!InternetReceiver.isConnected(getContext()))
            {
                DialogUtil.warningDialog(getContext(), "Disconnected", "You are not connected to an active network", "Wifi Settings",
                        dlg -> {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }, false);

                return;
            }

            String selectedMedicalField = (String) medicalField.getSelectedItem();
            String selectedDoctorName = (String) doctorName.getSelectedItem();
            String selectedGender = getSelectedRadioItem(patientGender.getCheckedRadioButtonId());
            String selectedIdentity = getSelectedRadioItem(patientIdentity.getCheckedRadioButtonId());
            String editedName = patientName.getEditText().getText().toString();
            int editedAge = Integer.parseInt(patientAge.getEditText().getText().toString());
            String editedAddress = patientAddress.getEditText().getText().toString();
            String editedReason = patientReason.getEditText().getText().toString();
            String selectedDate = patientDate.getText().toString();
            String selectedTime = patientTime.getText().toString();

            boolean hasChangesMade = !selectedMedicalField.equals(appointment.medicalField) ||
                    !selectedDoctorName.equals(appointment.doctor) ||
                    !selectedGender.equals(appointment.gender) ||
                    !selectedIdentity.equals(appointment.identity) ||
                    !editedName.equals(appointment.owner) ||
                    editedAge != appointment.age ||
                    !editedAddress.equals(appointment.address) ||
                    !editedReason.equals(appointment.reason) ||
                    !selectedDate.equals(appointment.date) ||
                    !selectedTime.equals(appointment.time);

            if(!hasChangesMade)
            {
                dismiss();
                return;
            }

            DialogUtil.progressDialog(getContext(), "Saving changes...", getContext().getColor(R.color.warningColor), false);
            PatientAPI api = AppInstance.retrofit().create(PatientAPI.class);
            Call<ServerResponse<AppointmentModel>> call = api.updateAppointment(PreferenceUtil.getInt("user_id", 0), appointment.id,
                    AppointmentModel.newAppointment(
                            PreferenceUtil.getInt("user_id", 0),
                            0,
                            selectedIdentity,
                            selectedMedicalField,
                            editedName,
                            selectedGender,
                            editedAddress,
                            editedAge,
                            editedReason,
                            selectedDate,
                            selectedTime,
                            AppointmentModel.Status.PENDING.name()
                    ));
            call.enqueue(new Callback<ServerResponse<AppointmentModel>>() {
                @Override
                public void onResponse(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Response<ServerResponse<AppointmentModel>> response)
                {
                    ServerResponse<AppointmentModel> server = response.body();
                    DialogUtil.dismissDialog();
                    call.cancel();

                    if(doctorNamesAdapter != null)
                        doctorNamesAdapter.clear();

                    if(server != null && !server.hasError)
                    {
                        Toasty.success(getContext(), server.message, Toasty.LENGTH_LONG).show();
                        dismiss();
                    }
                    else if(server != null)
                        Toasty.error(getContext(), server.message, Toasty.LENGTH_LONG).show();
                    else
                        DialogUtil.errorDialog(getContext(), "Server Error", "Server did not responded to your request", "Okay", false);
                }

                @Override
                public void onFailure(@NonNull Call<ServerResponse<AppointmentModel>> call, @NonNull Throwable t)
                {
                    DialogUtil.dismissDialog();
                    DialogUtil.errorDialog(getContext(), "Server Error", t.getLocalizedMessage(), "Okay", false);
                    call.cancel();
                }
            });
        });

        for(TextInputLayout field : fields)
            field.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    if(s.length() == 0)
                        field.setError("This field is required");
                    else if(s.length() > 0 && s.length() <= 4)
                        field.setError("Field's value is too short");
                    else
                        field.setError("");
                }

                @Override
                public void afterTextChanged(Editable s)
                {}
            });

        medicalField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                SpecialtyModel specialty = medicalFields.get(position);
                getDoctorsByMedicalField(specialty.name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {}
        });

        patientDate.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            String selected = ((TextView) view).getText().toString();

            if(selected != null)
                try {
                    Date date = DateFormat.getDateInstance().parse(selected);
                    calendar.setTime(date);
                } catch(Exception e) {
                    e.printStackTrace();
                }

            calendar.setLenient(true);
            DatePickerDialog dpd = DatePickerDialog.newInstance((dialogView, year, month, day) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day);
                patientDate.setText(DateFormat.getDateInstance().format(cal.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dpd.setVersion(DatePickerDialog.Version.VERSION_2);
            dpd.setTitle("Pick date of appointment");
            dpd.showYearPickerFirst(false);
            dpd.setMinDate(calendar);

            Calendar clone = (Calendar) calendar.clone();
            clone.add(Calendar.YEAR, clone.get(Calendar.YEAR) + 10);
            dpd.setMaxDate(clone);

            dpd.show(getFragmentManager(), PatientEditAppointmentFragment.class.getSimpleName().toUpperCase());
        });

        patientTime.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            String selected = ((TextView) view).getText().toString();

            if(selected != null)
                try {
                    Date date = DateFormat.getTimeInstance().parse(selected);
                    calendar.setTime(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            calendar.setLenient(true);
            TimePickerDialog tpd = TimePickerDialog.newInstance((dialogView, hour, minute, seconds) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hour, minute);
                patientTime.setText(DateFormat.getTimeInstance().format(cal.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
            tpd.setVersion(TimePickerDialog.Version.VERSION_2);
            tpd.setTitle("Pick time of appointment");
            tpd.enableSeconds(false);
            tpd.show(getFragmentManager(), PatientEditAppointmentFragment.class.getSimpleName().toUpperCase());
        });

        patientIdentity.setOnCheckedChangeListener((group, checkedId) -> {});
        patientGender.setOnCheckedChangeListener((group, checkedId) -> {});
    }

    private void getDoctorsByMedicalField(String medicalField)
    {
        if(!InternetReceiver.isConnected(getContext()))
        {
            DialogUtil.warningDialog(getContext(), "Disconnected", "You are not connected to an active network", "Wifi Settings",
                    dlg -> {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }, false);

            return;
        }

        DialogUtil.progressDialog(getContext(), "Fetching doctors...", getContext().getColor(R.color.warningColor), false);
        SpecialtyAPI api = AppInstance.retrofit().create(SpecialtyAPI.class);
        Call<ServerResponse<DoctorModel>> call = api.fetchDoctorsInMedicalField(SpecialtyModel.newSpecialtyModel(medicalField));
        call.enqueue(new Callback<ServerResponse<DoctorModel>>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse<DoctorModel>> call, @NonNull Response<ServerResponse<DoctorModel>> response)
            {
                ServerResponse<DoctorModel> server = response.body();
                DialogUtil.dismissDialog();

                if(doctorNamesAdapter != null)
                    doctorNamesAdapter.clear();

                if(server != null && !server.hasError)
                    populateDoctorSpinner(server.data);
                else if(server != null)
                    Toasty.error(getContext(), "No doctor is associated with that medical field", Toasty.LENGTH_LONG).show();
                else
                    DialogUtil.errorDialog(getContext(), "Server Error", "Server did not responded to your request", "Okay", false);

                call.cancel();
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse<DoctorModel>> call, @NonNull Throwable t)
            {
                DialogUtil.dismissDialog();
                DialogUtil.errorDialog(getContext(), "Server Error", t.getLocalizedMessage(), "Okay", false);
                call.cancel();
            }
        });
    }

    private void populateDoctorSpinner(List<DoctorModel> doctors)
    {
        String[] names = new String[doctors.size()];
        int tempIndex = 0;
        for(DoctorModel doctor : doctors)
            names[tempIndex++] = doctor.fullname;

        doctorNamesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
        doctorName.setAdapter(doctorNamesAdapter);
        doctorName.setSelection(Arrays.asList(names).indexOf(appointment.doctor));
    }

    @SuppressLint("NonConstantResourceId")
    private String getSelectedRadioItem(int radioButtonId)
    {
        String selection = "";

        switch(radioButtonId)
        {
            case R.id.radio_choice_myself:
                selection = "Myself";
                break;

            case R.id.radio_choice_other:
                selection = "Other";
                break;

            case R.id.radio_choice_male:
                selection = "Male";
                break;

            case R.id.radio_choice_female:
                selection = "Female";
                break;

            case R.id.radio_choice_lgbtqp:
                selection = "LGBTQ+";
                break;
        }

        return selection;
    }
}