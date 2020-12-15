package com.appointment.app.api;

import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.model.SpecialtyModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SpecialtyAPI
{
    @GET("doctor/medical-fields")
    Call<ServerResponse<SpecialtyModel>> fetchMedicalFields();

    @POST("doctor/medical-fields")
    Call<ServerResponse<DoctorModel>> fetchDoctorsInMedicalField(@Body SpecialtyModel specialtyModel);
}
