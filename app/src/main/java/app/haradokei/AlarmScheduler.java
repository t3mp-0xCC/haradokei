package app.haradokei;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

final class AlarmScheduler {
    private static final int REQUEST_CODE = 4102;

    private AlarmScheduler() {}

    static void schedule(Context context, long atMillis, long mealAt, int checkCount) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis,
                pendingIntent(context, mealAt, checkCount));
        MealStore.saveAlarm(context, atMillis, mealAt, checkCount);
    }

    static void cancel(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) manager.cancel(pendingIntent(context, 0L, 0));
        MealStore.saveAlarm(context, 0L, 0L, 0);
    }

    private static PendingIntent pendingIntent(Context context, long mealAt, int checkCount) {
        Intent intent = new Intent(context, HungerAlarmReceiver.class)
                .putExtra(HungerAlarmReceiver.EXTRA_MEAL_AT, mealAt)
                .putExtra(HungerAlarmReceiver.EXTRA_CHECK_COUNT, checkCount);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
