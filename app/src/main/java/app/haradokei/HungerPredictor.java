package app.haradokei;

import java.util.List;

final class HungerPredictor {
    private static final int[] DEFAULT_MINUTES = {0, 120, 180, 240, 300, 360};
    private static final double[] PORTION_FACTORS = {0, 0.85, 1.0, 1.15};
    private static final long MIN_VALID_INTERVAL = 45L * 60L * 1000L;
    private static final long MAX_VALID_INTERVAL = 10L * 60L * 60L * 1000L;

    private HungerPredictor() {}

    static int predictMinutes(int fullness, int portion, List<MealRecord> history) {
        int safeFullness = Math.max(1, Math.min(5, fullness));
        int safePortion = Math.max(1, Math.min(3, portion));
        int profileDefault = (int) Math.round(DEFAULT_MINUTES[safeFullness]
                * PORTION_FACTORS[safePortion] * UserProfile.initialIntervalFactor());
        double weightedMinutes = 0;
        double totalWeight = 0;
        int directSamples = 0;

        // Direct reports of hunger are preferred. Old records without feedback can
        // still contribute their next meal interval, but at much lower confidence.
        for (int i = 0; i < history.size(); i++) {
            MealRecord before = history.get(i);
            boolean direct = before.hungerAtMillis > before.eatenAtMillis;
            long interval;
            if (direct) {
                interval = before.hungerAtMillis - before.eatenAtMillis;
            } else if (i + 1 < history.size()) {
                interval = history.get(i + 1).eatenAtMillis - before.eatenAtMillis;
            } else {
                continue;
            }
            if (interval < MIN_VALID_INTERVAL || interval > MAX_VALID_INTERVAL) continue;
            int distance = Math.abs(before.fullness - safeFullness);
            if (distance > 1) continue;
            int portionDistance = Math.abs(before.portion - safePortion);
            double similarity = (distance == 0 ? 1.0 : 0.35)
                    * (portionDistance == 0 ? 1.0 : 0.55);
            double recency = 0.65 + 0.35 * (i + 1.0) / Math.max(1, history.size() - 1);
            double weight = similarity * recency * (direct ? 1.0 : 0.25);
            weightedMinutes += interval / 60_000.0 * weight;
            totalWeight += weight;
            if (direct && distance == 0 && portionDistance == 0) directSamples++;
        }

        if (totalWeight == 0) return profileDefault;
        double learned = weightedMinutes / totalWeight;
        double confidence = Math.min(0.85, (directSamples * 1.4 + totalWeight * 0.35) / 6.0);
        double blended = profileDefault * (1.0 - confidence) + learned * confidence;
        return (int) Math.round(Math.max(90, Math.min(480, blended)) / 5.0) * 5;
    }
}
