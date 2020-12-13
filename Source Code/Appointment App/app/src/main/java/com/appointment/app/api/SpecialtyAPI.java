package com.appointment.app.api;

import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.model.SpecialtyModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SpecialtyAPI
{
    @GET("doctor/medical-fields")
    Call<ServerResponse<SpecialtyModel>> fetchMedicalFields();

    @GET("doctor/medical-fields/{medical_field}")
    Call<ServerResponse<DoctorModel>> fetchDoctorsInMedicalField(@Path("medical_field") String medicalField);
}
