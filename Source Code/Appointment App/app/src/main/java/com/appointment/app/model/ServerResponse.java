package com.appointment.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * A generic class for Server Response
 *
 * @copyright 2020
 * @author John Roy L. Calimlim
 * @since 1.0
 * @version 1.0
 */
public class ServerResponse<E>
{
    @SerializedName("message")
    @Expose(serialize = false, deserialize = true)
    public String message;

    @SerializedName("hasError")
    @Expose(serialize = false, deserialize = true)
    public boolean hasError;

    @SerializedName("data")
    @Expose(serialize = false, deserialize = true)
    public List<E> data;
}
