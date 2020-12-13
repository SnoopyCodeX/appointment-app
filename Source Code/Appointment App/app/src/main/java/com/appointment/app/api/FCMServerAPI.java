package com.appointment.app.api;

import com.appointment.app.model.FCMModel;
import com.appointment.app.model.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FCMServerAPI
{
    @POST("fcm/{userId}/token")
    Call<ServerResponse<FCMModel>> saveTokenId(@Path("userId") String userId, @Body FCMModel fcm);

    @POST("fcm/{userId}/logout")
    Call<ServerResponse<FCMModel>> logoutFCM(@Path("userId") String userId, @Body FCMModel fcm);
}
