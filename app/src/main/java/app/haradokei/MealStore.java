package app.haradokei;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MealStore {
    private static final String PREFS = "haradokei_data";
    private static final String KEY_MEALS = "meals";
    private static final String KEY_ALARM_AT = "alarm_at";
    private static final String KEY_ALARM_MEAL_AT = "alarm_meal_at";
    private static final String KEY_ALARM_CHECK_COUNT = "alarm_check_count";
    private static final String KEY_PENDING_MEAL_AT = "pending_meal_at";
    private static final int MAX_RECORDS = 120;

    private MealStore() {}

    static List<MealRecord> load(Context context) {
        String raw = prefs(context).getString(KEY_MEALS, "[]");
        List<MealRecord> records = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                int fullness = item.getInt("fullness");
                if (fullness >= 1 && fullness <= 5) {
                    int portion = item.optInt("portion", 2);
                    records.add(new MealRecord(
                            item.getLong("at"),
                            fullness,
                            Math.max(1, Math.min(3, portion)),
                            item.optLong("hungerAt", 0L),
                            item.optInt("hungerRating", -1),
                            item.optLong("checkAt", 0L)));
                }
            }
        } catch (JSONException ignored) {
            // A malformed local record should not prevent the user from opening the app.
        }
        Collections.sort(records, (a, b) -> Long.compare(a.eatenAtMillis, b.eatenAtMillis));
        return records;
    }

    static void add(Context context, MealRecord record) {
        List<MealRecord> records = load(context);
        records.add(record);
        int from = Math.max(0, records.size() - MAX_RECORDS);
        save(context, records.subList(from, records.size()));
    }

    static boolean removeLast(Context context) {
        List<MealRecord> records = load(context);
        if (records.isEmpty()) return false;
        records.remove(records.size() - 1);
        save(context, records);
        return true;
    }

    static boolean updateHungerCheck(Context context, long mealAt, long checkAt, int rating) {
        List<MealRecord> records = load(context);
        for (int i = records.size() - 1; i >= 0; i--) {
            MealRecord item = records.get(i);
            if (item.eatenAtMillis != mealAt) continue;
            int safeRating = Math.max(0, Math.min(10, rating));
            long hungerAt = item.hungerAtMillis;
            if (safeRating >= 6 && hungerAt == 0L) hungerAt = checkAt;
            records.set(i, new MealRecord(item.eatenAtMillis, item.fullness, item.portion,
                    hungerAt, safeRating, checkAt));
            save(context, records);
            return true;
        }
        return false;
    }

    static long latestMealAt(Context context) {
        List<MealRecord> records = load(context);
        return records.isEmpty() ? 0L : records.get(records.size() - 1).eatenAtMillis;
    }

    static void saveAlarmAt(Context context, long millis) {
        prefs(context).edit().putLong(KEY_ALARM_AT, millis).apply();
    }

    static void saveAlarm(Context context, long millis, long mealAt, int checkCount) {
        prefs(context).edit()
                .putLong(KEY_ALARM_AT, millis)
                .putLong(KEY_ALARM_MEAL_AT, mealAt)
                .putInt(KEY_ALARM_CHECK_COUNT, checkCount)
                .apply();
    }

    static long alarmAt(Context context) {
        return prefs(context).getLong(KEY_ALARM_AT, 0L);
    }

    static long alarmMealAt(Context context) {
        return prefs(context).getLong(KEY_ALARM_MEAL_AT, 0L);
    }

    static int alarmCheckCount(Context context) {
        return prefs(context).getInt(KEY_ALARM_CHECK_COUNT, 0);
    }

    static void setPendingMealAt(Context context, long mealAt) {
        prefs(context).edit().putLong(KEY_PENDING_MEAL_AT, mealAt).apply();
    }

    static long pendingMealAt(Context context) {
        return prefs(context).getLong(KEY_PENDING_MEAL_AT, 0L);
    }

    private static void save(Context context, List<MealRecord> records) {
        JSONArray array = new JSONArray();
        for (MealRecord item : records) {
            JSONObject json = new JSONObject();
            try {
                json.put("at", item.eatenAtMillis);
                json.put("fullness", item.fullness);
                json.put("portion", item.portion);
                json.put("hungerAt", item.hungerAtMillis);
                json.put("hungerRating", item.lastHungerRating);
                json.put("checkAt", item.lastCheckAtMillis);
                array.put(json);
            } catch (JSONException ignored) {
                // Primitive values cannot normally fail to serialize.
            }
        }
        prefs(context).edit().putString(KEY_MEALS, array.toString()).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
