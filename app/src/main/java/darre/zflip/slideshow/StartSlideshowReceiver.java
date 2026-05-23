package darre.zflip.slideshow;

import android.Manifest;
import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class StartSlideshowReceiver extends android.content.BroadcastReceiver {

    private static Integer notifID = 0;
    private static final String CHANNEL_ID = "WallpaperServiceChannel";

    public void onReceive(Context context, Intent intent) {

        Log.d("CreateNotificationChannel", "Creating 'Wallpaper Service Channel'. (NotificationChannel)");
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Wallpaper Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );

        notifID = (int) System.currentTimeMillis();

        Context deviceProtectedContext = context.createDeviceProtectedStorageContext();

        if (Log.isLoggable("BOOT_RECEIVED", Log.DEBUG)) {
                Log.d("BOOT_RECEIVED", "Starting WallpaperSlideshowService");
        }

        Log.d("ReceiverDebug", "StartSlideshowReceiver triggered: " + intent.getAction());


        if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("ReceiverDebug", "Device booted (locked)");
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("ReceiverDebug", "Device booted (after unlock)");
            PendingIntent pi;
            Log.d("ReceiverDebug", "Received broadcast: "+intent.getAction()+". Starting WallpaperSlideshowService.");
            pi = PendingIntent.getBroadcast(deviceProtectedContext,
                    0,
                    new Intent(deviceProtectedContext, StartWallpaperSlideshowService.class)
                            .setAction("darre.zflip.slideshow.RegisterWallpaperService")
                            .putExtra("NOTIF_ID", notifID),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (ActivityCompat.checkSelfPermission(deviceProtectedContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            try {

                Notification notification =
                        new NotificationCompat.Builder(deviceProtectedContext, CHANNEL_ID)
                                .setSmallIcon(android.R.drawable.ic_media_play)
                                .setContentTitle("Wallpaper Slideshow Service")
                                .setContentText("Tap to start .\\WallpaperSlideshowService")
                                .setContentIntent(pi)
                                .build();

                NotificationManager notificationManager = (NotificationManager) deviceProtectedContext.getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.createNotificationChannel(serviceChannel);

                notificationManager.notify(notifID, notification);

            } catch (ForegroundServiceStartNotAllowedException e) {
                Log.e("NotificationFailed", "Foreground WallpaperSlideshowService blocked by system policy.", e);
            } catch (IllegalStateException e) {
                Log.e("NotificationFailed", "Launching WallpaperSlideshowService failed.", e);
            }
        }
    }
}