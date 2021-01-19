package com.appointment.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class is a model class for Doctor Accounts
 *
 * @copyright 2020
 * @author John Roy L. Calimlim
 * @since 1.0
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class DoctorModel
{
    @SerializedName("id")
    @Expose(serialize = false)
    public int id;

    @SerializedName("fullname")
    @Expose
    public String fullname;

    @SerializedName("gender")
    @Expose
    public String gender;

    @SerializedName("contact_number")
    @Expose
    public String contactNumber;

    @SerializedName("email_address")
    @Expose
    public String emailAddress;

    @SerializedName("room_number")
    @Expose
    public String roomNumber;

    @SerializedName("password")
    @Expose(deserialize = false)
    public String password;

    @SerializedName("specialty")
    @Expose
    public String specialty;

    @SerializedName("time")
    @Expose
    public String time;

    @SerializedName("schedule")
    @Expose
    public String schedule;

    @SerializedName("old_password")
    @Expose(deserialize = false)
    public String oldPassword;

    @SerializedName("new_password")
    @Expose(deserialize = false)
    public String newPassword;

    private DoctorModel()
    {}

    private DoctorModel(String fullname, String password, String gender, String emailAddress, String contactNumber, String roomNumber, String specialty, String schedule, String time)
    {
        this.fullname = fullname;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.emailAddress = emailAddress;
        this.time = time;
        this.schedule = schedule;
        this.specialty = specialty;
        this.roomNumber = roomNumber;
        this.password = password;
    }

    private DoctorModel(String fullname, String password)
    {
        this.fullname = fullname;
        this.password = password;
    }

    private DoctorModel(int stub, String oldPassword, String newPassword)
    {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public static DoctorModel loginModel(String fullname, String password)
    {
        return new DoctorModel(fullname, password);
    }

    public static DoctorModel changePassword(int stub, String oldPassword, String newPassword)
    {
        return new DoctorModel(stub, oldPassword, newPassword);
    }

    public static DoctorModel newDoctor(String fullname, String password, String gender, String emailAddress, String contactNumber, String roomNumber, String specialty, String schedule, String time)
    {
        return new DoctorModel(fullname, password, gender, emailAddress, contactNumber, roomNumber, specialty, schedule, time);
    }
}
