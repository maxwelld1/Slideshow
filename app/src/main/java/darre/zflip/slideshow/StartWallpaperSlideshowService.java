package darre.zflip.slideshow;

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class StartWallpaperSlideshowService extends android.content.BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReceiverDebug", "onReceive: action = " + intent.getAction());

        SharedPreferences prefs = context.getSharedPreferences("service_state", MODE_PRIVATE);
        Integer notifID = prefs.getInt("notification_id", 0);

        if (!notifID.equals(0)) {
            Log.w("NotificationFailed", "Notification failed. Initiating recovery.");
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent i = new Intent(context, WallpaperSlideshowService.class).setAction("UNREGISTER_SERVICE");
            Bundle extras = intent.getExtras();
            if (extras != null) {
                i.putExtras(new Bundle(extras));
            }

            context.startForegroundService(i);
            notificationManager.cancelAll();
        }

        try {
            Intent i = new Intent(context, WallpaperSlideshowService.class).setAction("BOOTSTRAP");
            Bundle extras = intent.getExtras();
            if (extras != null) {
                i.putExtras(new Bundle(extras));
            }

            ContextCompat.startForegroundService(context, i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
