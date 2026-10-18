package app.haradokei;

final class MealRecord {
    final long eatenAtMillis;
    final int fullness;

    MealRecord(long eatenAtMillis, int fullness) {
        this.eatenAtMillis = eatenAtMillis;
        this.fullness = fullness;
    }
}
