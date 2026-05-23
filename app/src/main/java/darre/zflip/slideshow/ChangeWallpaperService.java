package darre.zflip.slideshow;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Display;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class ChangeWallpaperService extends android.app.Service {
    private static final String DEF_DISPLAY_WATCH = "DefaultDisplayWatcher";
    private static final String WALLPAPE_PERM = "WallpaperPermissionsTag";
    private WallpaperManager wm;
    private DisplayManager displayManager;
    private static final String CHANNEL_ID = "ScreenMonitorChannel";

    // Binder for Wallpaper management service
    private final IBinder binder = new LocalBinder();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void onCreate() {
        mainHandler.post(() -> {
            wm = WallpaperManager.getInstance(this);
            Log.d(WALLPAPE_PERM, Boolean.toString(wm.isSetWallpaperAllowed()));

            displayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);

            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                    Log.d(DEF_DISPLAY_WATCH, "Added display: " + displayId);

                }

                @Override
                public void onDisplayChanged(int displayId) {
                    Display display = displayManager.getDisplay(displayId);
                    int currentDefault = display.DEFAULT_DISPLAY;

                    // IF cover screen active
                    // Start Lock screen Listener
                    // presentation overlay breaks out of lock screen listener
                    // screen off release the presentation
                    // release lock screen Listener

                    Log.d(DEF_DISPLAY_WATCH, "Display changed: Current " + displayId + " Default " + currentDefault);
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                    Log.d(DEF_DISPLAY_WATCH, "Removed display: " + displayId);
                }
            }, null);
        });
        // Create notification channel for foreground service
        createNotificationChannel();
        // Start service in foreground with a persistent notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Screen Monitor Running")
                .setContentText("Listening for screen on/off events")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setOngoing(true)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    public void changeWallpaperColorway() throws IOException {

        TypedArray images = this.getResources().obtainTypedArray(R.array.loading_images);
        int choice = (int) (Math.random() * images.length());

        mainHandler.post(() -> {
            try {
                wm.setBitmap(BitmapFactory.decodeResource(this.getResources(), images.getResourceId(choice, R.drawable.wallpaper_17)));
                images.recycle();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Screen Monitor Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(serviceChannel);
    }

    @Override
    public void onDestroy() {
        wm.clearWallpaper();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public ChangeWallpaperService getService () {
            return ChangeWallpaperService.this;
        }
    }
}