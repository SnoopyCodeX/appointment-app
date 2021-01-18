package com.appointment.app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appointment.app.AppInstance;
import com.appointment.app.R;
import com.appointment.app.api.AdminAPI;
import com.appointment.app.api.DoctorAPI;
import com.appointment.app.api.PatientAPI;
import com.appointment.app.model.AdminModel;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.PatientModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.util.Constants;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.net.InternetReceiver;
import com.appointment.app.util.PreferenceUtil;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class LoginActivity extends AppCompatActivity
{
    private static enum PanelType
    {
        TYPE_PATIENT_SIGNIN(0),
        TYPE_PATIENT_SIGNUP(1),
        TYPE_DOCTOR_SIGNIN(2),
        TYPE_ADMIN_SIGNIN(3);

        private int value;
        PanelType(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }
    }

    // Panel Buttons
    private MaterialRippleLayout btn_patient_login;
    private MaterialRippleLayout btn_patient_register;
    private MaterialRippleLayout btn_doctor_login;
    private MaterialRippleLayout btn_admin_login;

    // Bottom text prompts
    private TextView textSignInAsPatientPrompt; // Already have an account? Login here  -> From: Patient Register Panel
    private TextView textSignUpAsPatientPrompt; // Not registered yet? Register here  -> From: Patient Login Panel
    private TextView textSignInAsDoctorPrompt; // Are you a doctor? Login here  -> From: Patient Login &/Or Register Panel
    private TextView textSignInAsAdminPrompt; // Are you an admin? Login here -> From: Any login panel
    private TextView textSignInPrompt; // Not a doctor? Login here  -> From: Doctor Login Panel

    // Bottom text prompt parent layouts
    private LinearLayout parentSignInAsPatientPrompt; // Already have an account? Login here  -> From: Patient Register Panel
    private LinearLayout parentSignUpAsPatientPrompt; // Not registered yet? Register here  -> From: Patient Login Panel
    private LinearLayout parentSignInAsDoctorPrompt; // Are you a doctor? Login here  -> From: Patient Login &/Or Register Panel
    private LinearLayout parentSignInAsAdminPrompt; // Are you an admin? Login here  -> From: Any login panel
    private LinearLayout parentSignInPrompt; // Not a doctor? Login here  -> From: Doctor Login Panel

    // Separator
    private LinearLayout parentSeparator;

    // Patient & Doctor Panels
    private MaterialCardView patientLoginPanel;
    private MaterialCardView patientRegisterPanel;
    private MaterialCardView doctorLoginPanel;
    private MaterialCardView adminLoginPanel;

    // Patient Login Inputs
    private TextInputLayout patientLoginEmail;
    private TextInputLayout patientLoginPassword;

    // Doctor Login Inputs
    private TextInputLayout doctorLoginEmail;
    private TextInputLayout doctorLoginPassword;

    // Admin Login Inputs
    private TextInputLayout adminLoginEmail;
    private TextInputLayout adminLoginPassword;

    // Patient Register Inputs
    private TextInputLayout patientRegisterFullname;
    private TextInputLayout patientRegisterEmail;
    private TextInputLayout patientRegisterGender;
    private TextInputLayout patientRegisterContactNumber;
    private TextInputLayout patientRegisterAge;
    private TextInputLayout patientRegisterAddress;
    private TextInputLayout patientRegisterPassword;

    private PanelType panelType = PanelType.TYPE_PATIENT_SIGNIN;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        textSignInAsPatientPrompt = findViewById(R.id.text_patient_login_prompt);
        textSignUpAsPatientPrompt = findViewById(R.id.text_patient_register_prompt);
        textSignInAsDoctorPrompt = findViewById(R.id.text_login_as_doctor);
        textSignInAsAdminPrompt = findViewById(R.id.text_login_as_admin);
        textSignInPrompt = findViewById(R.id.text_login_as_patient);

        parentSignInAsPatientPrompt = findViewById(R.id.patient_sign_up_text);
        parentSignUpAsPatientPrompt = findViewById(R.id.patient_sign_in_text);
        parentSignInAsDoctorPrompt = findViewById(R.id.doctor_prompt_text);
        parentSignInAsAdminPrompt = findViewById(R.id.admin_prompt_text);
        parentSignInPrompt = findViewById(R.id.doctor_sign_in_text);

        parentSeparator = findViewById(R.id.separator);

        patientLoginPanel = findViewById(R.id.patient_login_ui);
        patientRegisterPanel = findViewById(R.id.patient_register_ui);
        doctorLoginPanel = findViewById(R.id.doctor_login_ui);
        adminLoginPanel = findViewById(R.id.admin_login_ui);

        btn_admin_login = findViewById(R.id.btn_login_admin);
        btn_doctor_login = findViewById(R.id.btn_login_doctor);
        btn_patient_login = findViewById(R.id.btn_login_patient);
        btn_patient_register = findViewById(R.id.btn_register_patient);

        patientLoginEmail = findViewById(R.id.input_login_patient_email);
        patientLoginPassword = findViewById(R.id.input_login_patient_password);

        doctorLoginEmail = findViewById(R.id.input_login_doctor_email);
        doctorLoginPassword = findViewById(R.id.input_login_doctor_password);

        adminLoginEmail = findViewById(R.id.input_login_admin_email);
        adminLoginPassword = findViewById(R.id.input_login_admin_password);

        patientRegisterFullname = findViewById(R.id.input_register_patient_fullname);
        patientRegisterEmail = findViewById(R.id.input_register_patient_email);
        patientRegisterAddress = findViewById(R.id.input_register_patient_address);
        patientRegisterContactNumber = findViewById(R.id.input_register_patient_number);
        patientRegisterAge = findViewById(R.id.input_register_patient_age);
        patientRegisterGender = findViewById(R.id.input_register_patient_gender);
        patientRegisterPassword = findViewById(R.id.input_register_patient_password);

        btn_doctor_login.setOnClickListener(v -> {
            switch(panelType)
            {
                case TYPE_PATIENT_SIGNIN:
                    signInAsPatient();
                    break;

                case TYPE_PATIENT_SIGNUP:
                    signUpAsPatient();
                    break;

                case TYPE_DOCTOR_SIGNIN:
                    signInAsDoctor();
                    break;

                case TYPE_ADMIN_SIGNIN:
                    signInAsAdmin();
                    break;
            }
        });

        btn_patient_login.setOnClickListener(v -> {
            switch(panelType)
            {
                case TYPE_PATIENT_SIGNIN:
                    signInAsPatient();
                    break;

                case TYPE_PATIENT_SIGNUP:
                    signUpAsPatient();
                    break;

                case TYPE_DOCTOR_SIGNIN:
                    signInAsDoctor();
                    break;

                case TYPE_ADMIN_SIGNIN:
                    signInAsAdmin();
                    break;
            }
        });

        btn_patient_register.setOnClickListener(v -> {
            switch(panelType)
            {
                case TYPE_PATIENT_SIGNIN:
                    signInAsPatient();
                    break;

                case TYPE_PATIENT_SIGNUP:
                    signUpAsPatient();
                    break;

                case TYPE_DOCTOR_SIGNIN:
                    signInAsDoctor();
                    break;

                case TYPE_ADMIN_SIGNIN:
                    signInAsAdmin();
                    break;
            }
        });

        btn_admin_login.setOnClickListener(v -> {
            switch(panelType)
            {
                case TYPE_PATIENT_SIGNIN:
                    signInAsPatient();
                    break;

                case TYPE_PATIENT_SIGNUP:
                    signUpAsPatient();
                    break;

                case TYPE_DOCTOR_SIGNIN:
                    signInAsDoctor();
                    break;

                case TYPE_ADMIN_SIGNIN:
                    signInAsAdmin();
                    break;
            }
        });

        textSignInPrompt.setOnClickListener(v -> {
            showPatientSignIn();
        });

        textSignInAsDoctorPrompt.setOnClickListener(v -> {
            showDoctorSignIn();
        });

        textSignUpAsPatientPrompt.setOnClickListener(v -> {
            showPatientSignUp();
        });

        textSignInAsPatientPrompt.setOnClickListener(v -> {
            showPatientSignIn();
        });

        textSignInAsAdminPrompt.setOnClickListener(v -> {
            showAdminSignIn();
        });

        hookEditorAction(patientRegisterPanel.getChildAt(0));
        hookEditorAction(patientLoginPanel.getChildAt(0));
        hookEditorAction(doctorLoginPanel.getChildAt(0));
        hookEditorAction(adminLoginPanel.getChildAt(0));
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        PreferenceUtil.bindWith(this);
        InternetReceiver.initiateSelf(this)
                .setOnConnectivityChangedListener(isConnected -> {
                    if(!isConnected)
                        DialogUtil.warningDialog(this, "Network Unavailable", "You are not connected to an active network", "Wifi Settings", "Exit",
                                dlg -> {
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                },
                                dlg -> {
                                    dlg.dismissWithAnimation();
                                    LoginActivity.this.finish();
                                }, false);
                    else
                        DialogUtil.dismissDialog();
                });

        Class activityClass = null;
        boolean isLoggedIn = PreferenceUtil.getBoolean(Constants.PREF_KEY_LOGGED_IN, false);
        int panelType = PreferenceUtil.getInt(Constants.PREF_KEY_PANEL_TYPE, PanelType.TYPE_PATIENT_SIGNIN.value);

        if(isLoggedIn && panelType == PanelType.TYPE_PATIENT_SIGNIN.value)
            activityClass = PatientPanelActivity.class;
        else if(isLoggedIn && panelType == PanelType.TYPE_DOCTOR_SIGNIN.value)
            activityClass = DoctorPanelActivity.class;
        else if(isLoggedIn && panelType == PanelType.TYPE_ADMIN_SIGNIN.value)
            activityClass = AdminPanelActivity.class;

        if(activityClass != null)
        {
            startActivity(new Intent(this, activityClass));
            InternetReceiver.unBindWith(this);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        InternetReceiver.unBindWith(this);
    }

    private void signInAsPatient()
    {
        String email = patientLoginEmail.getEditText().getText().toString();
        String password = patientLoginPassword.getEditText().getText().toString();

        if(!InternetReceiver.isConnected(this))
        {
            DialogUtil.warningDialog(this, "Disconnected", "You are not connected to an active network", "Wifi Settings", dlg -> {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }, false);

            return;
        }

        if(email.isEmpty() || !email.matches(Patterns.EMAIL_ADDRESS.pattern()))
            patientLoginEmail.setError("Please enter a valid email address");
        else if(password.isEmpty() || password.length() < 8)
            patientLoginPassword.setError("Please enter a valid password");
        else
        {
            DialogUtil.progressDialog(this, "Logging in...", Color.parseColor("#11A31F"), false);
            PatientAPI api = AppInstance.retrofit().create(PatientAPI.class);
            Call<ServerResponse<PatientModel>> call = api.login(PatientModel.loginModel(email, password));
            call.enqueue(new Callback<ServerResponse<PatientModel>>() {
                @Override
                public void onResponse(Call<ServerResponse<PatientModel>> call, Response<ServerResponse<PatientModel>> response)
                {
                    DialogUtil.dismissDialog();
                    ServerResponse<PatientModel> server = response.body();
                    call.cancel();

                    if(!call.isCanceled())
                        call.cancel();

                    if(server != null && server.hasError)
                        DialogUtil.errorDialog(LoginActivity.this, "Login Failed", server.message,"Okay",  false);
                    else if(server == null)
                        DialogUtil.errorDialog(LoginActivity.this, "Login Failed", "The server did not responded to your login request","Okay",  false);
                    else if(server != null && !server.hasError)
                    {
                        startActivity(new Intent(LoginActivity.this, PatientPanelActivity.class));
                        PreferenceUtil.putBoolean(Constants.PREF_KEY_LOGGED_IN, true);
                        PreferenceUtil.putInt(Constants.PREF_KEY_PANEL_TYPE, panelType.value);
                        PreferenceUtil.putString("user_name", server.data.get(0).fullname);
                        PreferenceUtil.putInt("user_id", server.data.get(0).id);
                        LoginActivity.this.finish();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<PatientModel>> call, Throwable t)
                {
                    DialogUtil.dismissDialog();
                    DialogUtil.errorDialog(LoginActivity.this, "Server Error", t.getMessage(), "Okay", false);

                    call.cancel();
                }
            });
        }
    }

    private void signInAsDoctor()
    {
        String email = doctorLoginEmail.getEditText().getText().toString();
        String password = doctorLoginPassword.getEditText().getText().toString();

        if(!InternetReceiver.isConnected(this))
        {
            DialogUtil.warningDialog(this, "Disconnected", "You are not connected to an active network", "Wifi Settings", dlg -> {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }, false);

            return;
        }

        if(email.isEmpty() || !email.matches(Patterns.EMAIL_ADDRESS.pattern()))
            doctorLoginEmail.setError("Please enter a valid email address");
        else if(password.isEmpty() || password.length() < 8)
            doctorLoginPassword.setError("Password must be atleast 8 characters long!");
        else
        {
            DialogUtil.progressDialog(this, "Logging in...", Color.parseColor("#11A31F"), false);
            DoctorAPI api = AppInstance.retrofit().create(DoctorAPI.class);
            Call<ServerResponse<DoctorModel>> call = api.login(DoctorModel.loginModel(email, password));
            call.enqueue(new Callback<ServerResponse<DoctorModel>>() {
                @Override
                public void onResponse(Call<ServerResponse<DoctorModel>> call, Response<ServerResponse<DoctorModel>> response)
                {
                    DialogUtil.dismissDialog();
                    ServerResponse<DoctorModel> server = response.body();
                    call.cancel();

                    if(server != null && server.hasError)
                        DialogUtil.errorDialog(LoginActivity.this, "Login Failed", server.message,"Okay",  false);
                    else if(server == null)
                        DialogUtil.errorDialog(LoginActivity.this, "Login Failed", "The server did not responded to your login request","Okay",  false);
                    else if(server != null && !server.hasError)
                    {
                        startActivity(new Intent(LoginActivity.this, DoctorPanelActivity.class));
                        PreferenceUtil.putBoolean(Constants.PREF_KEY_LOGGED_IN, true);
                        PreferenceUtil.putInt(Constants.PREF_KEY_PANEL_TYPE, panelType.value);
                        PreferenceUtil.putString("user_name", server.data.get(0).fullname);
                        PreferenceUtil.putInt("user_id", server.data.get(0).id);
                        LoginActivity.this.finish();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<DoctorModel>> call, Throwable t)
                {
                    DialogUtil.dismissDialog();
                    DialogUtil.errorDialog(LoginActivity.this, "Server Error", t.getMessage(), "Okay", false);
                    call.cancel();
                }
            });
        }
    }

    private void signInAsAdmin()
    {
        String email = adminLoginEmail.getEditText().getText().toString();
        String password = adminLoginPassword.getEditText().getText().toString();

        if(!InternetReceiver.isConnected(this))
        {
            DialogUtil.warningDialog(this, "Disconnected", "You are not connected to an active network", "Wifi Settings", dlg -> {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }, false);

            return;
        }

        if(email.isEmpty() || !email.matches(Patterns.EMAIL_ADDRESS.pattern()))
            adminLoginEmail.setError("Please enter a valid email address");
        else if(password.isEmpty() || password.length() < 8)
            adminLoginPassword.setError("Password must be atleast 8 characters long!");
        else
        {
            DialogUtil.progressDialog(this, "Logging in...", Color.parseColor("#11A31F"), false);
            AdminAPI api = AppInstance.retrofit().create(AdminAPI.class);
            Call<ServerResponse<AdminModel>> call = api.login(AdminModel.authenticate(email, password));
            call.enqueue(new Callback<ServerResponse<AdminModel>>() {
                @Override
                public void onResponse(Call<ServerResponse<AdminModel>> call, Response<ServerResponse<AdminModel>> response)
                {
                    DialogUtil.dismissDialog();
                    ServerResponse<AdminModel> server = response.body();
                    call.cancel();

                    if(server != null && server.hasError)
                        DialogUtil.errorDialog(LoginActivity.this, "Login Failed", server.message,"Okay",  false);
                    else if(server == null)
                        DialogUtil.errorDialog(LoginActivity.this, "Login Failed", "The server did not responded to your login request","Okay",  false);
                    else if(server != null && !server.hasError)
                    {
                        startActivity(new Intent(LoginActivity.this, AdminPanelActivity.class));
                        PreferenceUtil.putBoolean(Constants.PREF_KEY_LOGGED_IN, true);
                        PreferenceUtil.putInt(Constants.PREF_KEY_PANEL_TYPE, panelType.value);
                        PreferenceUtil.putString("user_name", server.data.get(0).emailAddress);
                        PreferenceUtil.putInt("user_id", server.data.get(0).id);
                        LoginActivity.this.finish();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<AdminModel>> call, Throwable t)
                {
                    DialogUtil.dismissDialog();
                    DialogUtil.errorDialog(LoginActivity.this, "Server Error", t.getMessage(), "Okay", false);
                    call.cancel();
                }
            });
        }
    }

    private void signUpAsPatient()
    {
        String fullname = patientRegisterFullname.getEditText().getText().toString();
        String email = patientRegisterEmail.getEditText().getText().toString();
        String address = patientRegisterAddress.getEditText().getText().toString();
        String number = patientRegisterContactNumber.getEditText().getText().toString();
        String gender = patientRegisterGender.getEditText().getText().toString();
        String age = patientRegisterAge.getEditText().getText().toString();
        String password = patientRegisterPassword.getEditText().getText().toString();

        if(!InternetReceiver.isConnected(this))
        {
            DialogUtil.warningDialog(this, "Disconnected", "You are not connected to an active network", "Wifi Settings", dlg -> {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }, false);

            return;
        }
        
        if(fullname.isEmpty() || fullname.length() < 4)
            patientRegisterFullname.setError("Please enter a valid full name");
        else if(email.isEmpty() || !email.matches("([a-zA-Z0-9\\-\\_\\.\\@]+)$"))
            patientRegisterEmail.setError("Please enter a valid email address");
        else if(address.isEmpty() || address.length() < 4)
            patientRegisterAddress.setError("Please enter a valid home address");
        else if(number.isEmpty() || number.length() < 3)
            patientRegisterContactNumber.setError("Please enter a valid mobile number");
        else if(gender.isEmpty() || gender.length() < 3)
            patientRegisterGender.setError("Please enter a valid gender");
        else if(age.isEmpty())
            patientRegisterAge.setError("Please enter a valid village");
        else if(password.isEmpty() || password.length() < 8)
            patientRegisterPassword.setError("Please enter valid password");
        else
        {
            DialogUtil.progressDialog(this, "Registering your account...", Color.parseColor("#11A31F"), false);
            PatientAPI api = AppInstance.retrofit().create(PatientAPI.class);
            Call<ServerResponse<PatientModel>> call = api.create(PatientModel.newPatient(fullname, gender, age, address, number, email, password));
            call.enqueue(new Callback<ServerResponse<PatientModel>>() {
                @Override
                public void onResponse(Call<ServerResponse<PatientModel>> call, Response<ServerResponse<PatientModel>> response)
                {
                    DialogUtil.dismissDialog();
                    ServerResponse<PatientModel> server = response.body();
                    Log.d(PatientModel.class.getSimpleName(), response.raw().toString());

                    if(server != null && server.hasError)
                        DialogUtil.errorDialog(LoginActivity.this, "Register Failed", server.message,"Okay",  false);
                    else if(server == null)
                        DialogUtil.errorDialog(LoginActivity.this, "Register Failed", "The server did not responded to your register request","Okay",  false);
                    else if(server != null && !server.hasError)
                    {
                        Toasty.success(LoginActivity.this, "You are now successfully registered", Toasty.LENGTH_LONG).show();
                        clearEditTexts(patientRegisterPanel.getChildAt(0));
                    }

                    call.cancel();
                }

                @Override
                public void onFailure(Call<ServerResponse<PatientModel>> call, Throwable t)
                {
                    DialogUtil.dismissDialog();
                    DialogUtil.errorDialog(LoginActivity.this, "Server Error", t.getMessage(), "Okay", false);

                    call.cancel();
                }
            });
        }
    }

    private void showPatientSignIn()
    {
        patientLoginPanel.setVisibility(View.VISIBLE);
        patientRegisterPanel.setVisibility(View.GONE);
        doctorLoginPanel.setVisibility(View.GONE);
        adminLoginPanel.setVisibility(View.GONE);

        parentSeparator.setVisibility(View.VISIBLE);
        parentSignInAsAdminPrompt.setVisibility(View.VISIBLE);
        parentSignUpAsPatientPrompt.setVisibility(View.VISIBLE);
        parentSignInAsDoctorPrompt.setVisibility(View.VISIBLE);
        parentSignInAsPatientPrompt.setVisibility(View.GONE);
        parentSignInPrompt.setVisibility(View.GONE);

        panelType = PanelType.TYPE_PATIENT_SIGNIN;

        clearEditTexts(patientRegisterPanel.getChildAt(0));
        clearEditTexts(doctorLoginPanel.getChildAt(0));
        clearEditTexts(adminLoginPanel.getChildAt(0));
    }

    private void showPatientSignUp()
    {
        patientRegisterPanel.setVisibility(View.VISIBLE);
        patientLoginPanel.setVisibility(View.GONE);
        doctorLoginPanel.setVisibility(View.GONE);
        adminLoginPanel.setVisibility(View.GONE);

        parentSeparator.setVisibility(View.VISIBLE);
        parentSignInAsAdminPrompt.setVisibility(View.VISIBLE);
        parentSignInAsPatientPrompt.setVisibility(View.VISIBLE);
        parentSignInAsDoctorPrompt.setVisibility(View.VISIBLE);
        parentSignUpAsPatientPrompt.setVisibility(View.GONE);
        parentSignInPrompt.setVisibility(View.GONE);

        panelType = PanelType.TYPE_PATIENT_SIGNUP;

        clearEditTexts(patientLoginPanel.getChildAt(0));
        clearEditTexts(doctorLoginPanel.getChildAt(0));
        clearEditTexts(adminLoginPanel.getChildAt(0));
    }

    private void showDoctorSignIn()
    {
        doctorLoginPanel.setVisibility(View.VISIBLE);
        patientRegisterPanel.setVisibility(View.GONE);
        patientLoginPanel.setVisibility(View.GONE);
        adminLoginPanel.setVisibility(View.GONE);

        parentSeparator.setVisibility(View.VISIBLE);
        parentSignInAsAdminPrompt.setVisibility(View.VISIBLE);
        parentSignInPrompt.setVisibility(View.VISIBLE);
        parentSignInAsDoctorPrompt.setVisibility(View.GONE);
        parentSignUpAsPatientPrompt.setVisibility(View.GONE);
        parentSignInAsPatientPrompt.setVisibility(View.GONE);

        panelType = PanelType.TYPE_DOCTOR_SIGNIN;

        clearEditTexts(patientRegisterPanel.getChildAt(0));
        clearEditTexts(patientLoginPanel.getChildAt(0));
        clearEditTexts(adminLoginPanel.getChildAt(0));
    }

    private void showAdminSignIn()
    {
        doctorLoginPanel.setVisibility(View.GONE);
        patientRegisterPanel.setVisibility(View.GONE);
        patientLoginPanel.setVisibility(View.GONE);
        adminLoginPanel.setVisibility(View.VISIBLE);

        parentSeparator.setVisibility(View.GONE);
        parentSignInAsAdminPrompt.setVisibility(View.GONE);
        parentSignInPrompt.setVisibility(View.GONE);
        parentSignInAsDoctorPrompt.setVisibility(View.VISIBLE);
        parentSignUpAsPatientPrompt.setVisibility(View.GONE);
        parentSignInAsPatientPrompt.setVisibility(View.GONE);

        panelType = PanelType.TYPE_ADMIN_SIGNIN;

        clearEditTexts(patientRegisterPanel.getChildAt(0));
        clearEditTexts(patientLoginPanel.getChildAt(0));
        clearEditTexts(doctorLoginPanel.getChildAt(0));
    }

    private void clearEditTexts(View parent)
    {
        for(int i = 0; i < ((ViewGroup) parent).getChildCount(); i++)
        {
            View child = ((ViewGroup) parent).getChildAt(i);

            if(child instanceof TextInputLayout)
            {
                TextInputLayout input = (TextInputLayout) child;
                input.getEditText().clearFocus();
                input.getEditText().setText("");
                input.setError("");
            }
        }
    }

    private void hookEditorAction(View parent)
    {
        for(int i = 0; i < ((ViewGroup) parent).getChildCount(); i++)
        {
            final View child = ((ViewGroup) parent).getChildAt(i);

            if(child instanceof TextInputLayout)
            {
                if(((TextInputLayout) child).getId() == R.id.input_login_patient_password ||
                        ((TextInputLayout) child).getId() == R.id.input_register_patient_password ||
                        ((TextInputLayout) child).getId() == R.id.input_login_doctor_password ||
                        ((TextInputLayout) child).getId() == R.id.input_login_admin_password)
                    ((TextInputLayout) child).getEditText().setOnEditorActionListener((textView, id, keyEvent) -> {
                        switch (panelType)
                        {
                            case TYPE_PATIENT_SIGNIN:
                                signInAsPatient();
                                return true;

                            case TYPE_PATIENT_SIGNUP:
                                signUpAsPatient();
                                return true;

                            case TYPE_DOCTOR_SIGNIN:
                                signInAsDoctor();
                                return true;

                            case TYPE_ADMIN_SIGNIN:
                                signInAsAdmin();
                                return true;
                        }
                        return false;
                    });

                ((TextInputLayout) child).getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
                    {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                    {
                        ((TextInputLayout) child).setError("");
                    }

                    @Override
                    public void afterTextChanged(Editable editable)
                    {}
                });
            }
        }
    }
}