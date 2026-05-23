package darre.zflip.slideshow;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WallpaperSlideshowService extends Service {
    private static ChangeWallpaperService changeWallpaper_S;
    private static final String TICK_TAG = "TickElapsed";
    private static final String TIMER_END = "TimerFinished";
    private static final String SERVICE_DISCONNECT = "ForegroundServiceDisconnected";

    // Shared state
    private static final String PREFS_NAME = "service_state";
    public static final String KEY_CLEAN_EXIT = "clean_exit";
    private static final String KEY_LAST_LAUNCH = "last_launch_marker";
    private static final String KEY_NOTIF_ID = "notification_id";

    private static final String CHANNEL_ID = "WallpaperServiceChannel";
    public static CountDownTimer countdownWallpaperChange;

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Defines callbacks for service binding
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            ChangeWallpaperService.LocalBinder binder = (ChangeWallpaperService.LocalBinder) service;
            changeWallpaper_S = binder.getService();
            changeWallpaper();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(SERVICE_DISCONNECT, "ChangeWallpaperService disconnected.");
        }
    };

    public void onCreate() {
        Intent intent = new Intent(this, ChangeWallpaperService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (Objects.equals(intent.getAction(), "UNREGISTER_SERVICE")) {

            prefs
                    .edit()
                    .putBoolean(KEY_CLEAN_EXIT, true)
                    .putInt(KEY_NOTIF_ID, 0)
                    .apply();

            stopForeground(STOP_FOREGROUND_REMOVE);

            NotificationManager serviceStatus = ContextCompat.getSystemService(this, NotificationManager.class);
            serviceStatus.cancel(prefs.getInt(KEY_NOTIF_ID, 0));

            boolean serviceStop = stopSelfResult(startId);
            Log.d("StopForeground", "Stopped WallpaperSlideshowService "+startId+" (stopSelfResult(startId)) = "+serviceStop+".");

            return flags;
        }

        prefs.edit()
                .putBoolean(KEY_CLEAN_EXIT, false)
                .putLong(KEY_LAST_LAUNCH, System.currentTimeMillis())
                .putInt(KEY_NOTIF_ID, intent.getIntExtra("NOTIF_ID", 0))
                .apply();

        executor.execute(() -> {
            try {

                Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .build();

                Log.d("StartForeground", "Starting WallpaperSlideshowService in the foreground.");

                startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
                NotificationManager serviceStatus = ContextCompat.getSystemService(this, NotificationManager.class);
                serviceStatus.cancel(intent.getIntExtra("NOTIF_ID", 0));

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        });

        initializeTimer();

        return flags;
    }

    private static void initializeTimer() {
        countdownWallpaperChange = new WallpaperChangeTimer();
        countdownWallpaperChange.start();
    }

    private void changeWallpaper() {
        mainHandler.post(() -> {
            try {
                changeWallpaper_S.changeWallpaperColorway();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void onDestroy() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_CLEAN_EXIT, true)
                .putInt(KEY_NOTIF_ID, 0)
                .apply();
        unbindService(connection);
        Intent intent = new Intent(this, ChangeWallpaperService.class);
        stopService(intent);
        executor.shutdown();
    }

    static class WallpaperChangeTimer extends CountDownTimer {

        public WallpaperChangeTimer() {
            super(30000, 1000);
        }

        public void onTick(long millisUntilFinished) {
            Log.d(TICK_TAG, "Tick - "+millisUntilFinished+" ms until finished.");
        }

        public void onFinish() {
            Log.d(TIMER_END, "Reached the end of countdown, changing wallpaper.");
            mainHandler.post(() -> {
                try {
                    changeWallpaper_S.changeWallpaperColorway();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            countdownWallpaperChange.start();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

}