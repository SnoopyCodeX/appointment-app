package com.appointment.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdminModel
{
    @SerializedName("id")
    @Expose(serialize = false)
    public int id;

    @SerializedName("email_address")
    @Expose
    public String emailAddress;

    @SerializedName("old_password")
    @Expose(deserialize = false)
    public String oldPassword;

    @SerializedName("new_password")
    @Expose(deserialize = false)
    public String newPassword;

    @SerializedName("password")
    @Expose(deserialize = false)
    public String password;

    private AdminModel(String emailAddress, String password, String oldPassword, String newPassword)
    {
        this.emailAddress = emailAddress;
        this.password = password;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public static AdminModel authenticate(String emailAddress, String password)
    {
        return new AdminModel(emailAddress, password, "", "");
    }

    public static AdminModel changePassword(String oldPassword, String newPassword)
    {
        return new AdminModel("", "", oldPassword, newPassword);
    }
}
