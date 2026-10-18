package app.haradokei;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        long alarmAt = MealStore.alarmAt(context);
        if (alarmAt > System.currentTimeMillis()) {
            AlarmScheduler.schedule(context, alarmAt);
        }
    }
}
