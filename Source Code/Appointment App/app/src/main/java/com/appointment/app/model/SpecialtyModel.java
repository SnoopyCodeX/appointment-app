package com.appointment.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SpecialtyModel
{
    @SerializedName("id")
    @Expose(serialize = false)
    public int id;

    @SerializedName("name")
    @Expose
    public String name;
}
