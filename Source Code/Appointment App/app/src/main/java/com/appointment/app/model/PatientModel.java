package com.appointment.app.model;

import com.appointment.app.api.PatientAPI;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class is a model class for Patient Accounts
 *
 * @copyright 2020
 * @author John Roy L. Calimlim
 * @since 1.0
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class PatientModel
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

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("password")
    @Expose(deserialize = false)
    public String password;

    private PatientModel()
    {}

    private PatientModel(String fullname, String gender, String age, String address, String contactNumber, String email, String password)
    {
        this.fullname = fullname;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.password = password;
    }

    private PatientModel(String fullname, String email, String password)
    {
        this.fullname = fullname;
        this.email = email;
        this.password = password;

        this.gender = "";
        this.age = "";
        this.address = "";
        this.contactNumber = "";
    }

    public static PatientModel newPatient(String fullname, String gender, String age, String address, String contactNumber, String email, String password)
    {
        return new PatientModel(fullname, gender, age, address, contactNumber, email, password);
    }

    public static PatientModel loginModel(String fullname, String email, String password)
    {
        return new PatientModel(fullname, email, password);
    }
}
