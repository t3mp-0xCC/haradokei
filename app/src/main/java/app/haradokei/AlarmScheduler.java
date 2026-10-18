package app.haradokei;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

final class AlarmScheduler {
    private static final int REQUEST_CODE = 4102;

    private AlarmScheduler() {}

    static void schedule(Context context, long atMillis) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent(context));
        MealStore.saveAlarmAt(context, atMillis);
    }

    static void cancel(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) manager.cancel(pendingIntent(context));
        MealStore.saveAlarmAt(context, 0L);
    }

    private static PendingIntent pendingIntent(Context context) {
        Intent intent = new Intent(context, HungerAlarmReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
