package com.example.app_prestamos;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app_prestamos.data.local.AppDatabase;
import com.example.app_prestamos.data.local.model.CollectionDueItem;
import com.example.app_prestamos.util.DateUtils;
import com.example.app_prestamos.util.MoneyUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    private TextView todaySummaryText;
    private TextView monthTitleText;
    private TextView collectionsTitleText;
    private GridLayout calendarGrid;
    private LinearLayout collectionsContainer;
    private Calendar visibleMonthCalendar;
    private long selectedEpochDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        todaySummaryText = findViewById(R.id.todaySummaryText);
        monthTitleText = findViewById(R.id.monthTitleText);
        collectionsTitleText = findViewById(R.id.collectionsTitleText);
        calendarGrid = findViewById(R.id.calendarGrid);
        collectionsContainer = findViewById(R.id.collectionsContainer);

        selectedEpochDay = DateUtils.todayEpochDay();
        visibleMonthCalendar = Calendar.getInstance();
        visibleMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);

        findViewById(R.id.previousMonthButton).setOnClickListener(view -> changeVisibleMonth(-1));
        findViewById(R.id.nextMonthButton).setOnClickListener(view -> changeVisibleMonth(1));
        findViewById(R.id.openClientsButton).setOnClickListener(view ->
                startActivity(new Intent(this, ClientListActivity.class))
        );
        findViewById(R.id.openLoansButton).setOnClickListener(view ->
                startActivity(new Intent(this, LoanListActivity.class))
        );
        renderCalendar();
        loadCollectionsForSelectedDate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    private void loadCollectionsForSelectedDate() {
        long dateToLoad = selectedEpochDay;
        databaseExecutor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(this);
            List<CollectionDueItem> collections = database.scheduledPaymentDao()
                    .getCollectionsDueOn(dateToLoad);

            runOnUiThread(() -> renderTodayCollections(dateToLoad, collections));
        });
    }

    private void changeVisibleMonth(int amount) {
        visibleMonthCalendar.add(Calendar.MONTH, amount);
        renderCalendar();
    }

    private void renderCalendar() {
        calendarGrid.removeAllViews();

        int year = visibleMonthCalendar.get(Calendar.YEAR);
        int month = visibleMonthCalendar.get(Calendar.MONTH);
        monthTitleText.setText(DateUtils.monthTitleFor(year, month));

        String[] weekDays = {"L", "M", "X", "J", "V", "S", "D"};
        for (String weekDay : weekDays) {
            calendarGrid.addView(buildCalendarLabel(weekDay));
        }

        Calendar monthCursor = (Calendar) visibleMonthCalendar.clone();
        monthCursor.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = monthCursor.get(Calendar.DAY_OF_WEEK);
        int leadingEmptyDays = firstDayOfWeek == Calendar.SUNDAY ? 6 : firstDayOfWeek - Calendar.MONDAY;

        for (int i = 0; i < leadingEmptyDays; i++) {
            calendarGrid.addView(buildCalendarEmptyCell());
        }

        int daysInMonth = monthCursor.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= daysInMonth; day++) {
            calendarGrid.addView(buildCalendarDayCell(year, month, day));
        }
    }

    private TextView buildCalendarLabel(String text) {
        TextView textView = buildCalendarCell(text);
        textView.setTextColor(getColor(R.color.app_calendar_muted));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        return textView;
    }

    private TextView buildCalendarEmptyCell() {
        TextView textView = buildCalendarCell("");
        textView.setEnabled(false);
        return textView;
    }

    private TextView buildCalendarDayCell(int year, int month, int day) {
        TextView textView = buildCalendarCell(String.valueOf(day));
        long dayEpoch = DateUtils.epochDayFor(year, month, day);
        boolean isSelected = dayEpoch == selectedEpochDay;
        boolean isToday = dayEpoch == DateUtils.todayEpochDay();

        textView.setTextColor(isSelected ? Color.WHITE : getColor(R.color.app_text_primary));
        textView.setTypeface(textView.getTypeface(), isSelected || isToday ? Typeface.BOLD : Typeface.NORMAL);
        textView.setBackground(buildDayBackground(isSelected, isToday));
        textView.setOnClickListener(view -> {
            selectedEpochDay = dayEpoch;
            renderCalendar();
            loadCollectionsForSelectedDate();
        });
        return textView;
    }

    private TextView buildCalendarCell(String text) {
        TextView textView = new TextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(44);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(2), dp(3), dp(2), dp(3));
        textView.setLayoutParams(params);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setText(text);
        textView.setTextSize(15);
        return textView;
    }

    private GradientDrawable buildDayBackground(boolean isSelected, boolean isToday) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(14));

        if (isSelected) {
            drawable.setColor(getColor(R.color.app_primary));
            return drawable;
        }

        drawable.setColor(isToday ? getColor(R.color.app_primary_light) : Color.TRANSPARENT);
        if (isToday) {
            drawable.setStroke(dp(1), getColor(R.color.app_primary));
        }
        return drawable;
    }

    private void renderTodayCollections(long epochDay, List<CollectionDueItem> collections) {
        collectionsContainer.removeAllViews();
        String selectedDateText = DateUtils.formatEpochDay(epochDay);
        collectionsTitleText.setText("Cobros del " + selectedDateText);

        if (collections.isEmpty()) {
            todaySummaryText.setText("No tienes cobros programados para el " + selectedDateText + ".");
            collectionsContainer.addView(buildEmptyStateCard("Selecciona otro dia en el calendario para revisar sus cobros."));
            return;
        }

        Map<String, Long> totalsByCurrency = new LinkedHashMap<>();
        for (CollectionDueItem item : collections) {
            Long currentTotal = totalsByCurrency.get(item.currencyCode);
            totalsByCurrency.put(item.currencyCode, (currentTotal == null ? 0 : currentTotal) + item.amountCents);
            collectionsContainer.addView(buildCollectionCard(item));
        }

        todaySummaryText.setText(
                "Para el " + selectedDateText + " tienes " + collections.size() +
                        " cobro(s) pendiente(s) por " + formatTotalsByCurrency(totalsByCurrency)
        );
    }

    private MaterialCardView buildCollectionCard(CollectionDueItem item) {
        MaterialCardView card = buildBaseCard();
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(16), dp(18), dp(16));

        String fullName = (item.firstName + " " + item.lastName).trim();
        content.addView(buildCardTitle(fullName.isEmpty() ? "Cliente sin nombre" : fullName));
        content.addView(buildCardDetail("Cuota " + item.installmentNumber + "/" + item.installmentCount));
        content.addView(buildAmountText(MoneyUtils.formatCents(item.amountCents, item.currencyCode)));
        content.addView(buildCardDetail("Vence: " + DateUtils.formatEpochDay(item.dueDateEpochDay)));

        card.addView(content);
        return card;
    }

    private MaterialCardView buildEmptyStateCard(String text) {
        MaterialCardView card = buildBaseCard();
        TextView textView = buildCardDetail(text);
        textView.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.addView(textView);
        return card;
    }

    private MaterialCardView buildBaseCard() {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(getColor(R.color.app_card));
        card.setCardElevation(dp(1));
        card.setRadius(dp(18));
        card.setStrokeColor(getColor(R.color.app_card_stroke));
        card.setStrokeWidth(dp(1));
        return card;
    }

    private TextView buildCardTitle(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setText(text);
        textView.setTextColor(getColor(R.color.app_text_primary));
        textView.setTextSize(18);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        return textView;
    }

    private TextView buildCardDetail(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setPadding(0, dp(4), 0, 0);
        textView.setText(text);
        textView.setTextColor(getColor(R.color.app_text_secondary));
        textView.setTextSize(16);
        return textView;
    }

    private TextView buildAmountText(String text) {
        TextView textView = buildCardDetail(text);
        textView.setTextColor(getColor(R.color.app_primary_dark));
        textView.setTextSize(20);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        return textView;
    }

    private String formatTotalsByCurrency(Map<String, Long> totalsByCurrency) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Long> entry : totalsByCurrency.entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(MoneyUtils.formatCents(entry.getValue(), entry.getKey()));
        }
        return builder.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}