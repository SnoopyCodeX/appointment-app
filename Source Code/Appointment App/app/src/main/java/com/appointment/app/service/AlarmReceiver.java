package com.appointment.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.appointment.app.service.worker.NotificationWorker;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String title = intent.getStringExtra("notification_title");
        String message = intent.getStringExtra("notification_message");

        Data data = new Data.Builder()
                .putString("notification_title", title)
                .putString("notification_message", message)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInputData(data)
                .build();

        WorkManager.getInstance().beginWith(workRequest).enqueue();
    }
}
