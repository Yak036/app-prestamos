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
import com.example.app_prestamos.data.local.entity.ClientEntity;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientListActivity extends AppCompatActivity {

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private LinearLayout clientsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_clients);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clientsContainer = findViewById(R.id.clientsContainer);
        findViewById(R.id.createClientButton).setOnClickListener(view ->
                startActivity(new Intent(this, CreateClientActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClients();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    private void loadClients() {
        databaseExecutor.execute(() -> {
            List<ClientEntity> clients = AppDatabase.getInstance(this).clientDao().getActiveClients();
            runOnUiThread(() -> renderClients(clients));
        });
    }

    private void renderClients(List<ClientEntity> clients) {
        clientsContainer.removeAllViews();

        if (clients.isEmpty()) {
            clientsContainer.addView(buildSimpleText("Todavia no tienes clientes registrados."));
            return;
        }

        for (ClientEntity client : clients) {
            clientsContainer.addView(buildClientCard(client));
        }
    }

    private MaterialCardView buildClientCard(ClientEntity client) {
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

        TextView nameText = buildTitleText(client.firstName + " " + client.lastName);
        TextView phoneText = buildDetailText("Telefono: " + safeText(client.phone));
        TextView addressText = buildDetailText("Direccion: " + safeText(client.address));

        content.addView(nameText);
        content.addView(phoneText);
        content.addView(addressText);
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

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "Sin registrar" : value;
    }
}
