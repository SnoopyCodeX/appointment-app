package com.appointment.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FCMModel
{
    @SerializedName("id")
    @Expose(serialize = false)
    public String fcmId;

    @SerializedName("token")
    @Expose
    public String token;

    @SerializedName("active")
    @Expose(serialize = false)
    public boolean active;

    private FCMModel()
    {}

    private FCMModel(String token)
    {
        this.token = token;
    }

    public static FCMModel newInstance(String token)
    {
        return new FCMModel(token);
    }
}
