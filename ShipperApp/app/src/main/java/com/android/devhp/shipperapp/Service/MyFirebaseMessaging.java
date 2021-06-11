package com.android.devhp.shipperapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.android.devhp.shipperapp.Activity.Home;
import com.android.devhp.shipperapp.Common.Common;
import com.android.devhp.shipperapp.Helper.NotificationHelper;
import com.android.devhp.shipperapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotificationAPI26(remoteMessage);
            } else {
                sendNormalNotification(remoteMessage);
            }
        }
    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        PendingIntent pendingIntent;
        NotificationHelper helper;
        Notification.Builder builder;

        if (Common.currentShipper != null) {
            // Here we will fix to click to notification -> go to Order List
            Intent intent = new Intent(this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            helper = new NotificationHelper(this);
            builder = helper.getOrderFoodAppChanelNotification(title, message,
                    pendingIntent, defSoundUri);

            helper.getManager().notify(new Random().nextInt(), builder.build());
        }else {
            Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            helper = new NotificationHelper(this);
            builder = helper.getOrderFoodAppChanelNotification(title, message, defSoundUri);

            helper.getManager().notify(new Random().nextInt(), builder.build());
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        if (Common.currentShipper != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            Intent intent = new Intent(this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_baseline_local_shipping_24)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defSoundUri)
                    .setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());
        }
        else {
            Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_baseline_local_shipping_24)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defSoundUri);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());
        }
    }
}
