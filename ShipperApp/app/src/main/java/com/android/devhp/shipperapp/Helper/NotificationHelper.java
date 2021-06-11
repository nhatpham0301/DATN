package com.android.devhp.shipperapp.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.android.devhp.shipperapp.R;

public class NotificationHelper extends ContextWrapper {

    private static final String ORDER_FOOD_APP_CHANEL_ID = "com.android.devhp.shipperapp.ORDERFOODAPP";
    private static final String ORDER_FOOD_APP_CHANEL_NAME = "Order Food App";

    private NotificationManager manager;
    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            // Only working this function if API is 26 or higher
        {
            createChanel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChanel() {

        NotificationChannel orderFoodChanel = new NotificationChannel(ORDER_FOOD_APP_CHANEL_ID,
                ORDER_FOOD_APP_CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        orderFoodChanel.enableLights(false);
        orderFoodChanel.enableVibration(true);
        orderFoodChanel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(orderFoodChanel);
    }

    public NotificationManager getManager() {

        if(manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getOrderFoodAppChanelNotification(
            String title, String body,
            PendingIntent contentIntent, Uri soundUri){

        return new android.app.Notification.Builder(getApplicationContext(), ORDER_FOOD_APP_CHANEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_baseline_local_shipping_24)
                .setSound(soundUri)
                .setAutoCancel(false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getOrderFoodAppChanelNotification(
            String title, String body, Uri soundUri){

        return new android.app.Notification.Builder(getApplicationContext(), ORDER_FOOD_APP_CHANEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_baseline_local_shipping_24)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}