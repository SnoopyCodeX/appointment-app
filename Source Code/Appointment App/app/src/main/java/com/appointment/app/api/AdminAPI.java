package com.appointment.app.api;

import com.appointment.app.model.AdminModel;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AdminAPI
{
    @POST("admin/login")
    Call<ServerResponse<AdminModel>> login(@Body AdminModel adminModel);

    @POST("admin/{adminId}/doctor/new")
    Call<ServerResponse<DoctorModel>> newDoctor(@Path("adminId") int adminId, @Body DoctorModel doctorModel);

    @POST("admin/{adminId}/doctor/{doctorId}/delete")
    Call<ServerResponse<DoctorModel>> deleteDoctor(@Path("adminId") int adminId, @Path("doctorId") int doctorId);

    @POST("admin/{adminId}/doctor/{doctorId}/update")
    Call<ServerResponse<DoctorModel>> updateDoctor(@Path("adminId") int adminId, @Path("doctorId") int doctorId, @Body DoctorModel doctorModel);

    @GET("admin/{adminId}/doctor")
    Call<ServerResponse<DoctorModel>> getAllDoctors(@Path("adminId") int adminId);

    @POST("admin/{adminId}/changePassword")
    Call<ServerResponse<AdminModel>> changePassword(@Path("adminId") int adminId, @Body AdminModel adminModel);
}
