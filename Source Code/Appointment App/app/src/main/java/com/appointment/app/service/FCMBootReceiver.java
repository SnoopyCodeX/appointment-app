package com.appointment.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FCMBootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent fcm = new Intent("com.google.firebase.MESSAGING_EVENT");
        fcm.setClass(context, FCMNotificationService.class);
        context.startService(fcm);
    }
}
