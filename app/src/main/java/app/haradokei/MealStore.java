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
                    records.add(new MealRecord(item.getLong("at"), fullness));
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
        JSONArray array = new JSONArray();
        for (MealRecord item : records.subList(from, records.size())) {
            JSONObject json = new JSONObject();
            try {
                json.put("at", item.eatenAtMillis);
                json.put("fullness", item.fullness);
                array.put(json);
            } catch (JSONException ignored) {
                // Primitive values cannot normally fail to serialize.
            }
        }
        prefs(context).edit().putString(KEY_MEALS, array.toString()).apply();
    }

    static boolean removeLast(Context context) {
        List<MealRecord> records = load(context);
        if (records.isEmpty()) return false;
        records.remove(records.size() - 1);
        JSONArray array = new JSONArray();
        for (MealRecord item : records) {
            JSONObject json = new JSONObject();
            try {
                json.put("at", item.eatenAtMillis);
                json.put("fullness", item.fullness);
                array.put(json);
            } catch (JSONException ignored) {}
        }
        prefs(context).edit().putString(KEY_MEALS, array.toString()).apply();
        return true;
    }

    static void saveAlarmAt(Context context, long millis) {
        prefs(context).edit().putLong(KEY_ALARM_AT, millis).apply();
    }

    static long alarmAt(Context context) {
        return prefs(context).getLong(KEY_ALARM_AT, 0L);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
