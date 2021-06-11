package com.example.serverside.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.serverside.Activity.OrderCancel;
import com.example.serverside.Activity.OrderFinish;
import com.example.serverside.Activity.OrderPlace;
import com.example.serverside.Activity.OrderProcessing;
import com.example.serverside.Activity.OrderShipping;
import com.example.serverside.Common.Common;
import com.example.serverside.Helper.NotificationHelper;
import com.example.serverside.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    PendingIntent pendingIntent;
    Intent intent;
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
        String status = data.get("status");

        NotificationHelper helper;
        Notification.Builder builder;

        if(Common.currentUser != null) {
            // Here we will fix to click to notification -> go to Order List
            switch (status){
                //Place
                case "0":
                    intent = new Intent(this, OrderPlace.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    break;
                // Shipping
                case "1":
                    intent = new Intent(this, OrderShipping.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    break;
                // Finished
                case "2":
                    intent = new Intent(this, OrderFinish.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    break;
                // Cancel
                case "3":
                    intent = new Intent(this, OrderCancel.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    break;
                // Processing
                default:
                    intent = new Intent(this, OrderProcessing.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            }


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
        String status = data.get("status");

        if (Common.currentUser != null) {

            RemoteMessage.Notification notification = remoteMessage.getNotification();
            switch (status){
                //Place
                case "0":
                    intent = new Intent(this, OrderPlace.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    break;
                // Shipping
                case "1":
                    intent = new Intent(this, OrderShipping.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    break;
                // Finished
                case "2":
                    intent = new Intent(this, OrderFinish.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    break;
                // Cancel
                case "3":
                    intent = new Intent(this, OrderCancel.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    break;
                // Processing
                default:
                    intent = new Intent(this, OrderProcessing.class);
                    intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            }

            Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_menu_manage)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defSoundUri)
                    .setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());
        }else {
            Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_menu_manage)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defSoundUri);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());
        }
    }
}