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

    @SerializedName("age")
    @Expose
    public String age;

    @SerializedName("address")
    @Expose
    public String address;

    @SerializedName("contact_number")
    @Expose
    public String contactNumber;

    @SerializedName("specialty")
    @Expose
    public String specialty;

    @SerializedName("time")
    @Expose(serialize = false)
    public String time;

    @SerializedName("schedule")
    @Expose(serialize = false)
    public String schedule;

    private DoctorModel()
    {}

    private DoctorModel(String fullname, String gender, String age, String address, String contactNumber, String specialty, String time)
    {
        this.fullname = fullname;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.contactNumber = contactNumber;
        this.time = time;
        this.specialty = specialty;
    }

    private DoctorModel(String fullname, String specialty)
    {
        this.fullname = fullname;
        this.specialty = specialty;

        this.gender = "";
        this.age = "";
        this.address = "";
        this.time = "";
        this.contactNumber = "";
    }

    public static DoctorModel newDoctor(String fullname, String gender, String age, String address, String contactNumber, String specialty, String time)
    {
        return new DoctorModel(fullname, gender, age, address, contactNumber, specialty, time);
    }

    public static DoctorModel loginModel(String fullname, String specialty)
    {
        return new DoctorModel(fullname, specialty);
    }
}
