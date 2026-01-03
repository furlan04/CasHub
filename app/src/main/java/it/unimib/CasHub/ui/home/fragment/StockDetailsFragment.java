package it.unimib.CasHub.ui.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.StockQuote;
import it.unimib.CasHub.repository.PortfolioFirebaseRepository;
import it.unimib.CasHub.repository.StockAPIRepository;
import it.unimib.CasHub.utils.NetworkUtil;
import it.unimib.CasHub.utils.StockCache;
import it.unimib.CasHub.utils.StockResponseCallback;

public class StockDetailsFragment extends Fragment implements StockResponseCallback {

    private static final String TAG = StockDetailsFragment.class.getSimpleName();

    private TextView stockNameTextView;
    private TextView exchangeFullTextView;
    private TextView symbolTextView;
    private TextView currencyTextView;
    private TextView exchangeTextView;
    private Button addToPortfolioButton;
    private LineChart weeklyChart;

    private StockAPIRepository stockRepository;
    private PortfolioFirebaseRepository portfolioRepository;

    // Dati ricevuti dal Bundle
    private String symbol;
    private String companyName;
    private String currency;
    private String exchange;
    private String exchangeFull;

    private StockQuote currentStockQuote;

    public StockDetailsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ottieni tutti i dati dal Bundle
        if (getArguments() != null) {
            symbol = getArguments().getString("agencySymbol");
            companyName = getArguments().getString("agencyName");
            currency = getArguments().getString("agencyCurrency");
            exchange = getArguments().getString("agencyExchange");
            exchangeFull = getArguments().getString("agencyExchangeFull");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stock_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inizializza le view
        stockNameTextView = view.findViewById(R.id.StockName);
        exchangeFullTextView = view.findViewById(R.id.textExchangeFull);
        symbolTextView = view.findViewById(R.id.textSymbol);
        currencyTextView = view.findViewById(R.id.textCurrency);
        exchangeTextView = view.findViewById(R.id.textExchange);
        addToPortfolioButton = view.findViewById(R.id.btnAddToPortfolio);
        weeklyChart = view.findViewById(R.id.weeklyChart);

        // Mostra subito le info base dell'Agency
        if (companyName != null) {
            stockNameTextView.setText(companyName);
        }
        if (symbol != null) {
            symbolTextView.setText("Symbol: " + symbol);
        }
        if (currency != null) {
            currencyTextView.setText("\nCurrency: " + currency);
        }
        if (exchange != null) {
            exchangeTextView.setText("\nExchange: " + exchange);
        }

        // Inizializza repository
        if (requireActivity().getResources().getBoolean(R.bool.debug)) {
            stockRepository = StockAPIRepository.getInstance(requireActivity().getApplication());
        } else {
            stockRepository = StockAPIRepository.getInstance(requireActivity().getApplication());
        }

        portfolioRepository = new PortfolioFirebaseRepository();

        // Setup button
        addToPortfolioButton.setOnClickListener(v -> addToPortfolio());

        // Configura il grafico
        setupChart();

        // Carica i dati del prezzo e del grafico
        if (symbol != null && !symbol.isEmpty()) {
            loadStockData();
            loadChartData();
        } else {
            Toast.makeText(requireContext(), "Simbolo non valido", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupChart() {
        // Configurazione estetica del grafico
        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.setTouchEnabled(true);
        weeklyChart.setDragEnabled(true);
        weeklyChart.setScaleEnabled(false);
        weeklyChart.setPinchZoom(false);
        weeklyChart.setDrawGridBackground(false);
        weeklyChart.setBackgroundColor(Color.parseColor("#1E1E1E"));


        // Asse X (date)
        XAxis xAxis = weeklyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#B0B0B0"));
        xAxis.setTextSize(10f);

        // Asse Y sinistro
        YAxis leftAxis = weeklyChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#2E2E2E"));
        leftAxis.setTextColor(Color.parseColor("#B0B0B0"));
        leftAxis.setTextSize(10f);

        // Asse Y destro (disabilitato)
        weeklyChart.getAxisRight().setEnabled(false);

        // Legend
        weeklyChart.getLegend().setEnabled(false);

        // Extra padding
        weeklyChart.setExtraBottomOffset(10f);
    }


    private void loadStockData() {
        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Nessuna connessione internet", Toast.LENGTH_SHORT).show();
            return;
        }

        addToPortfolioButton.setEnabled(false);
        stockRepository.getStockQuote(symbol, this); // Passa "this" come callback
    }

    private void loadChartData() {
        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            return;
        }

        stockRepository.getWeeklyChart(symbol, new StockAPIRepository.ChartCallback() {
            @Override
            public void onSuccess(ChartData chartData) {
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        populateChart(chartData);
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Errore caricamento grafico: " + errorMessage);
                        Toast.makeText(requireContext(), "Impossibile caricare il grafico", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void populateChart(ChartData chartData) {
        List<Entry> entries = new ArrayList<>();
        List<String> dates = chartData.getDates();
        List<Float> prices = chartData.getPrices();

        // Crea gli entry per il grafico
        for (int i = 0; i < prices.size(); i++) {
            entries.add(new Entry(i, prices.get(i)));
        }

        // Calcola se il trend è positivo o negativo
        boolean isPositiveTrend = prices.get(prices.size() - 1) >= prices.get(0);
        int lineColor = isPositiveTrend ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"); // Verde o Rosso

        // Crea il dataset con stile moderno
        LineDataSet dataSet = new LineDataSet(entries, "Prezzo");
        dataSet.setColor(lineColor);
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(false); // Rimuove i punti
        dataSet.setDrawValues(false); // Rimuove i valori sui punti
        dataSet.setDrawFilled(true); // Abilita il riempimento
        dataSet.setFillColor(lineColor);
        dataSet.setFillAlpha(30); // Trasparenza del riempimento
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Linea curva smooth
        dataSet.setCubicIntensity(0.2f); // Intensità della curva (più basso = più smooth)
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setHighLightColor(lineColor);

        // Crea LineData e assegnalo al grafico
        LineData lineData = new LineData(dataSet);
        weeklyChart.setData(lineData);

        // Formatta le date sull'asse X (mostra solo giorno-mese, es: "23/12")
        List<String> formattedDates = new ArrayList<>();
        for (String date : dates) {
            // date formato: "2025-12-23" -> mostra "23/12"
            String[] parts = date.split("-");
            if (parts.length == 3) {
                formattedDates.add(parts[2] + "/" + parts[1]);
            } else {
                formattedDates.add(date);
            }
        }

        XAxis xAxis = weeklyChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(formattedDates));
        xAxis.setLabelCount(formattedDates.size());
        xAxis.setDrawGridLines(false); // Rimuove le linee verticali

        // Aggiorna il grafico con animazione
        weeklyChart.animateX(800); // Animazione di 800ms
    }


    @Override
    public void onSuccess(StockQuote stockQuote) {
        if (getActivity() == null) return;

        this.currentStockQuote = stockQuote;

        requireActivity().runOnUiThread(() -> {
            addToPortfolioButton.setEnabled(true);

            // Ottieni il simbolo della valuta corretto
            String currencySymbol = getCurrencySymbol(currency);

            // Mostra prezzo e variazione giornaliera con simbolo valuta dinamico
            String price = stockQuote.getPrice();
            String change = stockQuote.getChange();
            String changePercent = stockQuote.getChangePercent();

            String priceInfo = "Prezzo: " + currencySymbol + price +
                    "\n\nVariazione giornaliera: " + currencySymbol + change +
                    " (" + changePercent + ")";

            exchangeFullTextView.setText(priceInfo);
        });
    }

    @Override
    public void onFailure(String errorMessage) {
        if (getActivity() == null) return;

        requireActivity().runOnUiThread(() -> {
            addToPortfolioButton.setEnabled(true);
            Toast.makeText(requireContext(), "Errore: " + errorMessage, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Errore caricamento dati: " + errorMessage);
        });
    }

    private void addToPortfolio() {
        if (currentStockQuote == null) {
            Toast.makeText(requireContext(), "Dati non ancora caricati", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "ERRORE: Non sei loggato!", Toast.LENGTH_LONG).show();
            return;
        }


        showBuyStockDialog();
    }

    private void showBuyStockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Compra " + companyName);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView priceInfo = new TextView(requireContext());
        priceInfo.setText("Prezzo attuale: " + getCurrencySymbol(currency) + currentStockQuote.getPrice());
        priceInfo.setTextSize(16);
        priceInfo.setPadding(0, 0, 0, 20);
        layout.addView(priceInfo);

        final EditText quantityInput = new EditText(requireContext());
        quantityInput.setHint("Quantità");
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(quantityInput);

        builder.setView(layout);


        builder.setPositiveButton("CONFERMA", null);
        builder.setNegativeButton("ANNULLA", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String quantityStr = quantityInput.getText().toString();

            Log.d(TAG, "=== CONFERMA CLICKED ===");
            Log.d(TAG, "Quantity input: " + quantityStr);

            if (quantityStr.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci una quantità", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double quantity = Double.parseDouble(quantityStr);
                Log.d(TAG, "Quantity parsed: " + quantity);

                if (quantity <= 0) {
                    Toast.makeText(requireContext(), "Quantità deve essere > 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                savePurchase(quantity);

            } catch (NumberFormatException e) {
                Log.e(TAG, "Parse error: " + e.getMessage());
                Toast.makeText(requireContext(), "Quantità non valida", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void savePurchase(double quantity) {
        if (currentStockQuote == null) {
            Toast.makeText(requireContext(), "Errore: dati prezzo mancanti", Toast.LENGTH_SHORT).show();
            return;
        }

        double currentPrice;
        try {
            currentPrice = Double.parseDouble(currentStockQuote.getPrice());
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Errore parsing prezzo", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Non sei loggato!", Toast.LENGTH_LONG).show();
            return;
        }

        String safeSymbol = symbol.replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_");

        addToPortfolioButton.setEnabled(false);
        addToPortfolioButton.setText("Aggiungendo...");

        String databaseUrl = "https://cashub-29595-default-rtdb.europe-west1.firebasedatabase.app";
        DatabaseReference portfolioRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference()
                .child("users")
                .child(auth.getCurrentUser().getUid())
                .child("portfolio")
                .child(safeSymbol);

        portfolioRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // ✅ AZIONE GIÀ ESISTENTE → SOMMA QUANTITÀ
                    PortfolioStock existingStock = snapshot.getValue(PortfolioStock.class);

                    if (existingStock != null) {
                        double oldQuantity = existingStock.getQuantity();
                        double oldAvgPrice = existingStock.getAveragePrice();

                        double newQuantity = oldQuantity + quantity;
                        double newAvgPrice = ((oldQuantity * oldAvgPrice) + (quantity * currentPrice)) / newQuantity;

                        existingStock.setQuantity(newQuantity);
                        existingStock.setAveragePrice(newAvgPrice);

                        portfolioRef.setValue(existingStock)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✅ QUANTITÀ AGGIORNATA! Vecchia: " + oldQuantity + " → Nuova: " + newQuantity);

                                    // 🎯 SALVA IN CACHE
                                    StockCache.saveStock(
                                            requireContext(),
                                            symbol,
                                            companyName != null ? companyName : symbol,
                                            currency != null ? currency : "USD",
                                            exchange != null ? exchange : "N/A",
                                            exchangeFull != null ? exchangeFull : "N/A",
                                            currentPrice
                                    );

                                    Toast.makeText(requireContext(),
                                            "Ora possiedi " + newQuantity + " azioni di " + companyName,
                                            Toast.LENGTH_LONG).show();
                                    addToPortfolioButton.setText("Aggiunto ✓");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ ERRORE UPDATE: " + e.getMessage());
                                    Toast.makeText(requireContext(), "Errore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    addToPortfolioButton.setText("Aggiungi al portafoglio");
                                    addToPortfolioButton.setEnabled(true);
                                });
                    } else {
                        Toast.makeText(requireContext(), "Errore lettura dati esistenti", Toast.LENGTH_SHORT).show();
                        addToPortfolioButton.setText("Aggiungi al portafoglio");
                        addToPortfolioButton.setEnabled(true);
                    }

                } else {
                    // ✅ AZIONE NUOVA → SALVA NORMALMENTE
                    PortfolioStock portfolioStock = new PortfolioStock(
                            symbol,
                            companyName != null ? companyName : symbol,
                            currency != null ? currency : "USD",
                            exchange != null ? exchange : "N/A",
                            exchangeFull != null ? exchangeFull : "N/A"
                    );
                    portfolioStock.setQuantity(quantity);
                    portfolioStock.setAveragePrice(currentPrice);

                    portfolioRef.setValue(portfolioStock)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ NUOVA AZIONE SALVATA!");

                                // 🎯 SALVA IN CACHE
                                StockCache.saveStock(
                                        requireContext(),
                                        symbol,
                                        companyName != null ? companyName : symbol,
                                        currency != null ? currency : "USD",
                                        exchange != null ? exchange : "N/A",
                                        exchangeFull != null ? exchangeFull : "N/A",
                                        currentPrice
                                );

                                Toast.makeText(requireContext(), "Acquistate " + quantity + " azioni di " + symbol + "!", Toast.LENGTH_SHORT).show();
                                addToPortfolioButton.setText("Aggiunto ✓");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ ERRORE: " + e.getMessage());
                                Toast.makeText(requireContext(), "Errore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                addToPortfolioButton.setText("Aggiungi al portafoglio");
                                addToPortfolioButton.setEnabled(true);
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e(TAG, "ERRORE DB: " + error.getMessage());
                Toast.makeText(requireContext(), "Errore database: " + error.getMessage(), Toast.LENGTH_LONG).show();
                addToPortfolioButton.setText("Aggiungi al portafoglio");
                addToPortfolioButton.setEnabled(true);
            }
        });
    }

    private String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null) {
            return "$"; // Default USD
        }

        switch (currencyCode.toUpperCase()) {
            case "USD":
                return "$";
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            case "JPY":
                return "¥";
            case "CHF":
                return "CHF ";
            case "CAD":
                return "C$";
            case "AUD":
                return "A$";
            case "CNY":
                return "¥";
            case "INR":
                return "₹";
            default:
                return currencyCode + " ";
        }
    }
}
