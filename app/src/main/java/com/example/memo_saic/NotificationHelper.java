package com.example.memo_saic;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NotificationHelper {
    private static final String CHANNEL_ID = "memo_saic_reminders";
    public static final int NOTIFICATION_PERMISSION_CODE = 123;

    // Time intervals in milliseconds
    private static final long HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1);
    private static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final long MIN_NOTIFICATION_INTERVAL = HOUR_IN_MILLIS * 4; // 4 hours
    private static final long MAX_NOTIFICATION_INTERVAL = DAY_IN_MILLIS * 2;  // 2 days

    private MainActivity activity;
    private Random random;
    private Handler notificationHandler;
    private Runnable notificationRunnable;

    // List of memory-related reminder messages
    private final List<String> reminderMessages = Arrays.asList(
            "You're missing opportunities to gather memories!",
            "Time to create new memories in your area!",
            "When was the last time you captured a moment?",
            "Some memories fade, but photos are forever!",
            "Take a moment to capture your surroundings today",
            "Looking for new photo locations nearby?",
            "Your photo collection is waiting to grow!",
            "Create memories today that you'll cherish tomorrow",
            "Haven't seen new photos from you in a while...",
            "Every day offers new photo opportunities!",
            "Your camera misses you!"
    );

    public NotificationHelper(MainActivity activity) {
        this.activity = activity;
        this.random = new Random();
        this.notificationHandler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
        setupRecurringNotifications();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Memory Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Memory collection reminder notifications");

            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    public void displayNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission();
                return;
            }
        }

        // Get a random message from the list
        String message = reminderMessages.get(random.nextInt(reminderMessages.size()));

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Memory Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
        notificationManager.notify(1, mBuilder.build());
    }

    // Set up the recurring notification system
    private void setupRecurringNotifications() {
        notificationRunnable = new Runnable() {
            @Override
            public void run() {
                displayNotification();
                // Schedule the next notification with a random interval
                long nextInterval = MIN_NOTIFICATION_INTERVAL +
                        random.nextLong() % (MAX_NOTIFICATION_INTERVAL - MIN_NOTIFICATION_INTERVAL);
                notificationHandler.postDelayed(this, nextInterval);
            }
        };

        // Start the first notification with a random delay between 1-8 hours
        long initialDelay = random.nextInt(8) * HOUR_IN_MILLIS;
        notificationHandler.postDelayed(notificationRunnable, initialDelay);
    }

    // Call this method to stop the notifications, e.g., when the app is being closed
    public void stopNotifications() {
        if (notificationHandler != null && notificationRunnable != null) {
            notificationHandler.removeCallbacks(notificationRunnable);
        }
    }
}