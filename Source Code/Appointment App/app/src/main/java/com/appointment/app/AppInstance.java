package com.appointment.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appointment.app.api.FCMServerAPI;
import com.appointment.app.model.FCMModel;
import com.appointment.app.model.ServerResponse;
import com.appointment.app.util.Constants;
import com.appointment.app.util.DialogUtil;
import com.appointment.app.util.PreferenceUtil;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppInstance extends Application
{
    private static Retrofit retrofit;
    private static Gson gson;

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    public static Retrofit retrofit()
    {
        if(retrofit == null)
        {
            if(gson == null)
                gson = new Gson()
                        .newBuilder()
                        .setLenient()
                        .excludeFieldsWithoutExposeAnnotation()
                        .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }

    public static void getFCMToken(Context context)
    {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                context.getResources().getString(R.string.fcm_token_key, task.getResult());
                saveTokenId(context, context.getResources().getString(R.string.fcm_token_key, task.getResult()));
            }
        });
    }

    public static void logoutFCMToken(Context context, OnLogoutListener listener)
    {
        PreferenceUtil.bindWith(context);
        FirebaseMessaging.getInstance().setAutoInitEnabled(false);
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
           if(task.isSuccessful())
               logoutFromServer(context, listener);
        });
    }

    private static void logoutFromServer(Context context, OnLogoutListener listener)
    {
        PreferenceUtil.bindWith(context);
        DialogUtil.progressDialog(context, "Logging out...", context.getResources().getColor(R.color.warningColor), false);
        FCMServerAPI api = retrofit().create(FCMServerAPI.class);
        Call<ServerResponse<FCMModel>> call = api.logoutFCM(String.valueOf(PreferenceUtil.getInt("user_id", 0)), FCMModel.newInstance(PreferenceUtil.getString("user_token", "")));
        call.enqueue(new Callback<ServerResponse<FCMModel>>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse<FCMModel>> call, @NonNull Response<ServerResponse<FCMModel>> response)
            {
                ServerResponse<FCMModel> server = response.body();
                DialogUtil.dismissDialog();

                if(server != null)
                {
                    String message = server.message;
                    boolean success = !server.hasError;

                    if(listener != null)
                        listener.onLogout(message, success);
                }
                else if(listener != null)
                    listener.onLogout("Server failed to respond to your request", false);

                call.cancel();
                Log.i("RESP_LOG_OUT_FUNC", response.body().toString());
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse<FCMModel>> call, @NonNull Throwable t)
            {
                DialogUtil.dismissDialog();
                if(listener != null)
                    listener.onLogout(t.getLocalizedMessage(), false);

                call.cancel();
                Log.i("FAIL_LOG_OUT_FUNC", t.getLocalizedMessage());
            }
        });
    }

    private static void saveTokenId(Context context, String token)
    {
        PreferenceUtil.bindWith(context);
        FCMServerAPI api = retrofit().create(FCMServerAPI.class);
        Call<ServerResponse<FCMModel>> call = api.saveTokenId(String.valueOf(PreferenceUtil.getInt("user_id", 0)), FCMModel.newInstance(token));
        call.enqueue(new Callback<ServerResponse<FCMModel>>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse<FCMModel>> call, @NonNull Response<ServerResponse<FCMModel>> response)
            {
                call.cancel();
                PreferenceUtil.putString("user_token", token);
                Log.i("RESP_SAVE_TOKEN_ID_FUNC", response.body().toString());
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse<FCMModel>> call, @NonNull Throwable t)
            {
                call.cancel();
                Log.i("FAIL_SAVE_TOKEN_ID_FUNC", t.getLocalizedMessage());
            }
        });
    }

    public interface OnLogoutListener {
        void onLogout(String message, boolean success);
    }
}

