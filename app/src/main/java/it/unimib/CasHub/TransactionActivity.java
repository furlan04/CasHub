package it.unimib.CasHub;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TransactionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);  // <-- il tuo XML

        // -------------------------
        // 1) Collegamento elementi UI
        // -------------------------
        Button btnEntrata = findViewById(R.id.btnEntrata);
        Button btnUscita = findViewById(R.id.btnUscita);
        EditText etNome = findViewById(R.id.etNome);
        EditText etQuantita = findViewById(R.id.etQuantita);
        Spinner spinnerValuta = findViewById(R.id.spinnerValuta);

        // -------------------------
        // 2) Spinner Valute
        // -------------------------
        String[] valute = {"€", "$", "£", "CHF", "¥", "C$"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                valute
        );
        spinnerValuta.setAdapter(adapter);

        // -------------------------
        // 3) Logica bottoni Entrata / Uscita
        // -------------------------
        btnEntrata.setOnClickListener(v -> {
            btnEntrata.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
            btnEntrata.setTextColor(Color.WHITE);

            btnUscita.setBackgroundColor(Color.parseColor("#DDDDDD"));
            btnUscita.setTextColor(Color.BLACK);
        });

        btnUscita.setOnClickListener(v -> {
            btnUscita.setBackgroundColor(Color.parseColor("#F44336")); // Rosso
            btnUscita.setTextColor(Color.WHITE);

            btnEntrata.setBackgroundColor(Color.parseColor("#DDDDDD"));
            btnEntrata.setTextColor(Color.BLACK);
        });
    }
}