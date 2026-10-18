package app.haradokei;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION_REQUEST = 501;
    private static final String[] FULLNESS_NAMES = {
            "", "まだ少し食べられる", "ほどよく軽め", "ちょうどいい", "しっかり満足", "おなかいっぱい"
    };
    private static final String[] FULLNESS_FACES = {"", "◔", "◑", "●", "◕", "◎"};
    private static final String[] PORTION_NAMES = {"", "軽め", "普通", "多め"};

    private TextView fullnessValue;
    private TextView fullnessDescription;
    private TextView nextAlarmTitle;
    private TextView nextAlarmDetail;
    private TextView historyBody;
    private TextView learningText;
    private TextView hungerValue;
    private TextView hungerContextText;
    private SeekBar fullnessSeek;
    private SeekBar hungerSeek;
    private final Button[] portionButtons = new Button[4];
    private int selectedPortion = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.cream));
        window.setNavigationBarColor(getColor(R.color.cream));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            HungerAlarmReceiver.createChannel(this, notificationManager);
        }
        setContentView(buildContent());
        updateFullness(3);
        updatePortion(2);
        updateHungerRating(5);
        refreshDataViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nextAlarmTitle != null) refreshDataViews();
    }

    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(getColor(R.color.cream));

        LinearLayout root = vertical();
        root.setPadding(dp(22), dp(22), dp(22), dp(36));
        scroll.addView(root, matchWrap());

        TextView eyebrow = text("HARA × TIME", 12, R.color.orange, true);
        eyebrow.setLetterSpacing(0.18f);
        root.addView(eyebrow);

        TextView title = text("haradokei", 36, R.color.ink, true);
        LinearLayout.LayoutParams titleParams = wrapWrap();
        titleParams.topMargin = dp(2);
        root.addView(title, titleParams);

        TextView intro = text("気づきにくい空腹を、\n食事の記録からそっと予測します。", 16, R.color.muted, false);
        intro.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams introParams = wrapWrap();
        introParams.topMargin = dp(5);
        introParams.bottomMargin = dp(24);
        root.addView(intro, introParams);

        TextView profile = text(UserProfile.summary(), 12, R.color.muted, false);
        profile.setGravity(Gravity.CENTER);
        profile.setPadding(dp(12), dp(9), dp(12), dp(9));
        profile.setBackgroundColor(getColor(R.color.peach));
        LinearLayout.LayoutParams profileParams = matchWrap();
        profileParams.bottomMargin = dp(16);
        root.addView(profile, profileParams);

        LinearLayout nextCard = card();
        TextView nextLabel = text("NEXT HUNGER", 11, R.color.orange, true);
        nextLabel.setLetterSpacing(0.12f);
        nextCard.addView(nextLabel);
        nextAlarmTitle = text("まだ予測はありません", 24, R.color.ink, true);
        LinearLayout.LayoutParams nextTitleParams = wrapWrap();
        nextTitleParams.topMargin = dp(9);
        nextCard.addView(nextAlarmTitle, nextTitleParams);
        nextAlarmDetail = text("食事を記録すると、ここに通知予定が表示されます。", 14, R.color.muted, false);
        nextAlarmDetail.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams nextDetailParams = wrapWrap();
        nextDetailParams.topMargin = dp(6);
        nextCard.addView(nextAlarmDetail, nextDetailParams);
        root.addView(nextCard, cardParams(0));

        LinearLayout inputCard = card();
        TextView inputTitle = text("食後の満腹度は？", 21, R.color.ink, true);
        inputCard.addView(inputTitle);
        TextView hint = text("いまの感覚にいちばん近い数字を選んでください", 13, R.color.muted, false);
        LinearLayout.LayoutParams hintParams = wrapWrap();
        hintParams.topMargin = dp(4);
        inputCard.addView(hint, hintParams);

        fullnessValue = text("3  ●", 34, R.color.orange, true);
        fullnessValue.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams valueParams = matchWrap();
        valueParams.topMargin = dp(18);
        inputCard.addView(fullnessValue, valueParams);

        fullnessDescription = text(FULLNESS_NAMES[3], 15, R.color.ink, true);
        fullnessDescription.setGravity(Gravity.CENTER);
        inputCard.addView(fullnessDescription, matchWrap());

        fullnessSeek = new SeekBar(this);
        fullnessSeek.setMax(4);
        fullnessSeek.setProgress(2);
        fullnessSeek.setContentDescription("満腹度");
        fullnessSeek.setMin(0);
        fullnessSeek.setProgressTintList(getColorStateList(R.color.orange));
        fullnessSeek.setThumbTintList(getColorStateList(R.color.orange));
        fullnessSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateFullness(progress + 1);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        LinearLayout.LayoutParams seekParams = matchWrap();
        seekParams.topMargin = dp(9);
        inputCard.addView(fullnessSeek, seekParams);

        LinearLayout scale = new LinearLayout(this);
        scale.setOrientation(LinearLayout.HORIZONTAL);
        TextView low = text("1  軽い", 12, R.color.muted, false);
        TextView high = text("満腹  5", 12, R.color.muted, false);
        scale.addView(low, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        high.setGravity(Gravity.END);
        scale.addView(high, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        inputCard.addView(scale, matchWrap());

        TextView portionTitle = text("食事量", 15, R.color.ink, true);
        LinearLayout.LayoutParams portionTitleParams = wrapWrap();
        portionTitleParams.topMargin = dp(20);
        inputCard.addView(portionTitle, portionTitleParams);
        TextView portionHint = text("だいたいで大丈夫です", 12, R.color.muted, false);
        inputCard.addView(portionHint);

        LinearLayout portions = new LinearLayout(this);
        portions.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 1; i <= 3; i++) {
            final int portion = i;
            Button choice = new Button(this);
            choice.setText(PORTION_NAMES[i]);
            choice.setTextSize(14);
            choice.setAllCaps(false);
            choice.setOnClickListener(v -> updatePortion(portion));
            portionButtons[i] = choice;
            LinearLayout.LayoutParams choiceParams = new LinearLayout.LayoutParams(0, dp(48), 1);
            if (i > 1) choiceParams.leftMargin = dp(8);
            portions.addView(choice, choiceParams);
        }
        LinearLayout.LayoutParams portionsParams = matchWrap();
        portionsParams.topMargin = dp(9);
        inputCard.addView(portions, portionsParams);

        Button record = new Button(this);
        record.setText("食事を記録して予測する");
        record.setTextSize(16);
        record.setTextColor(Color.WHITE);
        record.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        record.setAllCaps(false);
        record.setBackgroundResource(R.drawable.button_background);
        record.setOnClickListener(v -> recordMeal());
        LinearLayout.LayoutParams recordParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        recordParams.topMargin = dp(18);
        inputCard.addView(record, recordParams);
        root.addView(inputCard, cardParams(16));

        LinearLayout hungerCard = card();
        TextView hungerTitle = text("今の空腹度", 21, R.color.ink, true);
        hungerCard.addView(hungerTitle);
        hungerContextText = text(getString(R.string.hunger_context_default), 13,
                R.color.muted, false);
        hungerContextText.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams hungerContextParams = matchWrap();
        hungerContextParams.topMargin = dp(4);
        hungerCard.addView(hungerContextText, hungerContextParams);

        hungerValue = text("5 / 10", 30, R.color.orange, true);
        hungerValue.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hungerValueParams = matchWrap();
        hungerValueParams.topMargin = dp(14);
        hungerCard.addView(hungerValue, hungerValueParams);

        hungerSeek = new SeekBar(this);
        hungerSeek.setMin(0);
        hungerSeek.setMax(10);
        hungerSeek.setProgress(5);
        hungerSeek.setContentDescription("現在の空腹度");
        hungerSeek.setProgressTintList(getColorStateList(R.color.orange));
        hungerSeek.setThumbTintList(getColorStateList(R.color.orange));
        hungerSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateHungerRating(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        hungerCard.addView(hungerSeek, matchWrap());

        LinearLayout hungerScale = new LinearLayout(this);
        hungerScale.setOrientation(LinearLayout.HORIZONTAL);
        TextView notHungry = text("0  空腹でない", 12, R.color.muted, false);
        TextView veryHungry = text("強い空腹  10", 12, R.color.muted, false);
        hungerScale.addView(notHungry, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        veryHungry.setGravity(Gravity.END);
        hungerScale.addView(veryHungry, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        hungerCard.addView(hungerScale, matchWrap());

        Button recordHunger = new Button(this);
        recordHunger.setText("現在の空腹度を記録する");
        recordHunger.setTextSize(15);
        recordHunger.setTextColor(Color.WHITE);
        recordHunger.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        recordHunger.setAllCaps(false);
        recordHunger.setBackgroundResource(R.drawable.button_background);
        recordHunger.setOnClickListener(v -> recordHungerCheck());
        LinearLayout.LayoutParams hungerButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        hungerButtonParams.topMargin = dp(14);
        hungerCard.addView(recordHunger, hungerButtonParams);
        root.addView(hungerCard, cardParams(16));

        LinearLayout historyCard = card();
        LinearLayout historyHeader = new LinearLayout(this);
        historyHeader.setOrientation(LinearLayout.HORIZONTAL);
        historyHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView historyTitle = text("最近の記録", 19, R.color.ink, true);
        historyHeader.addView(historyTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView undo = text("最後を取り消す", 13, R.color.orange, true);
        undo.setPadding(dp(8), dp(8), 0, dp(8));
        undo.setOnClickListener(v -> undoLast());
        historyHeader.addView(undo);
        historyCard.addView(historyHeader, matchWrap());

        historyBody = text("まだ記録はありません", 14, R.color.muted, false);
        historyBody.setLineSpacing(dp(7), 1f);
        LinearLayout.LayoutParams historyParams = matchWrap();
        historyParams.topMargin = dp(12);
        historyCard.addView(historyBody, historyParams);
        learningText = text("記録が増えるほど、あなたの食事間隔に予測を近づけます。", 12, R.color.muted, false);
        learningText.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams learningParams = matchWrap();
        learningParams.topMargin = dp(14);
        historyCard.addView(learningText, learningParams);
        root.addView(historyCard, cardParams(16));

        TextView privacy = text("記録はこの端末の中だけに保存されます", 12, R.color.muted, false);
        privacy.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams privacyParams = matchWrap();
        privacyParams.topMargin = dp(20);
        root.addView(privacy, privacyParams);
        return scroll;
    }

    private void recordMeal() {
        int fullness = fullnessSeek.getProgress() + 1;
        List<MealRecord> history = MealStore.load(this);
        int minutes = HungerPredictor.predictMinutes(fullness, selectedPortion, history);
        long now = System.currentTimeMillis();
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.cancel(1001);
        MealStore.add(this, new MealRecord(now, fullness, selectedPortion));
        MealStore.setPendingMealAt(this, 0L);
        AlarmScheduler.schedule(this, now + minutes * 60_000L, now, 0);
        requestNotificationPermissionIfNeeded();
        refreshDataViews();
        Toast.makeText(this, "記録しました。約" + humanDuration(minutes) + "後にお知らせします", Toast.LENGTH_LONG).show();
    }

    private void recordHungerCheck() {
        List<MealRecord> records = MealStore.load(this);
        if (records.isEmpty()) {
            Toast.makeText(this, "先に食事を記録してください", Toast.LENGTH_SHORT).show();
            return;
        }
        MealRecord last = records.get(records.size() - 1);
        int rating = hungerSeek.getProgress();
        long now = System.currentTimeMillis();
        MealStore.updateHungerCheck(this, last.eatenAtMillis, now, rating);
        if (rating >= 6) {
            MealStore.setPendingMealAt(this, 0L);
            AlarmScheduler.cancel(this);
            Toast.makeText(this, "空腹を感じた時刻として記録しました", Toast.LENGTH_SHORT).show();
        } else if (MealStore.pendingMealAt(this) == last.eatenAtMillis) {
            MealStore.setPendingMealAt(this, 0L);
            AlarmScheduler.schedule(this, now + 30L * 60L * 1000L, last.eatenAtMillis,
                    MealStore.alarmCheckCount(this) + 1);
            Toast.makeText(this, "30分後にもう一度お知らせします", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "現在の空腹度を記録しました", Toast.LENGTH_SHORT).show();
        }
        refreshDataViews();
    }

    private void undoLast() {
        if (!MealStore.removeLast(this)) {
            Toast.makeText(this, "取り消せる記録がありません", Toast.LENGTH_SHORT).show();
            return;
        }
        AlarmScheduler.cancel(this);
        List<MealRecord> records = MealStore.load(this);
        if (!records.isEmpty()) {
            MealRecord last = records.get(records.size() - 1);
            if (last.hungerAtMillis == 0L) {
                int minutes = HungerPredictor.predictMinutes(last.fullness, last.portion,
                        records.subList(0, records.size() - 1));
                long at = last.eatenAtMillis + minutes * 60_000L;
                if (at > System.currentTimeMillis()) {
                    AlarmScheduler.schedule(this, at, last.eatenAtMillis, 0);
                }
            }
        }
        refreshDataViews();
        Toast.makeText(this, "最後の記録を取り消しました", Toast.LENGTH_SHORT).show();
    }

    private void refreshDataViews() {
        List<MealRecord> records = MealStore.load(this);
        long alarmAt = MealStore.alarmAt(this);
        long pendingMealAt = MealStore.pendingMealAt(this);
        long now = System.currentTimeMillis();
        if (alarmAt > now) {
            nextAlarmTitle.setText(formatAlarm(alarmAt));
            int minutes = (int) Math.max(1, Math.round((alarmAt - now) / 60_000.0));
            nextAlarmDetail.setText(String.format(
                    Locale.JAPAN, "あと約%s。空腹になりそうな頃に通知します。", humanDuration(minutes)));
        } else {
            nextAlarmTitle.setText(records.isEmpty() ? "まだ予測はありません" : "次の食事を記録しましょう");
            nextAlarmDetail.setText(records.isEmpty()
                    ? "食事を記録すると、ここに通知予定が表示されます。"
                    : "通知後に食べたら、満腹度をまた教えてください。");
        }

        if (pendingMealAt != 0L) {
            hungerContextText.setText(R.string.hunger_context_pending);
        } else {
            hungerContextText.setText(R.string.hunger_context_default);
        }

        if (records.isEmpty()) {
            historyBody.setText("まだ記録はありません");
        } else {
            StringBuilder body = new StringBuilder();
            DateFormat format = new SimpleDateFormat("M/d  HH:mm", Locale.JAPAN);
            int first = Math.max(0, records.size() - 5);
            for (int i = records.size() - 1; i >= first; i--) {
                MealRecord record = records.get(i);
                if (body.length() > 0) body.append('\n');
                body.append(format.format(new Date(record.eatenAtMillis)))
                        .append("     満腹度 ").append(record.fullness)
                        .append("  ").append(FULLNESS_FACES[record.fullness])
                        .append(" / ").append(PORTION_NAMES[record.portion]);
                if (record.hungerAtMillis > 0L) {
                    body.append("\n    ↳ 空腹 ").append(format.format(new Date(record.hungerAtMillis)))
                            .append("  (").append(record.lastHungerRating).append("/10)");
                } else if (record.lastHungerRating >= 0) {
                    body.append("\n    ↳ 空腹度 ").append(record.lastHungerRating).append("/10");
                }
            }
            historyBody.setText(body.toString());
        }
        int hungerSamples = 0;
        for (MealRecord record : records) if (record.hungerAtMillis > 0L) hungerSamples++;
        learningText.setText(hungerSamples == 0
                ? "空腹度の回答が増えるほど、実際の感覚に予測を近づけます。"
                : "学習済みの空腹タイミング: " + hungerSamples + "件（端末内でのみ計算）");
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    private void updateFullness(int fullness) {
        if (fullnessValue == null) return;
        fullnessValue.setText(String.format(Locale.JAPAN, "%d  %s", fullness, FULLNESS_FACES[fullness]));
        fullnessDescription.setText(FULLNESS_NAMES[fullness]);
    }

    private void updatePortion(int portion) {
        selectedPortion = portion;
        for (int i = 1; i <= 3; i++) {
            Button button = portionButtons[i];
            if (button == null) continue;
            boolean selected = i == portion;
            button.setBackgroundResource(selected
                    ? R.drawable.choice_selected : R.drawable.choice_unselected);
            button.setTextColor(getColor(selected ? R.color.orange_dark : R.color.muted));
            button.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void updateHungerRating(int rating) {
        if (hungerValue == null) return;
        hungerValue.setText(String.format(Locale.JAPAN, "%d / 10", rating));
    }

    private String formatAlarm(long millis) {
        SimpleDateFormat day = new SimpleDateFormat("M月d日(E)  H:mm", Locale.JAPAN);
        return day.format(new Date(millis));
    }

    private static String humanDuration(int minutes) {
        int hours = minutes / 60;
        int rest = minutes % 60;
        if (hours == 0) return rest + "分";
        if (rest == 0) return hours + "時間";
        return hours + "時間" + rest + "分";
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout card() {
        LinearLayout card = vertical();
        card.setPadding(dp(22), dp(20), dp(22), dp(20));
        card.setBackgroundResource(R.drawable.card_background);
        card.setElevation(dp(2));
        return card;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(getColor(color));
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private LinearLayout.LayoutParams cardParams(int topMargin) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(topMargin);
        return params;
    }

    private static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private static LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
