package com.example.app_prestamos;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app_prestamos.data.local.AppDatabase;
import com.example.app_prestamos.data.local.entity.ClientEntity;
import com.example.app_prestamos.data.local.entity.CurrencyType;
import com.example.app_prestamos.data.local.entity.LoanEntity;
import com.example.app_prestamos.data.local.entity.PaymentFrequency;
import com.example.app_prestamos.data.local.entity.ScheduledPaymentEntity;
import com.example.app_prestamos.domain.PaymentScheduleCalculator;
import com.example.app_prestamos.util.DateUtils;
import com.example.app_prestamos.util.MoneyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateLoanActivity extends AppCompatActivity {

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    private Spinner clientSpinner;
    private Spinner currencySpinner;
    private Spinner frequencySpinner;
    private EditText principalAmountInput;
    private EditText profitPercentInput;
    private EditText installmentCountInput;
    private EditText notesInput;
    private List<ClientOption> clientOptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_loan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clientSpinner = findViewById(R.id.clientSpinner);
        currencySpinner = findViewById(R.id.currencySpinner);
        frequencySpinner = findViewById(R.id.frequencySpinner);
        principalAmountInput = findViewById(R.id.principalAmountInput);
        profitPercentInput = findViewById(R.id.profitPercentInput);
        installmentCountInput = findViewById(R.id.installmentCountInput);
        notesInput = findViewById(R.id.loanNotesInput);

        setupStaticSpinners();
        loadClients();
        findViewById(R.id.saveLoanButton).setOnClickListener(view -> saveLoan());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    private void setupStaticSpinners() {
        ArrayAdapter<OptionItem> currencyAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner,
                buildCurrencyOptions()
        );
        currencyAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        currencySpinner.setAdapter(currencyAdapter);

        ArrayAdapter<OptionItem> frequencyAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner,
                buildFrequencyOptions()
        );
        frequencyAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        frequencySpinner.setAdapter(frequencyAdapter);
    }

    private void loadClients() {
        databaseExecutor.execute(() -> {
            List<ClientEntity> clients = AppDatabase.getInstance(this).clientDao().getActiveClients();
            List<ClientOption> options = new ArrayList<>();
            options.add(new ClientOption(0, "Selecciona un cliente"));
            for (ClientEntity client : clients) {
                options.add(new ClientOption(client.id, client.firstName + " " + client.lastName));
            }

            runOnUiThread(() -> {
                clientOptions = options;
                ArrayAdapter<ClientOption> adapter = new ArrayAdapter<>(
                        this,
                        R.layout.item_spinner,
                        clientOptions
                );
                adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
                clientSpinner.setAdapter(adapter);
                if (clients.isEmpty()) {
                    Toast.makeText(this, "Primero registra un cliente.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void saveLoan() {
        if (clientOptions.isEmpty()) {
            Toast.makeText(this, "No hay clientes disponibles para crear el prestamo.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            ClientOption selectedClient = (ClientOption) clientSpinner.getSelectedItem();
            OptionItem selectedCurrency = (OptionItem) currencySpinner.getSelectedItem();
            OptionItem selectedFrequency = (OptionItem) frequencySpinner.getSelectedItem();

            if (selectedClient == null || selectedClient.clientId <= 0) {
                Toast.makeText(this, "Selecciona un cliente.", Toast.LENGTH_LONG).show();
                return;
            }
            if (selectedCurrency == null || selectedCurrency.value.isEmpty()) {
                Toast.makeText(this, "Selecciona una moneda.", Toast.LENGTH_LONG).show();
                return;
            }
            if (selectedFrequency == null || selectedFrequency.value.isEmpty()) {
                Toast.makeText(this, "Selecciona una frecuencia de pago.", Toast.LENGTH_LONG).show();
                return;
            }

            long principalCents = MoneyUtils.parseToCents(readText(principalAmountInput));
            double profitPercent = Double.parseDouble(readText(profitPercentInput).replace(",", "."));
            int installmentCount = Integer.parseInt(readText(installmentCountInput));

            if (profitPercent < 0) {
                profitPercentInput.setError("La ganancia no puede ser negativa");
                return;
            }
            if (installmentCount <= 0) {
                installmentCountInput.setError("Debe ser mayor que cero");
                return;
            }

            long now = System.currentTimeMillis();
            long startDate = DateUtils.todayEpochDay();
            long profitCents = MoneyUtils.calculateProfitCents(principalCents, profitPercent);
            long totalCents = principalCents + profitCents;

            LoanEntity loan = new LoanEntity();
            loan.clientId = selectedClient.clientId;
            loan.currencyCode = selectedCurrency.value;
            loan.principalAmountCents = principalCents;
            loan.profitPercent = profitPercent;
            loan.profitAmountCents = profitCents;
            loan.totalReceivableCents = totalCents;
            loan.installmentCount = installmentCount;
            loan.installmentAmountCents = totalCents / installmentCount;
            loan.paymentFrequency = selectedFrequency.value;
            loan.startDateEpochDay = startDate;
            loan.expectedEndDateEpochDay = DateUtils.addFrequency(startDate, selectedFrequency.value, installmentCount);
            loan.notes = readText(notesInput);
            loan.createdAtMillis = now;
            loan.updatedAtMillis = now;

            databaseExecutor.execute(() -> saveLoanWithPayments(loan, installmentCount, now));
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Revisa los numeros del prestamo.", Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveLoanWithPayments(LoanEntity loan, int installmentCount, long now) {
        AppDatabase database = AppDatabase.getInstance(this);
        long loanId = database.loanDao().insert(loan);

        List<ScheduledPaymentEntity> payments = PaymentScheduleCalculator.buildScheduledPayments(
                loanId,
                loan.startDateEpochDay,
                loan.totalReceivableCents,
                installmentCount,
                loan.paymentFrequency,
                now
        );
        database.scheduledPaymentDao().insertOrReplaceAll(payments);

        runOnUiThread(() -> {
            Toast.makeText(this, "Prestamo guardado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private List<OptionItem> buildCurrencyOptions() {
        List<OptionItem> options = new ArrayList<>();
        options.add(new OptionItem("", "Selecciona una moneda"));
        options.add(new OptionItem(CurrencyType.PEN, "Soles peruanos"));
        options.add(new OptionItem(CurrencyType.MXN, "Pesos mexicanos"));
        options.add(new OptionItem(CurrencyType.COP, "Pesos colombianos"));
        options.add(new OptionItem(CurrencyType.VES, "Bolivares venezolanos"));
        options.add(new OptionItem(CurrencyType.USD, "Dolares"));
        options.add(new OptionItem(CurrencyType.EUR, "Euros"));
        return options;
    }

    private List<OptionItem> buildFrequencyOptions() {
        List<OptionItem> options = new ArrayList<>();
        options.add(new OptionItem("", "Selecciona una frecuencia"));
        options.add(new OptionItem(PaymentFrequency.DAILY, "Diario"));
        options.add(new OptionItem(PaymentFrequency.BIWEEKLY, "Quincenal"));
        options.add(new OptionItem(PaymentFrequency.MONTHLY, "Mensual"));
        return options;
    }

    private String readText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private static class ClientOption {
        final long clientId;
        final String label;

        ClientOption(long clientId, String label) {
            this.clientId = clientId;
            this.label = label;
        }

        @Override
        public String toString() {
            return label.trim().isEmpty() ? "Cliente sin nombre" : label;
        }
    }

    private static class OptionItem {
        final String value;
        final String label;

        OptionItem(String value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
