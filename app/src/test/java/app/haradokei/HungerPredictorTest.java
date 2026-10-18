package app.haradokei;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HungerPredictorTest {
    @Test
    public void largerPortionProducesLaterInitialPrediction() {
        int light = HungerPredictor.predictMinutes(3, 1, Collections.emptyList());
        int normal = HungerPredictor.predictMinutes(3, 2, Collections.emptyList());
        int heavy = HungerPredictor.predictMinutes(3, 3, Collections.emptyList());

        assertTrue(light < normal);
        assertTrue(normal < heavy);
    }

    @Test
    public void directHungerReportsOverrideMealIntervalPrior() {
        List<MealRecord> history = new ArrayList<>();
        long start = 1_700_000_000_000L;
        for (int i = 0; i < 4; i++) {
            long mealAt = start + i * 24L * 60L * 60L * 1000L;
            history.add(new MealRecord(mealAt, 3, 2,
                    mealAt + 3L * 60L * 60L * 1000L, 7, mealAt + 3L * 60L * 60L * 1000L));
        }

        int prediction = HungerPredictor.predictMinutes(3, 2, history);
        assertTrue(prediction >= 175 && prediction <= 200);
    }

    @Test
    public void implausiblyShortFeedbackIsIgnored() {
        long mealAt = 1_700_000_000_000L;
        MealRecord invalid = new MealRecord(mealAt, 3, 2,
                mealAt + 10L * 60L * 1000L, 8, mealAt + 10L * 60L * 1000L);
        int withoutHistory = HungerPredictor.predictMinutes(3, 2, Collections.emptyList());
        int withInvalidHistory = HungerPredictor.predictMinutes(3, 2,
                Collections.singletonList(invalid));

        assertTrue(withoutHistory == withInvalidHistory);
    }
}
