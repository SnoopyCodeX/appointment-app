package com.appointment.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class is a model class for Appointment Details
 *
 * @copyright 2020
 * @author John Roy L. Calimlim
 * @since 1.0
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class AppointmentModel
{
    public static enum Status
    {
        APPROVED(-1),
        DECLINED(0),
        PENDING(1),
        CANCELLED(2),
        RESCHEDULED(3);

        private int value;
        Status(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }
    }

    @SerializedName("id")
    @Expose(serialize = false)
    public int id;

    @SerializedName("date")
    @Expose
    public String date;

    @SerializedName("time")
    @Expose
    public String time;

    @SerializedName("reason")
    @Expose
    public String reason;

    @SerializedName("gender")
    @Expose
    public String gender;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("age")
    @Expose
    public int age;

    @SerializedName("address")
    @Expose
    public String address;

    @SerializedName("medical_field")
    @Expose
    public String medicalField;

    @SerializedName("identity")
    @Expose
    public String identity;

    @SerializedName("ownerId")
    @Expose(deserialize = false)
    public int ownerId;

    @SerializedName("doctorId")
    @Expose(deserialize = false)
    public int doctorId;

    @SerializedName("doctor")
    @Expose(serialize = false)
    public String doctor;

    @SerializedName("owner")
    @Expose(serialize = false)
    public String owner;

    @SerializedName("status")
    @Expose
    public String status;

    @SerializedName("isDoctor")
    @Expose(serialize = false)
    public boolean isDoctor = false;

    public AppointmentModel(int ownerId, int doctorId, String identity, String medicalField, String name, String gender, String address, int age, String reason, String date, String time, String status)
    {
        this.ownerId = ownerId;
        this.doctorId = doctorId;
        this.medicalField = medicalField;
        this.name = name;
        this.gender = gender;
        this.address = address;
        this.identity = identity;
        this.age = age;
        this.reason = reason;
        this.date = date;
        this.time = time;
    }

    public static AppointmentModel newAppointment(int ownerId, int doctorId, String identity, String medicalField, String name, String gender, String address, int age, String reason, String date, String time, String status)
    {
        return new AppointmentModel(ownerId, doctorId, identity, medicalField, name, gender, address, age, reason, date, time,status);
    }
}
