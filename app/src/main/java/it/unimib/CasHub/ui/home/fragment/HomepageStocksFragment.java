package it.unimib.CasHub.ui.home.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import it.unimib.CasHub.utils.StockCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import java.util.Date;
import java.util.Calendar;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.LinearLayout;
import android.widget.EditText;
import com.github.mikephil.charting.charts.LineChart;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.database.DatabaseReference;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.database.ValueEventListener;
import androidx.navigation.Navigation;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.PortfolioAdapter;
import it.unimib.CasHub.model.PortfolioStock;


public class HomepageStocksFragment extends Fragment {

    private RecyclerView recyclerViewPortfolio;
    private PortfolioAdapter adapter;
    private TextView tvEmpty;
    private TextView textViewTitoli;
    private ProgressBar progressBar;
    private FloatingActionButton fabMain, fabAdd, fabRemove;
    private TextView tvAdd, tvRemove;
    private View fabOverlay;
    private boolean isFabOpen = false;
    private TextView textViewRendimentoPortafoglio;
    private LineChart portfolioChart;
    private static final String TAG = "HomepageStocksFragment";




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homepage_stocks, container, false);

        // Inizializza views
        recyclerViewPortfolio = view.findViewById(R.id.recyclerViewPortfolio);
        tvEmpty = view.findViewById(R.id.tvEmptyPortfolio);
        textViewTitoli = view.findViewById(R.id.textViewTitoli);
        textViewRendimentoPortafoglio = view.findViewById(R.id.textViewRendimentoPortafoglio);
        progressBar = view.findViewById(R.id.progressBar);
        portfolioChart = view.findViewById(R.id.portfolioChart);


        // FAB
        fabMain = view.findViewById(R.id.fabMain);
        fabAdd = view.findViewById(R.id.fabAdd);
        fabRemove = view.findViewById(R.id.fabRemove);
        tvAdd = view.findViewById(R.id.tvAdd);
        tvRemove = view.findViewById(R.id.tvRemove);
        fabOverlay = view.findViewById(R.id.fabOverlay);

        // Setup RecyclerView
        recyclerViewPortfolio.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PortfolioAdapter(stock -> {
            Toast.makeText(requireContext(), "Clicked: " + stock.getSymbol(), Toast.LENGTH_SHORT).show();
        });
        recyclerViewPortfolio.setAdapter(adapter);

        // Setup FAB
        setupFab();

        // Carica portfolio
        loadPortfolio();

        setupPortfolioChart();

        return view;
    }

    private void setupPortfolioChart() {
        portfolioChart.getDescription().setEnabled(false);
        portfolioChart.setTouchEnabled(true);
        portfolioChart.setDragEnabled(true);
        portfolioChart.setScaleEnabled(false);
        portfolioChart.setPinchZoom(false);
        portfolioChart.setDrawGridBackground(false);
        portfolioChart.setBackgroundColor(Color.parseColor("#1E1E1E"));

        XAxis xAxis = portfolioChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#B0B0B0"));
        xAxis.setTextSize(10f);

        YAxis leftAxis = portfolioChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#2E2E2E"));
        leftAxis.setTextColor(Color.parseColor("#B0B0B0"));
        leftAxis.setTextSize(10f);

        portfolioChart.getAxisRight().setEnabled(false);
        portfolioChart.getLegend().setEnabled(false);
        portfolioChart.setExtraBottomOffset(10f);
    }
    private void loadPortfolioHistoryChart() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String databaseUrl = "https://cashub-29595-default-rtdb.europe-west1.firebasedatabase.app";

        FirebaseDatabase.getInstance(databaseUrl)
                .getReference()
                .child("users")
                .child(auth.getCurrentUser().getUid())
                .child("portfolioHistory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded() || getContext() == null) return;

                        Log.e("CHART_DEBUG", "Snapshot exists: " + snapshot.exists());
                        Log.e("CHART_DEBUG", "Children count: " + snapshot.getChildrenCount());

                        if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                            portfolioChart.setNoDataText("Dati storici non disponibili");
                            portfolioChart.clear();
                            portfolioChart.invalidate();
                            return;
                        }

                        List<Entry> entries = new ArrayList<>();
                        List<String> labels = new ArrayList<>();

                        int index = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String timeKey = child.getKey();
                            Double value = child.getValue(Double.class);

                            if (value != null && timeKey != null) {
                                entries.add(new Entry(index, value.floatValue()));

                                // Estrae solo HH:mm per label
                                String[] parts = timeKey.split(" ");
                                if (parts.length > 1) {
                                    labels.add(parts[1]); // HH:mm
                                } else {
                                    labels.add(timeKey);
                                }

                                index++;
                            }
                        }

                        if (entries.isEmpty()) {
                            portfolioChart.setNoDataText("Nessun dato disponibile");
                            portfolioChart.clear();
                            portfolioChart.invalidate();
                            return;
                        }

                        // Crea dataset
                        LineDataSet dataSet = new LineDataSet(entries, "Valore Portafoglio");
                        dataSet.setColor(Color.parseColor("#4CAF50"));
                        dataSet.setValueTextColor(Color.WHITE);
                        dataSet.setLineWidth(2f);
                        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
                        dataSet.setCircleRadius(4f);
                        dataSet.setDrawCircleHole(false);
                        dataSet.setDrawValues(false);

                        LineData lineData = new LineData(dataSet);
                        portfolioChart.setData(lineData);

                        // Configura asse X con label temporali
                        XAxis xAxis = portfolioChart.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1f);
                        xAxis.setTextColor(Color.WHITE);

                        // Configura asse Y
                        YAxis leftAxis = portfolioChart.getAxisLeft();
                        leftAxis.setTextColor(Color.WHITE);
                        portfolioChart.getAxisRight().setEnabled(false);

                        portfolioChart.getDescription().setEnabled(false);
                        portfolioChart.getLegend().setTextColor(Color.WHITE);
                        portfolioChart.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (!isAdded() || getContext() == null) return;
                        Log.e(TAG, "Errore caricamento grafico: " + error.getMessage());
                    }
                });
    }


    private void setupFab() {
        fabMain.setOnClickListener(v -> {
            if (isFabOpen) {
                closeFabMenu();
            } else {
                openFabMenu();
            }
        });

        fabOverlay.setOnClickListener(v -> closeFabMenu());

        fabAdd.setOnClickListener(v -> {
            closeFabMenu();
            Navigation.findNavController(v).navigate(R.id.selectionAgencyStockFragment);
        });




        fabRemove.setOnClickListener(v -> {
            closeFabMenu();  // Chiude il menu FAB
            showRemoveStockDialog();  // Mostra dialog rimozione
        });


        tvAdd.setOnClickListener(v -> fabAdd.performClick());
        tvRemove.setOnClickListener(v -> fabRemove.performClick());
    }

    private void openFabMenu() {
        isFabOpen = true;
        fabOverlay.setVisibility(View.VISIBLE);

        fabAdd.setVisibility(View.VISIBLE);
        fabRemove.setVisibility(View.VISIBLE);
        tvAdd.setVisibility(View.VISIBLE);
        tvRemove.setVisibility(View.VISIBLE);

        // Animazione rotazione FAB principale
        fabMain.animate().rotation(45f).setDuration(200).start();

        // Animazione entrata mini FAB
        fabAdd.setAlpha(0f);
        fabAdd.setTranslationY(100f);
        fabAdd.animate().alpha(1f).translationY(0f).setDuration(200).start();

        fabRemove.setAlpha(0f);
        fabRemove.setTranslationY(100f);
        fabRemove.animate().alpha(1f).translationY(0f).setDuration(200).setStartDelay(50).start();

        tvAdd.setAlpha(0f);
        tvAdd.animate().alpha(1f).setDuration(200).start();

        tvRemove.setAlpha(0f);
        tvRemove.animate().alpha(1f).setDuration(200).setStartDelay(50).start();
    }

    private void closeFabMenu() {
        isFabOpen = false;
        fabOverlay.setVisibility(View.GONE);

        // Animazione rotazione FAB principale
        fabMain.animate().rotation(0f).setDuration(200).start();

        // Animazione uscita mini FAB
        fabAdd.animate().alpha(0f).translationY(100f).setDuration(200).withEndAction(() -> {
            fabAdd.setVisibility(View.GONE);
        }).start();

        fabRemove.animate().alpha(0f).translationY(100f).setDuration(200).withEndAction(() -> {
            fabRemove.setVisibility(View.GONE);
        }).start();

        tvAdd.animate().alpha(0f).setDuration(200).withEndAction(() -> {
            tvAdd.setVisibility(View.GONE);
        }).start();

        tvRemove.animate().alpha(0f).setDuration(200).withEndAction(() -> {
            tvRemove.setVisibility(View.GONE);
        }).start();
    }

    private void loadPortfolio() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewPortfolio.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        String databaseUrl = "https://cashub-29595-default-rtdb.europe-west1.firebasedatabase.app";

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setText("Effettua il login per vedere il portfolio");
            tvEmpty.setVisibility(View.VISIBLE);
            textViewTitoli.setText("€0.00");
            return;
        }

        FirebaseDatabase.getInstance(databaseUrl)
                .getReference()
                .child("users")
                .child(auth.getCurrentUser().getUid())
                .child("portfolio")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<PortfolioStock> stocks = new ArrayList<>();
                        double totalInvested = 0.0;  // Valore investito
                        double totalCurrent = 0.0;   // Valore attuale

                        for (DataSnapshot child : snapshot.getChildren()) {
                            PortfolioStock stock = child.getValue(PortfolioStock.class);
                            if (stock != null) {
                                // Carica dalla cache
                                StockCache.CachedStock cached = StockCache.getStock(requireContext(), stock.getSymbol());
                                if (cached != null) {
                                    stock.setName(cached.companyName);
                                    stock.setCurrency(cached.currency);
                                    stock.setExchange(cached.exchange);
                                    stock.setExchangeFullName(cached.exchangeFull);
                                    stock.setCurrentPrice(cached.currentPrice); // Prezzo attuale dalla cache
                                }

                                // Calcola valori
                                double invested = stock.getQuantity() * stock.getAveragePrice();
                                double current = stock.getQuantity() * stock.getCurrentPrice();

                                totalInvested += invested;
                                totalCurrent += current;

                                stocks.add(stock);
                            }
                        }

                        progressBar.setVisibility(View.GONE);

                        if (stocks.isEmpty()) {
                            tvEmpty.setText("Nessuna azione in portafoglio");
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerViewPortfolio.setVisibility(View.GONE);
                            textViewTitoli.setText("€0.00");
                            textViewRendimentoPortafoglio.setText("€0.00 (0.00%)");
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerViewPortfolio.setVisibility(View.VISIBLE);
                            adapter.setStocks(stocks);

                            textViewTitoli.setText(String.format("€%.2f", totalCurrent));

                            double change = totalCurrent - totalInvested;
                            double changePercent = (totalInvested > 0) ? (change / totalInvested) * 100 : 0;

                            String changeText = String.format("€%.2f (%.2f%%)", change, changePercent);
                            textViewRendimentoPortafoglio.setText(changeText);

                            if (change >= 0) {
                                textViewRendimentoPortafoglio.setTextColor(Color.parseColor("#4CAF50"));
                            } else {
                                textViewRendimentoPortafoglio.setTextColor(Color.parseColor("#F44336"));
                            }

                            // 🎯 SALVA SNAPSHOT GIORNALIERO
                            savePortfolioSnapshot(totalCurrent);

                            // 🎯 CARICA IL GRAFICO STORICO
                            loadPortfolioHistoryChart();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Errore: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void savePortfolioSnapshot(double totalValue) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String databaseUrl = "https://cashub-29595-default-rtdb.europe-west1.firebasedatabase.app";
        DatabaseReference historyRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference()
                .child("users")
                .child(auth.getCurrentUser().getUid())
                .child("portfolioHistory");

        // 🎯 TEST: Usa TIMESTAMP invece di data
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String timeKey = timeFormat.format(new Date());

        historyRef.child(timeKey).setValue(totalValue)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Snapshot salvato: " + timeKey + " = " + totalValue);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Errore salvataggio snapshot: " + e.getMessage());
                });
    }

    private void showStockSelectionDialog(List<PortfolioStock> stocks) {
        String[] stockNames = new String[stocks.size()];
        for (int i = 0; i < stocks.size(); i++) {
            PortfolioStock stock = stocks.get(i);
            String currencySymbol = getCurrencySymbol(stock.getCurrency());
            double value = stock.getQuantity() * stock.getAveragePrice();

            stockNames[i] = stock.getName() + "\n" +
                    stock.getSymbol() + " • " +
                    stock.getQuantity() + " azioni • " +
                    currencySymbol + String.format("%.2f", value);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialogStyle);
        builder.setTitle("🗑️ Vendi titolo");
        builder.setItems(stockNames, (dialog, which) -> {
            PortfolioStock selectedStock = stocks.get(which);
            showQuantityInputDialog(selectedStock);
        });
        builder.setNegativeButton("Annulla", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#757575"));
        }
    }
    private void showQuantityInputDialog(PortfolioStock stock) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialogStyle);
        builder.setTitle("Quantità da vendere");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        // Info titolo
        TextView info = new TextView(requireContext());
        String currencySymbol = getCurrencySymbol(stock.getCurrency());
        info.setText("" + stock.getName() + "\n" +
                "" + stock.getSymbol() + "\n" +
                "Possedute: " + stock.getQuantity() + " azioni\n" +
                "Prezzo medio: " + currencySymbol + String.format("%.2f", stock.getAveragePrice()));
        info.setTextSize(14);
        info.setPadding(0, 0, 0, 30);
        layout.addView(info);

        // Input quantità
        final EditText quantityInput = new EditText(requireContext());
        quantityInput.setHint("Quantità da vendere");
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        quantityInput.setText(String.valueOf(stock.getQuantity())); // Default: tutte
        quantityInput.setSelectAllOnFocus(true);
        layout.addView(quantityInput);

        builder.setView(layout);

        builder.setPositiveButton("VENDI", null);
        builder.setNegativeButton("ANNULLA", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Personalizza pulsanti
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String quantityStr = quantityInput.getText().toString();

            if (quantityStr.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci una quantità", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double quantityToRemove = Double.parseDouble(quantityStr);

                if (quantityToRemove <= 0) {
                    Toast.makeText(requireContext(), "Quantità deve essere > 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (quantityToRemove > stock.getQuantity()) {
                    Toast.makeText(requireContext(),
                            "Non puoi vendere più di " + stock.getQuantity() + " azioni",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                confirmRemoveStock(stock, quantityToRemove);

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Quantità non valida", Toast.LENGTH_SHORT).show();
            }
        });

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#F44336"));
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#757575"));
        }
    }

    private void showRemoveStockDialog() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Non sei loggato!", Toast.LENGTH_SHORT).show();
            return;
        }

        String databaseUrl = "https://cashub-29595-default-rtdb.europe-west1.firebasedatabase.app";

        // 🎯 CARICA DATI DIRETTAMENTE DA FIREBASE (non dall'adapter!)
        FirebaseDatabase.getInstance(databaseUrl)
                .getReference()
                .child("users")
                .child(auth.getCurrentUser().getUid())
                .child("portfolio")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                            Toast.makeText(requireContext(), "Nessun titolo da vendere", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<PortfolioStock> stocks = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            PortfolioStock stock = child.getValue(PortfolioStock.class);
                            if (stock != null) {
                                stocks.add(stock);
                            }
                        }

                        if (stocks.isEmpty()) {
                            Toast.makeText(requireContext(), "Nessun titolo da vendere", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Mostra il dialog con i dati aggiornati
                        showStockSelectionDialog(stocks);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), "Errore: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void confirmRemoveStock(PortfolioStock stock, double quantityToRemove) {
        String currencySymbol = getCurrencySymbol(stock.getCurrency());
        double valueToRemove = quantityToRemove * stock.getAveragePrice();
        boolean removeAll = (quantityToRemove >= stock.getQuantity());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialogStyle);
        builder.setTitle("Conferma " + (removeAll ? "vendita" : "vendita"));

        String action = removeAll ? "vendere completamente" : "vendere";
        String message = "Stai per " + action + ":\n\n" +
                "" + stock.getName() + "\n" +
                "" + stock.getSymbol() + "\n" +
                "Quantità: " + quantityToRemove + " azioni\n" +
                "Valore: " + currencySymbol + String.format("%.2f", valueToRemove) + "\n";

        if (!removeAll) {
            double remaining = stock.getQuantity() - quantityToRemove;
            message += "\nRimarranno: " + remaining + " azioni";
        }

        message += "\n\nQuesta azione è irreversibile.";

        builder.setMessage(message);

        builder.setPositiveButton(removeAll ? "VENDI" : "VENDI", (dialog, which) -> {
            removeStockFromPortfolio(stock, quantityToRemove);
        });

        builder.setNegativeButton("ANNULLA", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#F44336"));
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#757575"));
        }
    }



    private void removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String databaseUrl = "https://cashub-29595-default-rtdb.europe-west1.firebasedatabase.app";

        String safeSymbol = stock.getSymbol().replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_");

        DatabaseReference stockRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference()
                .child("users")
                .child(auth.getCurrentUser().getUid())
                .child("portfolio")
                .child(safeSymbol);

        double newQuantity = stock.getQuantity() - quantityToRemove;

        if (newQuantity <= 0) {
            // 🗑️ ELIMINA COMPLETAMENTE
            stockRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(),
                                stock.getName() + " venduto dal portafoglio",
                                Toast.LENGTH_SHORT).show();
                        loadPortfolio();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                "Errore: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Errore vendita titolo: " + e.getMessage());
                    });
        } else {
            // 📉 AGGIORNA QUANTITÀ
            stock.setQuantity(newQuantity);
            stockRef.setValue(stock)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(),
                                "Vendute " + quantityToRemove + " azioni. Possiedi: " + newQuantity,
                                Toast.LENGTH_SHORT).show();
                        loadPortfolio();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                "Errore: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Errore aggiornamento quantità: " + e.getMessage());
                    });
        }
    }


    private String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null) return "$";

        switch (currencyCode.toUpperCase()) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CHF": return "CHF ";
            case "CAD": return "C$";
            case "AUD": return "A$";
            case "CNY": return "¥";
            case "INR": return "₹";
            default: return currencyCode + " ";
        }
    }





}
