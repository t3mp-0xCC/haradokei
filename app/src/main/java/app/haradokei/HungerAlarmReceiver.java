package app.haradokei;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

public final class HungerAlarmReceiver extends BroadcastReceiver {
    static final String CHANNEL_ID = "hunger_reminders";
    static final String EXTRA_MEAL_AT = "meal_at";
    static final String EXTRA_CHECK_COUNT = "check_count";

    @Override
    public void onReceive(Context context, Intent intent) {
        long mealAt = intent.getLongExtra(EXTRA_MEAL_AT, MealStore.alarmMealAt(context));
        if (mealAt == 0L) mealAt = MealStore.latestMealAt(context);
        int checkCount = intent.getIntExtra(EXTRA_CHECK_COUNT, MealStore.alarmCheckCount(context));
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        createChannel(context, manager);
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent open = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        android.app.Notification notification = new android.app.Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(checkCount == 0
                        ? context.getString(R.string.notification_title)
                        : context.getString(R.string.notification_recheck_title))
                .setContentText(context.getString(R.string.notification_feedback_text))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setCategory(android.app.Notification.CATEGORY_REMINDER)
                .addAction(feedbackAction(context, "もう空腹", HungerFeedbackReceiver.ALREADY_HUNGRY,
                        mealAt, checkCount))
                .addAction(feedbackAction(context, "ちょうどいい", HungerFeedbackReceiver.JUST_RIGHT,
                        mealAt, checkCount))
                .addAction(feedbackAction(context, "まだ", HungerFeedbackReceiver.NOT_YET,
                        mealAt, checkCount))
                .build();
        manager.notify(1001, notification);
        MealStore.saveAlarmAt(context, 0L);
        MealStore.setPendingMealAt(context, mealAt);
    }

    private static android.app.Notification.Action feedbackAction(
            Context context, String label, int response, long mealAt, int checkCount) {
        Intent feedback = new Intent(context, HungerFeedbackReceiver.class)
                .setData(Uri.parse("haradokei://feedback/" + mealAt + "/" + response))
                .putExtra(EXTRA_MEAL_AT, mealAt)
                .putExtra(EXTRA_CHECK_COUNT, checkCount)
                .putExtra(HungerFeedbackReceiver.EXTRA_RESPONSE, response);
        PendingIntent pending = PendingIntent.getBroadcast(
                context,
                4300 + response,
                feedback,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new android.app.Notification.Action.Builder(0, label, pending).build();
    }

    static void createChannel(Context context, NotificationManager manager) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.notification_channel_description));
        manager.createNotificationChannel(channel);
    }
}
