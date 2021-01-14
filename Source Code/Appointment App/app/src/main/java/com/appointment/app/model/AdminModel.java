package com.appointment.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdminModel
{
    @SerializedName("email_address")
    @Expose
    public String emailAddress;

    @SerializedName("password")
    @Expose
    public String password;

    private AdminModel(String emailAddress, String password)
    {
        this.emailAddress = emailAddress;
        this.password = password;
    }

    public static AdminModel authenticate(String emailAddress, String password)
    {
        return new AdminModel(emailAddress, password);
    }
}
