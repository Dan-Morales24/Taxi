package com.example.rastreosgps.taxi.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.rastreosgps.taxi.Model.RiderModel;
import com.example.rastreosgps.taxi.R;

public class Common {


    public static final String DRIVER_INFO_REFERENCE ="Riders" ;
    public static final String TOKEN_REFERENCE = "Token";
    public static RiderModel currentRide;
    public static final String NOTI_TITLE ="title";
    public static final String NOTI_CONTENT ="message";

    public static  String builderWelcomeMessage(){
        if(Common.currentRide != null){
            return  new StringBuilder("")
                    .append(Common.currentRide.getFirstName())
                    .append(" ")
                    .append(Common.currentRide.getLastName()).toString();

        }
        else
            return "";
    }

    public static void showNotification (Context context , int id, String title, String body, Intent intent){

        PendingIntent pendingIntent = null;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            String NOTIFICATION_CHANNEL_ID ="Taxi Driver";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "Taxi Driver ",NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("Taxi Driver");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);

            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(false)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_baseline_directions_car_24));

            if(pendingIntent != null)
            {
                builder.setContentIntent(pendingIntent);
            }
            Notification notification = builder.build();
            notificationManager.notify(id,notification);

    }


}
