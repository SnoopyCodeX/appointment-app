package com.appointment.app.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.api.SpecialtyAPI;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.model.SpecialtyModel;
import com.appointment.app.util.Constants;
import com.appointment.app.util.DialogUtil;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class PatientDialogFragment extends BottomSheetDialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{
    private LinearLayout parentMedicalField;
    private LinearLayout parentDoctor;
    private LinearLayout parentPatient;
    private LinearLayout parentName;
    private LinearLayout parentGender;
    private LinearLayout parentAge;
    private LinearLayout parentReason;
    private LinearLayout parentDate;
    private LinearLayout parentTime;
    private LinearLayout parentPreview;

    private AppCompatSpinner medicalFieldSelection;
    private AppCompatSpinner doctorSelection;
    private RadioGroup patientIdentitySelection;
    private TextInputLayout patientNameInput;
    private RadioGroup patientGenderSelection;
    private TextInputLayout patientAgeInput;
    private TextInputLayout patientReasonInput;
    private TextView patientDateSelection;
    private TextView patientTimeSelection;

    private TextView previewMedicalField;
    private TextView previewDoctor;
    private TextView previewIdentity;
    private TextView previewName;
    private TextView previewGender;
    private TextView previewAge;
    private TextView previewReason;
    private TextView previewDate;
    private TextView previewTime;

    private MaterialRippleLayout btnNextConfirm;

    // Bottom Sheet steps
    private List<LinearLayout> forms = new ArrayList<>();
    private List<View> fields = new ArrayList<>();
    private int currentStep = 0;
    private int lastStep = 0;

    // Selected values from dropdowns
    private String[] names;
    private String medicalName;
    private int doctorId;

    // Error Messages
    private List<String> errors = Arrays.asList(
            "Please select a medical field",
            "Please select a doctor",
            "Please specify the patient",
            "Please enter the patient's full name",
            "Please select the patient's gender",
            "Please enter a valid age",
            "Reason is too short, please elaborate as much as you can",
            "Please select the date of the appointment",
            "Please select the time of the appointment"
    );

    // Selector Adapters
    private ArrayAdapter<String> medicalFieldNamesAdapter;
    private ArrayAdapter<String> doctorNamesAdapter;

    private PatientDialogFragment()
    {}

    private PatientDialogFragment(String[] medicalNames)
    {
        this.names = medicalNames;
    }

    public static PatientDialogFragment getInstance()
    {
        return new PatientDialogFragment();
    }

    public static PatientDialogFragment getInstance(String[] medicalNames)
    {
        return new PatientDialogFragment(medicalNames);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.dialog_create_appointment, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> setupDialog(dialogInterface));
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View parent, @Nullable Bundle savedInstanceState)
    {
        initViews(parent);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        patientDateSelection.setText(DateFormat.getDateInstance().format(calendar.getTime()));
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, second);
        patientTimeSelection.setText(DateFormat.getTimeInstance().format(calendar.getTime()));
    }

    private void setupDialog(DialogInterface di)
    {
        BottomSheetDialog bsd = (BottomSheetDialog) di;
        View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        bottomSheet.setBackgroundColor(Color.TRANSPARENT);
    }

    private void setupDoctorNames(String medicalField)
    {
        DialogUtil.progressDialog(getContext(), "Fetching doctors...", getContext().getResources().getColor(R.color.successColor), false);
        SpecialtyAPI api = AppInstance.retrofit().create(SpecialtyAPI.class);
        Call<ServerResponse<DoctorModel>> call = api.fetchDoctorsInMedicalField(medicalField);
        call.enqueue(new Callback<ServerResponse<DoctorModel>>() {
            @Override
            public void onResponse(Call<ServerResponse<DoctorModel>> call, Response<ServerResponse<DoctorModel>> response)
            {
                ServerResponse<DoctorModel> server = response.body();
                DialogUtil.dismissDialog();

                if(server != null && !server.hasError)
                {
                    List<DoctorModel> data = server.data;
                    String[] names = new String[data.size()];

                    for(int i = 0; i < names.length; i++)
                        names[i] = data.get(i).fullname;

                    doctorNamesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
                    doctorSelection.setAdapter(doctorNamesAdapter);

                    doctorSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                        {
                            doctorId = data.get(position).id;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent)
                        {}
                    });
                }
                else if(server != null && server.hasError)
                {
                    PatientDialogFragment.this.dismiss();
                    Toasty.error(getContext(), server.message, Toasty.LENGTH_LONG).show();
                }
                else
                    Toasty.warning(getContext(), "An unexpected has occured in the server", Toasty.LENGTH_LONG).show();

                call.cancel();
            }

            @Override
            public void onFailure(Call<ServerResponse<DoctorModel>> call, Throwable t)
            {
                Toasty.warning(getContext(), "[Server]: " + t.getLocalizedMessage(), Toasty.LENGTH_LONG).show();
                DialogUtil.dismissDialog();
                call.cancel();
            }
        });
    }

    private void initViews(@NonNull View parent)
    {
        btnNextConfirm = parent.findViewById(R.id.btn_next_confirm);
        ((AppCompatButton) btnNextConfirm.getChildView()).setText(Objects.requireNonNull(getContext()).getString(R.string.btn_text_next_confirm, "Next"));
        btnNextConfirm.setOnClickListener(view -> {
            if(isCurrentStepCleared())
            {
                if(medicalName == null)
                {
                    Toasty.warning(getContext(), "Please select a medical field before proceeding", Toasty.LENGTH_LONG).show();
                    return;
                }

                if(currentStep >= forms.size()-1)
                    return;

                lastStep = currentStep++;
                forms.get(lastStep).startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left));
                forms.get(lastStep).getAnimation().setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation)
                    {}

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        forms.get(lastStep).setVisibility(View.GONE);
                        animation.cancel();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {}
                });

                forms.get(currentStep).startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));
                forms.get(currentStep).setVisibility(View.VISIBLE);

                if(currentStep >= forms.size()-1)
                {
                    ((AppCompatButton) btnNextConfirm.getChildView()).setText(Objects.requireNonNull(getContext()).getString(R.string.btn_text_next_confirm, "Preview"));
                    ((AppCompatButton) btnNextConfirm.getChildView()).setBackgroundResource(R.drawable.btn_warning_selector);
                }

                if(currentStep == 1)
                    setupDoctorNames(medicalName);
            }

            if(((AppCompatButton) view).getText().toString().toLowerCase().equals("preview"))
            {
                /**
                 * TODO: Show summary of his/her appointment
                 */
            }

            if(((AppCompatButton) view).getText().toString().toLowerCase().equals("submit"))
            {
                /**
                 * TODO: Submit appointment to the database
                 */
            }
        });

        parentMedicalField = parent.findViewById(R.id.parent_medical_field);
        parentDoctor = parent.findViewById(R.id.parent_doctor);
        parentPatient = parent.findViewById(R.id.parent_patient);
        parentName = parent.findViewById(R.id.parent_name);
        parentGender = parent.findViewById(R.id.parent_gender);
        parentAge = parent.findViewById(R.id.parent_age);
        parentReason = parent.findViewById(R.id.parent_reason);
        parentDate = parent.findViewById(R.id.parent_date);
        parentTime = parent.findViewById(R.id.parent_time);

        medicalFieldSelection = parent.findViewById(R.id.spinner_medical_field);
        doctorSelection = parent.findViewById(R.id.spinner_doctor);
        patientIdentitySelection = parent.findViewById(R.id.radio_patient);
        patientNameInput = parent.findViewById(R.id.input_patient_full_name);
        patientGenderSelection = parent.findViewById(R.id.radio_gender);
        patientAgeInput = parent.findViewById(R.id.input_patient_age);
        patientReasonInput = parent.findViewById(R.id.input_patient_reason);
        patientDateSelection = parent.findViewById(R.id.input_date);
        patientTimeSelection = parent.findViewById(R.id.input_time);

        forms = Arrays.asList(parentMedicalField, parentDoctor, parentPatient, parentName, parentGender, parentAge, parentReason, parentDate, parentTime);
        fields = Arrays.asList(medicalFieldSelection, doctorSelection, patientIdentitySelection, patientNameInput, patientGenderSelection, patientAgeInput, patientReasonInput, patientDateSelection, patientTimeSelection);

        patientReasonInput.getEditText().addTextChangedListener(new TextWatcher() {
            private String tempReason = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(patientReasonInput.getEditText().getText().toString().length() == Constants.MAX_REASON_LENGTH)
                    patientReasonInput.getEditText().setText(tempReason.substring(0, tempReason.length()-1));
                else
                    tempReason += charSequence.toString();

                patientReasonInput.setHelperText(String.format("Length: %d / %d", charSequence.length(), Constants.MAX_REASON_LENGTH));
                patientReasonInput.setHelperTextEnabled(true);

                if(charSequence.length() < (Constants.MAX_REASON_LENGTH / 3))
                    patientReasonInput.setHelperTextColor(ColorStateList.valueOf(R.color.successColor));

                if(charSequence.length() >= (Constants.MAX_REASON_LENGTH / 3) && charSequence.length() < (Constants.MAX_REASON_LENGTH / 2))
                    patientReasonInput.setHelperTextColor(ColorStateList.valueOf(R.color.warningColor));

                if(charSequence.length() >= (Constants.MAX_REASON_LENGTH / 2) && charSequence.length() <= (Constants.MAX_REASON_LENGTH))
                    patientReasonInput.setHelperTextColor(ColorStateList.valueOf(R.color.errorColor));
            }

            @Override
            public void afterTextChanged(Editable editable)
            {}
        });

        patientDateSelection.setOnClickListener(view -> {
            showDatePicker();
        });

        patientTimeSelection.setOnClickListener(view -> {
            showTimePicker();
        });

        medicalFieldNamesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
        medicalFieldSelection.setAdapter(medicalFieldNamesAdapter);
        medicalFieldSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                medicalName = names[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {}
        });
    }

    private boolean isCurrentStepCleared()
    {
        Object field = fields.get(currentStep);

        if(field instanceof AppCompatSpinner)
        {
            String value = (String) ((AppCompatSpinner) field).getSelectedItem();

            if(value!= null && !value.isEmpty())
                return true;

            Toast toast = Toasty.error(getContext(), errors.get(currentStep), Toasty.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 20, 20);
            toast.show();

            return false;
        }

        if(field instanceof TextInputLayout)
        {
            TextInputLayout inputLayout = (TextInputLayout) field;
            TextInputEditText inputEditText = (TextInputEditText) inputLayout.getEditText();
            String value = inputEditText.getText().toString();

            if(value != null && !value.isEmpty())
                return true;

            Toast toast = Toasty.error(getContext(), errors.get(currentStep), Toasty.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 20, 20);
            toast.show();

            return false;
        }

        if(field instanceof RadioGroup)
        {
            RadioGroup group = (RadioGroup) field;
            int id = group.getCheckedRadioButtonId();

            if(id == R.id.radio_choice_male || id == R.id.radio_choice_female || id == R.id.radio_choice_lgbtqp || id == R.id.radio_choice_myself || id == R.id.radio_choice_other)
                return true;

            Toast toast = Toasty.error(getContext(), errors.get(currentStep), Toasty.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 20, 20);
            toast.show();

            return false;
        }

        if(field instanceof TextView)
        {
            TextView textView = (TextView) field;
            String value = textView.getText().toString();

            if(value != null && !value.isEmpty())
                return true;

            Toast toast = Toasty.error(getContext(), errors.get(currentStep), Toasty.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 20, 20);
            toast.show();

            return false;
        }

        return false;
    }

    private void showDatePicker()
    {
        Calendar calendar = Calendar.getInstance();
        String selected = patientDateSelection.getText().toString();

        if(selected != null)
        {
            try {
                Date date = DateFormat.getDateInstance().parse(selected);
                calendar.setTime(date);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

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

        dpd.show(getFragmentManager(), PatientDialogFragment.class.getSimpleName().toUpperCase());
    }

    private void showTimePicker()
    {
        Calendar calendar = Calendar.getInstance();
        String selected = patientTimeSelection.getText().toString();

        if(selected != null)
        {
            try {
                Date date = DateFormat.getTimeInstance().parse(selected);
                calendar.setTime(date);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        calendar.setLenient(true);
        TimePickerDialog tpd = TimePickerDialog.newInstance(this::onTimeSet, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.setTitle("Pick appointment time");
        tpd.enableSeconds(false);
        tpd.show(getFragmentManager(), PatientDialogFragment.class.getSimpleName().toUpperCase());
    }
}