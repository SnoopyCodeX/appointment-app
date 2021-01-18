package com.appointment.app.api;

import com.appointment.app.model.AppointmentModel;
import com.appointment.app.model.DoctorModel;
import com.appointment.app.model.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * This class is an interface that contains
 * all of the CRUD operations for Doctor Accounts
 *
 * @copyright 2020
 * @author John Roy L. Calimlim
 * @since 1.0
 * @version 1.0
 */
public interface DoctorAPI
{
    @POST("doctor/login")
    Call<ServerResponse<DoctorModel>> login(@Body DoctorModel doctor);

    @POST("doctor/{doctorId}/appointment/{appointmentId}/approve")
    Call<ServerResponse<AppointmentModel>> approveAppointment(@Path("doctorId") int doctorId, @Path("appointmentId") int appointmentId);

    @POST("doctor/{doctorId}/appointment/{appointmentId}/reschedule")
    Call<ServerResponse<AppointmentModel>> rescheduleAppointment(@Path("doctorId") int doctorId, @Path("appointmentId") int appointmentId, @Body AppointmentModel appointment);

    @POST("doctor/{doctorId}/appointment/{appointmentId}/decline")
    Call<ServerResponse<AppointmentModel>> declineAppointment(@Path("doctorId") int doctorId, @Path("appointmentId") int appointmentId);

    @POST("doctor/{doctorId}/appointment/{appointmentId}/cancel")
    Call<ServerResponse<AppointmentModel>> cancelAppointment(@Path("doctorId") int doctorId, @Path("appointmentId") int appointmentId);

    @GET("doctor/{doctorId}/appointment/approved")
    Call<ServerResponse<AppointmentModel>> fetchApprovedAppointments(@Path("doctorId") int doctorId);

    @GET("doctor/{doctorId}/appointment/pending")
    Call<ServerResponse<AppointmentModel>> fetchPendingAppointments(@Path("doctorId") int doctorId);

    @POST("doctor/{doctorId}/changePassword")
    Call<ServerResponse<DoctorModel>> changePassword(@Path("doctorId") int doctorId, @Body DoctorModel doctorModel);
}
