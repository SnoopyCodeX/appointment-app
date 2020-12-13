package com.appointment.app.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;

public class InternetReceiver extends BroadcastReceiver
{
    private OnConnectivityChangedListener listener;
    private static InternetReceiver internetReceiver;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(listener != null)
            listener.onConnectivityChanged(isConnected(context));
    }

    public static boolean isConnected(@NonNull Context ctx)
    {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
    }

    public static InternetReceiver initiateSelf(@NonNull Context ctx)
    {
        internetReceiver = new InternetReceiver();
        ctx.registerReceiver(internetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        return internetReceiver;
    }

    public void setOnConnectivityChangedListener(OnConnectivityChangedListener listener)
    {
        this.listener = listener;
    }

    public interface OnConnectivityChangedListener
    {
        void onConnectivityChanged(boolean isConnected);
    }
}