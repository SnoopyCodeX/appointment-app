package com.appointment.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.appointment.app.service.AlarmReceiver;

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

    public void scheduleAlarm(Calendar calendar, int id)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public void cancelAlarm(int id)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pendingIntent);
    }
}
