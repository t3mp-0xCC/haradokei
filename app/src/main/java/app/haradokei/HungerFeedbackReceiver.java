package app.haradokei;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public final class HungerFeedbackReceiver extends BroadcastReceiver {
    static final String EXTRA_RESPONSE = "response";
    static final int ALREADY_HUNGRY = 1;
    static final int JUST_RIGHT = 2;
    static final int NOT_YET = 3;
    private static final long RECHECK_DELAY = 30L * 60L * 1000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        long mealAt = intent.getLongExtra(HungerAlarmReceiver.EXTRA_MEAL_AT, 0L);
        if (mealAt == 0L) mealAt = MealStore.latestMealAt(context);
        int response = intent.getIntExtra(EXTRA_RESPONSE, 0);
        int checkCount = intent.getIntExtra(HungerAlarmReceiver.EXTRA_CHECK_COUNT, 0);
        long now = System.currentTimeMillis();

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) manager.cancel(1001);

        if (response == NOT_YET) {
            MealStore.updateHungerCheck(context, mealAt, now, 3);
            MealStore.setPendingMealAt(context, 0L);
            AlarmScheduler.schedule(context, now + RECHECK_DELAY, mealAt, checkCount + 1);
            Toast.makeText(context, "30分後にもう一度お知らせします", Toast.LENGTH_SHORT).show();
            return;
        }

        int rating = response == ALREADY_HUNGRY ? 8 : 6;
        MealStore.updateHungerCheck(context, mealAt, now, rating);
        MealStore.setPendingMealAt(context, 0L);
        AlarmScheduler.cancel(context);
        Toast.makeText(context, "空腹のタイミングを記録しました", Toast.LENGTH_SHORT).show();
    }
}
