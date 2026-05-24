package com.example.app_prestamos;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app_prestamos.data.local.AppDatabase;
import com.example.app_prestamos.data.local.entity.ClientEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateClientActivity extends AppCompatActivity {

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText phoneInput;
    private EditText addressInput;
    private EditText notesInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_client);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        phoneInput = findViewById(R.id.phoneInput);
        addressInput = findViewById(R.id.addressInput);
        notesInput = findViewById(R.id.clientNotesInput);

        findViewById(R.id.saveClientButton).setOnClickListener(view -> saveClient());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    private void saveClient() {
        String firstName = readText(firstNameInput);
        if (firstName.isEmpty()) {
            firstNameInput.setError("El nombre es obligatorio");
            return;
        }

        long now = System.currentTimeMillis();
        ClientEntity client = new ClientEntity();
        client.firstName = firstName;
        client.lastName = readText(lastNameInput);
        client.phone = readText(phoneInput);
        client.address = readText(addressInput);
        client.notes = readText(notesInput);
        client.createdAtMillis = now;
        client.updatedAtMillis = now;

        databaseExecutor.execute(() -> {
            AppDatabase.getInstance(this).clientDao().insert(client);
            runOnUiThread(() -> {
                Toast.makeText(this, "Cliente guardado", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private String readText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
