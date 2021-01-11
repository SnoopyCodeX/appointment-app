package com.appointment.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.appointment.app.service.worker.NotificationWorker;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "snoopy:alarm_wakelock");
        wl.acquire(10*60*1000L); // 10 minutes but in milliseconds format (1000ms = 1s, thus, 1000ms x 60 = 1min, then, (1000ms x 60) x 10 = 10min)

        String title = intent.getStringExtra("notification_title");
        String message = intent.getStringExtra("notification_message");

        Data data = new Data.Builder()
                .putString("notification_title", title)
                .putString("notification_message", message)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInputData(data)
                .build();

        WorkManager.getInstance()
                .beginWith(workRequest)
                .enqueue();

        wl.release();
    }
}
