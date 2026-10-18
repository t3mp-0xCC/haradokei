package app.haradokei;

import java.time.LocalDate;
import java.time.Period;

final class UserProfile {
    static final LocalDate BIRTH_DATE = LocalDate.of(2002, 4, 27);

    private UserProfile() {}

    static int ageToday() {
        return ageOn(LocalDate.now());
    }

    static int ageOn(LocalDate date) {
        return Math.max(0, Period.between(BIRTH_DATE, date).getYears());
    }

    static double initialIntervalFactor() {
        int age = ageToday();
        // Age and sex are only a light prior: they cannot reliably determine hunger
        // by themselves. Personal meal intervals progressively override this value.
        double maleFactor = 0.98;
        double ageFactor = 0.98 + Math.max(0, Math.min(40, age - 20)) * 0.002;
        return maleFactor * ageFactor;
    }

    static String summary() {
        return "2002年4月27日生まれ・男性・現在" + ageToday() + "歳（誕生日に自動更新）";
    }
}
