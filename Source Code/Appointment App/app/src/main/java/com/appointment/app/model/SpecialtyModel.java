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

    private SpecialtyModel()
    {}

    private SpecialtyModel(String name)
    {
        this.name = name;
    }

    public static SpecialtyModel newSpecialtyModel(String name)
    {
        return new SpecialtyModel(name);
    }
}
