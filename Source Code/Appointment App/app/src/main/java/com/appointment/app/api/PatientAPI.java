package com.appointment.app.api;

import com.appointment.app.model.AppointmentModel;
import com.appointment.app.model.PatientModel;
import com.appointment.app.model.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * This class is an interface that contains
 * all of the CRUD operations for Patient Accounts
 *
 * @copyright 2020
 * @author John Roy L. Calimlim
 * @since 1.0
 * @version 1.0
 */
public interface PatientAPI
{
    @POST("patient/login")
    Call<ServerResponse<PatientModel>> login(@Body PatientModel patient);

    @POST("patient/new")
    Call<ServerResponse<PatientModel>> create(@Body PatientModel patient);

    @POST("patient/{patientId}/appointment/new")
    Call<ServerResponse<AppointmentModel>> createAppointment(@Path("patientId") int patientId, @Body AppointmentModel appointment);

    @POST("patient/{patientId}/appointment/{appointmentId}/delete")
    Call<ServerResponse<AppointmentModel>> deleteAppointment(@Path("patientId") int patientId, @Path("appointmentId") int appointmentId);

    @POST("patient/{patientId}/appointment/{appointmentId}/update")
    Call<ServerResponse<AppointmentModel>> updateAppointment(@Path("patientId") int patientId, @Path("appointmentId") int appointmentId, @Body AppointmentModel appointment);

    @GET("patient/{patientId}/appointment/{appointmentId}")
    Call<ServerResponse<AppointmentModel>> fetchAppointment(@Path("patientId") int patientId, @Path("appointmentId") int appointmentId);

    @GET("patient/{patientId}/appointment/approved")
    Call<ServerResponse<AppointmentModel>> fetchApprovedAppointments(@Path("patientId") int patientId);

    @GET("patient/{patientId}/appointment/pending")
    Call<ServerResponse<AppointmentModel>> fetchPendingAppointments(@Path("patientId") int patientId);

    @GET("patient/{patientId}/appointment")
    Call<ServerResponse<AppointmentModel>> fetchAllAppointments(@Path("patientId") int patientId);

    @POST("patient/{patientId}/changePassword")
    Call<ServerResponse<PatientModel>> changePassword(@Path("patientId") int patientId, @Body PatientModel patientModel);
}
