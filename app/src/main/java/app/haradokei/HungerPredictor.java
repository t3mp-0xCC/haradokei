package app.haradokei;

import java.util.List;

final class HungerPredictor {
    private static final int[] DEFAULT_MINUTES = {0, 120, 180, 240, 300, 360};
    private static final long MIN_VALID_INTERVAL = 60L * 60L * 1000L;
    private static final long MAX_VALID_INTERVAL = 10L * 60L * 60L * 1000L;

    private HungerPredictor() {}

    static int predictMinutes(int fullness, List<MealRecord> history) {
        int safeFullness = Math.max(1, Math.min(5, fullness));
        int profileDefault = (int) Math.round(DEFAULT_MINUTES[safeFullness]
                * UserProfile.initialIntervalFactor());
        double weightedMinutes = 0;
        double totalWeight = 0;
        int samples = 0;

        // Recent intervals count a little more, and neighboring fullness levels can
        // contribute when there is not yet enough exact personal data.
        for (int i = 0; i + 1 < history.size(); i++) {
            MealRecord before = history.get(i);
            MealRecord after = history.get(i + 1);
            long interval = after.eatenAtMillis - before.eatenAtMillis;
            if (interval < MIN_VALID_INTERVAL || interval > MAX_VALID_INTERVAL) continue;
            int distance = Math.abs(before.fullness - safeFullness);
            if (distance > 1) continue;
            double similarity = distance == 0 ? 1.0 : 0.35;
            double recency = 0.65 + 0.35 * (i + 1.0) / Math.max(1, history.size() - 1);
            double weight = similarity * recency;
            weightedMinutes += interval / 60_000.0 * weight;
            totalWeight += weight;
            if (distance == 0) samples++;
        }

        if (totalWeight == 0) return profileDefault;
        double learned = weightedMinutes / totalWeight;
        double confidence = Math.min(0.75, (samples + totalWeight * 0.35) / 6.0);
        double blended = profileDefault * (1.0 - confidence) + learned * confidence;
        return (int) Math.round(Math.max(90, Math.min(480, blended)) / 5.0) * 5;
    }
}
