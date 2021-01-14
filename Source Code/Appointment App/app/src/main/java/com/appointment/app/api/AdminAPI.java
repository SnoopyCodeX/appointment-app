package com.appointment.app.api;

import com.appointment.app.model.AdminModel;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AdminAPI
{
    @POST("admin/login")
    Call<ServerResponse<AdminModel>> login(@Body AdminModel adminModel);

    @POST("admin/doctor/new")
    Call<ServerResponse<DoctorAPI>> newDoctor(@Body DoctorModel doctorModel);

    @POST("admin/doctor/delete")
    Call<ServerResponse<DoctorAPI>> deleteDoctor(@Body DoctorModel doctorModel);

    @POST("admin/doctor/update")
    Call<ServerResponse<DoctorAPI>> updateDoctor(@Body DoctorModel doctorModel);
}
