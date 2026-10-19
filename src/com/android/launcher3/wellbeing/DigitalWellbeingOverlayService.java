/*
 * Optional floating reminder overlay for Rocket Launcher Digital Wellbeing.
 * See COPYING and THIRD_PARTY_LICENSES/DRAGON_LAUNCHER.md for licensing and attribution.
 */
package com.android.launcher3.wellbeing;

import com.android.launcher3.R;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DigitalWellbeingOverlayService extends Service {
    private static final String CHANNEL_ID = "rocket_digital_wellbeing_overlay";
    private static final int NOTIFICATION_ID = 47011;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windowManager;
    private View overlay;
    private final Runnable dismiss = this::stopSelf;

    public static void show(android.content.Context context, String appLabel) {
        Intent intent = new Intent(context, DigitalWellbeingOverlayService.class);
        intent.putExtra("app_label", appLabel);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                    "Floating mindful reminders", NotificationManager.IMPORTANCE_LOW));
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        startForeground(NOTIFICATION_ID, builder
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Mindful reminder")
                .setContentText("A short pause can help you reset.")
                .setOngoing(true).build());
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= 23 && !android.provider.Settings.canDrawOverlays(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        String appLabel = intent == null ? "" : intent.getStringExtra("app_label");
        if (appLabel == null || appLabel.trim().isEmpty()) appLabel = "this app";
        try {
            showOverlay(appLabel);
            handler.removeCallbacks(dismiss);
            handler.postDelayed(dismiss, 8000L);
        } catch (RuntimeException e) {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void showOverlay(String appLabel) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(16), dp(20), dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(24, 35, 48));
        bg.setCornerRadius(dp(22));
        bg.setStroke(dp(1), Color.rgb(0, 206, 201));
        card.setBackground(bg);
        card.setElevation(dp(12));

        TextView title = new TextView(this);
        title.setText("Take a mindful break");
        title.setTextColor(Color.rgb(0, 220, 205));
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(title);

        TextView message = new TextView(this);
        message.setText("You've been using " + appLabel + " for a while. Take a breath, stretch, or look away from the screen.");
        message.setTextColor(Color.WHITE);
        message.setTextSize(14);
        message.setPadding(0, dp(8), 0, dp(8));
        card.addView(message);

        Button close = new Button(this);
        close.setText("Got it");
        close.setOnClickListener(v -> stopSelf());
        card.addView(close);

        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = dp(48);
        overlay = card;
        windowManager.addView(overlay, params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (overlay != null && windowManager != null) {
            try { windowManager.removeViewImmediate(overlay); }
            catch (RuntimeException ignored) {}
            overlay = null;
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
