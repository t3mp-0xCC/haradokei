package app.haradokei;

final class MealRecord {
    final long eatenAtMillis;
    final int fullness;
    final int portion;
    final long hungerAtMillis;
    final int lastHungerRating;
    final long lastCheckAtMillis;

    MealRecord(long eatenAtMillis, int fullness, int portion) {
        this(eatenAtMillis, fullness, portion, 0L, -1, 0L);
    }

    MealRecord(long eatenAtMillis, int fullness, int portion, long hungerAtMillis,
               int lastHungerRating, long lastCheckAtMillis) {
        this.eatenAtMillis = eatenAtMillis;
        this.fullness = fullness;
        this.portion = portion;
        this.hungerAtMillis = hungerAtMillis;
        this.lastHungerRating = lastHungerRating;
        this.lastCheckAtMillis = lastCheckAtMillis;
    }
}
