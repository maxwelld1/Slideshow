package darre.zflip.slideshow;

import static android.content.Context.MODE_PRIVATE;

import static darre.zflip.slideshow.WallpaperSlideshowService.KEY_CLEAN_EXIT;

import android.Manifest;
import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


public class TriggerNotification extends BroadcastReceiver {

    private static Integer notifID = 0;
    private static final String CHANNEL_ID = "WallpaperServiceChannel";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("service_state", MODE_PRIVATE);
        PendingIntent pi;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Log.d("CreateNotificationChannel", "Creating 'Wallpaper Service Channel'. (NotificationChannel)");
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Wallpaper Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );

        notificationManager.createNotificationChannel(serviceChannel);
        notificationManager.cancelAll();

        boolean cleanExit = prefs.getBoolean(KEY_CLEAN_EXIT, true);

        try {

            if (!cleanExit) {
                // This means the app did not exit cleanly last time due to force stop
                Log.w("ForceStopDetect", "Possible force stop or crash detected. Initiating recovery.");
                Intent i = new Intent(context, WallpaperSlideshowService.class).setAction("UNREGISTER_SERVICE");
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    i.putExtras(new Bundle(extras));
                }

                context.startForegroundService(i);

                return;
            }

            notifID = (int) System.currentTimeMillis(); // Generates a unique ID based on the current time.

            Log.d("ReceiverDebug", "Received broadcast: "+intent.getAction()+". Starting WallpaperSlideshowService.");

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            pi = PendingIntent.getBroadcast(context,
                    0,
                    new Intent(context, StartWallpaperSlideshowService.class)
                            .setAction("darre.zflip.slideshow.RegisterWallpaperService")
                            .putExtra("NOTIF_ID", notifID),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Notification notification =
                    new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_media_play)
                            .setContentTitle("Wallpaper Slideshow Service")
                            .setContentText("Tap to start .\\WallpaperSlideshowService")
                            .setContentIntent(pi)   // user tap triggers receiver
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .build();

            notificationManager.notify(notifID, notification);

        } catch (ForegroundServiceStartNotAllowedException e) {
            Log.e("NotificationFailed", "Foreground WallpaperSlideshowService blocked by system policy.", e);
        } catch (IllegalStateException e) {
            Log.e("NotificationFailed", "Launching WallpaperSlideshowService failed.", e);
        }
    }

}