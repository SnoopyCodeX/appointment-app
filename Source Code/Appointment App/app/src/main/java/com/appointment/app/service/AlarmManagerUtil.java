package com.appointment.app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmManagerUtil
{
    private AlarmManager am;
    private Context context;

    private AlarmManagerUtil()
    {}

    private AlarmManagerUtil(Context context)
    {
        this.am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
    }

    public static AlarmManagerUtil getInstance(Context context)
    {
        return new AlarmManagerUtil(context);
    }

    public AlarmManagerUtil scheduleAlarm(Calendar calendar)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        return this;
    }

    public AlarmManagerUtil cancelAlarm()
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        am.cancel(pendingIntent);

        return this;
    }
}
