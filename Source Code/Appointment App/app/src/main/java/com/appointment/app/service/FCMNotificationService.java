package com.appointment.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.appointment.app.R;
import com.appointment.app.activity.SplashActivity;
import com.appointment.app.util.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

@SuppressWarnings("ALL")
public class FCMNotificationService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData() != null && remoteMessage.getData().size() > 0)
            processData(remoteMessage.getData());

        if(remoteMessage.getNotification() != null)
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    }

    @Override
    public void onNewToken(@NonNull String token)
    {
        super.onNewToken(token);
    }

    private void processData(Map<String, String> data)
    {
        String action = data.get("action");

        if(action != null && !action.isEmpty())
        {
            String trigger = "";

            switch(action)
            {
                case "doctor_approveAppointment":
                case "patient_approveAppointment":
                    trigger = Constants.ACTION_APPOINTMENT_APPROVE;
                break;

                case "doctor_declineAppointment":
                case "patient_declineAppointment":
                    trigger = Constants.ACTION_APPOINTMENT_DECLINE;
                break;

                case "doctor_updateAppointment":
                case "patient_updateAppointment":
                    trigger = Constants.ACTION_APPOINTMENT_UPDATE;
                break;

                case "doctor_cancelAppointment":
                case "patient_cancelAppointment":
                    trigger = Constants.ACTION_APPOINTMENT_CANCEL;
                break;

                case "doctor_newAppointment":
                case "patient_newAppointment":
                    trigger = Constants.ACTION_APPOINTMENT_NEW;
                break;

                case "doctor_rescheduleAppointment":
                case "patient_rescheduleAppointment":
                    trigger = Constants.ACTION_APPOINTMENT_RESCHEDULE;
                break;
            }

            Intent event = new Intent(trigger);
            event.putExtra("data", data.get("data"));
            sendBroadcast(event);
        }
    }

    public void sendNotification(String title, String message)
    {
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getApplicationContext().getResources().getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_logo))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(21, notificationBuilder.build());
    }
}