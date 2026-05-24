package com.example.app_prestamos;

import android.os.Bundle;
import android.view.ViewGroup;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    private TextView todaySummaryText;
    private TextView monthTitleText;
    private TextView calendarText;
    private LinearLayout collectionsContainer;

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
        calendarText = findViewById(R.id.calendarText);
        collectionsContainer = findViewById(R.id.collectionsContainer);

        monthTitleText.setText(DateUtils.currentMonthTitle());
        calendarText.setText(DateUtils.buildCurrentMonthCalendarText());
        loadTodayCollections();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    private void loadTodayCollections() {
        databaseExecutor.execute(() -> {
            long todayEpochDay = DateUtils.todayEpochDay();
            AppDatabase database = AppDatabase.getInstance(this);
            List<CollectionDueItem> collections = database.scheduledPaymentDao()
                    .getCollectionsDueUntil(todayEpochDay);

            runOnUiThread(() -> renderTodayCollections(collections));
        });
    }

    private void renderTodayCollections(List<CollectionDueItem> collections) {
        collectionsContainer.removeAllViews();

        if (collections.isEmpty()) {
            todaySummaryText.setText("Hoy no tienes cobros pendientes.");
            collectionsContainer.addView(buildCollectionText("No hay clientes para cobrar hoy."));
            return;
        }

        Map<String, Long> totalsByCurrency = new LinkedHashMap<>();
        for (CollectionDueItem item : collections) {
            Long currentTotal = totalsByCurrency.get(item.currencyCode);
            totalsByCurrency.put(item.currencyCode, (currentTotal == null ? 0 : currentTotal) + item.amountCents);
            collectionsContainer.addView(buildCollectionText(formatCollectionItem(item)));
        }

        todaySummaryText.setText(
                "Tienes " + collections.size() + " cobro(s) pendiente(s) por " + formatTotalsByCurrency(totalsByCurrency)
        );
    }

    private TextView buildCollectionText(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setPadding(0, 12, 0, 12);
        textView.setText(text);
        return textView;
    }

    private String formatCollectionItem(CollectionDueItem item) {
        String fullName = item.firstName + " " + item.lastName;
        return fullName.trim() +
                "\nCuota " + item.installmentNumber + "/" + item.installmentCount +
                " - " + MoneyUtils.formatCents(item.amountCents, item.currencyCode) +
                "\nVence: " + DateUtils.formatEpochDay(item.dueDateEpochDay);
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
}