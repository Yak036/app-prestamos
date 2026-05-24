package com.example.app_prestamos;

import android.content.Intent;
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
import com.example.app_prestamos.data.local.model.LoanListItem;
import com.example.app_prestamos.util.MoneyUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoanListActivity extends AppCompatActivity {

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private LinearLayout loansContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loans);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loansContainer = findViewById(R.id.loansContainer);
        findViewById(R.id.createLoanButton).setOnClickListener(view ->
                startActivity(new Intent(this, CreateLoanActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLoans();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    private void loadLoans() {
        databaseExecutor.execute(() -> {
            List<LoanListItem> loans = AppDatabase.getInstance(this).loanDao().getActiveLoanListItems();
            runOnUiThread(() -> renderLoans(loans));
        });
    }

    private void renderLoans(List<LoanListItem> loans) {
        loansContainer.removeAllViews();

        if (loans.isEmpty()) {
            loansContainer.addView(buildSimpleText("Todavia no tienes prestamos activos."));
            return;
        }

        for (LoanListItem loan : loans) {
            loansContainer.addView(buildLoanCard(loan));
        }
    }

    private MaterialCardView buildLoanCard(LoanListItem loan) {
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

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(16), dp(18), dp(16));

        content.addView(buildTitleText(loan.firstName + " " + loan.lastName));
        content.addView(buildDetailText("Prestado: " + MoneyUtils.formatCents(loan.principalAmountCents, loan.currencyCode)));
        content.addView(buildDetailText("Total a cobrar: " + MoneyUtils.formatCents(loan.totalReceivableCents, loan.currencyCode)));
        content.addView(buildDetailText("Ganancia: " + loan.profitPercent + "% | Cuotas: " + loan.installmentCount));
        content.addView(buildDetailText("Frecuencia: " + formatFrequency(loan.paymentFrequency)));

        card.addView(content);
        return card;
    }

    private TextView buildSimpleText(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setText(text);
        textView.setTextSize(16);
        return textView;
    }

    private TextView buildTitleText(String text) {
        TextView textView = buildSimpleText(text.trim().isEmpty() ? "Cliente sin nombre" : text.trim());
        textView.setTextColor(getColor(R.color.app_text_primary));
        textView.setTextSize(18);
        textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD);
        return textView;
    }

    private TextView buildDetailText(String text) {
        TextView textView = buildSimpleText(text);
        textView.setTextColor(getColor(R.color.app_text_secondary));
        textView.setPadding(0, dp(4), 0, 0);
        return textView;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String formatFrequency(String frequency) {
        if ("MONTHLY".equals(frequency)) {
            return "Mensual";
        }
        if ("BIWEEKLY".equals(frequency)) {
            return "Quincenal";
        }
        return "Diario";
    }
}
