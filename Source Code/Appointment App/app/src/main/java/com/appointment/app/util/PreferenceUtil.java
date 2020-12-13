package com.appointment.app.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

@SuppressWarnings({"ALL", "RedundantSuppression"})
public final class PreferenceUtil
{
    private static SharedPreferences sharedPreferences;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static void bindWith(Context ctx)
    {
        context = ctx;
    }

    public static SharedPreferences getPreference(String name, int mode)
    {
        return (sharedPreferences = context.getSharedPreferences(name, mode));
    }

    public static SharedPreferences getPreference()
    {
        return (sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static String getString(String key, String defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getString(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static float getFloat(String key, float defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getFloat(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getLong(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getInt(key, defaultValue);
    }

    public static boolean putString(String key, String value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        return edit.commit();
    }

    public static boolean putBoolean(String key, boolean value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(key, value);
        return edit.commit();
    }

    public static boolean putInt(String key, int value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(key, value);
        return edit.commit();
    }

    public static boolean putFloat(String key, float value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putFloat(key, value);
        return edit.commit();
    }

    public static boolean putLong(String key, long value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong(key, value);
        return edit.commit();
    }
}