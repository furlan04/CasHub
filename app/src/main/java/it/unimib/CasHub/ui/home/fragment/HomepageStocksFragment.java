package it.unimib.CasHub.ui.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.PortfolioAdapter;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.ui.home.viewmodel.HomepageStocksViewModel;
import it.unimib.CasHub.ui.home.viewmodel.HomepageStocksViewModelFactory;

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
    private HomepageStocksViewModel viewModel;
    private double lastPortfolioValue = 0.0;
    private final List<PortfolioStock> portfolioStocks = new ArrayList<>();

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

        setupFab();
        setupPortfolioChart();

        HomepageStocksViewModelFactory factory = new HomepageStocksViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(HomepageStocksViewModel.class);

        observePortfolio();
        observePortfolioHistory();
        observeSnackbar();

        return view;
    }

    private void observeSnackbar() {
        viewModel.getSnackbarMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
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

    private void observePortfolio() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewPortfolio.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        viewModel.getPortfolio().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                List<PortfolioStock> portfolio = ((Result.Success<List<PortfolioStock>>) result).getData();
                viewModel.refreshPortfolioStocks(portfolio);
                handlePortfolio(portfolio);
            } else if (result instanceof Result.Error) {
                progressBar.setVisibility(View.GONE);
                String errorMessage = ((Result.Error) result).getMessage();
                Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handlePortfolio(List<PortfolioStock> stocks) {
        this.portfolioStocks.clear();
        this.portfolioStocks.addAll(stocks);
        double totalCurrentValue = 0.0;

        for (PortfolioStock stock : stocks) {
            if (stock != null) {
                totalCurrentValue += stock.getQuantity() * stock.getAveragePrice();
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

            textViewTitoli.setText(String.format("€%.2f", totalCurrentValue));

            double change = 0.0;
            double changePercent = 0.0;
            if (lastPortfolioValue > 0) {
                change = totalCurrentValue - lastPortfolioValue;
                changePercent = (change / lastPortfolioValue) * 100;
            }

            String changeText = String.format("€%.2f (%.2f%%)", change, changePercent);
            textViewRendimentoPortafoglio.setText(changeText);

            if (change >= 0) {
                textViewRendimentoPortafoglio.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                textViewRendimentoPortafoglio.setTextColor(Color.parseColor("#F44336"));
            }

            viewModel.savePortfolioSnapshot(totalCurrentValue);
        }
    }

    private void observePortfolioHistory() {
        viewModel.getPortfolioHistory().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                List<DataSnapshot> history = ((Result.Success<List<DataSnapshot>>) result).getData();
                handlePortfolioHistory(history);
            } else if (result instanceof Result.Error) {
                Log.e(TAG, "Error loading chart: " + ((Result.Error) result).getMessage());
            }
        });
    }

    private void handlePortfolioHistory(List<DataSnapshot> history) {
        if (!isAdded() || getContext() == null) return;

        if (history.isEmpty()) {
            portfolioChart.setNoDataText("Dati storici non disponibili");
            portfolioChart.clear();
            portfolioChart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (history.size() > 1) {
            DataSnapshot previousSnapshot = history.get(history.size() - 2);
            Double previousValue = previousSnapshot.getValue(Double.class);
            if (previousValue != null) {
                lastPortfolioValue = previousValue;
            }
        } else if (history.size() == 1) {
            DataSnapshot firstSnapshot = history.get(0);
            Double firstValue = firstSnapshot.getValue(Double.class);
            if (firstValue != null) {
                lastPortfolioValue = firstValue;
            }
        }

        int index = 0;
        for (DataSnapshot child : history) {
            String timeKey = child.getKey();
            Double value = child.getValue(Double.class);

            if (value != null && timeKey != null) {
                entries.add(new Entry(index, value.floatValue()));

                String[] parts = timeKey.split("-");
                if (parts.length == 3) {
                    labels.add(parts[2] + "/" + parts[1]);
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

        XAxis xAxis = portfolioChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);

        YAxis leftAxis = portfolioChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        portfolioChart.getAxisRight().setEnabled(false);

        portfolioChart.getDescription().setEnabled(false);
        portfolioChart.getLegend().setTextColor(Color.WHITE);
        portfolioChart.invalidate();
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
            closeFabMenu();
            showRemoveStockDialog();
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

        fabMain.animate().rotation(45f).setDuration(200).start();

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

        fabMain.animate().rotation(0f).setDuration(200).start();

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

    private void showRemoveStockDialog() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Non sei loggato!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (portfolioStocks.isEmpty()) {
            Toast.makeText(requireContext(), "Nessun titolo da vendere", Toast.LENGTH_SHORT).show();
            return;
        }

        showStockSelectionDialog(portfolioStocks);
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

        TextView info = new TextView(requireContext());
        String currencySymbol = getCurrencySymbol(stock.getCurrency());
        info.setText("" + stock.getName() + "\n" +
                "" + stock.getSymbol() + "\n" +
                "Possedute: " + stock.getQuantity() + " azioni\n" +
                "Prezzo medio: " + currencySymbol + String.format("%.2f", stock.getAveragePrice()));
        info.setTextSize(14);
        info.setPadding(0, 0, 0, 30);
        layout.addView(info);

        final EditText quantityInput = new EditText(requireContext());
        quantityInput.setHint("Quantità da vendere");
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        quantityInput.setText(String.valueOf(stock.getQuantity()));
        quantityInput.setSelectAllOnFocus(true);
        layout.addView(quantityInput);

        builder.setView(layout);

        builder.setPositiveButton("VENDI", null);
        builder.setNegativeButton("ANNULLA", null);

        AlertDialog dialog = builder.create();
        dialog.show();

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
            viewModel.removeStockFromPortfolio(stock, quantityToRemove);
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
