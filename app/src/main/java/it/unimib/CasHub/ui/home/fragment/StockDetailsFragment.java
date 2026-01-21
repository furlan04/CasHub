package it.unimib.CasHub.ui.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.StockQuote;
import it.unimib.CasHub.ui.home.viewmodel.StockDetailsViewModel;
import it.unimib.CasHub.ui.home.viewmodel.StockDetailsViewModelFactory;
import it.unimib.CasHub.utils.NetworkUtil;

public class StockDetailsFragment extends Fragment {

    private static final String TAG = StockDetailsFragment.class.getSimpleName();

    private TextView stockNameTextView;
    private TextView exchangeFullTextView;
    private TextView symbolTextView;
    private TextView currencyTextView;
    private TextView exchangeTextView;
    private Button addToPortfolioButton;
    private LineChart weeklyChart;

    private StockDetailsViewModel viewModel;

    private String symbol;
    private String companyName;
    private String currency;
    private String exchange;
    private String exchangeFull;
    private boolean fromPortfolio = false;

    private StockQuote currentStock;

    public StockDetailsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            symbol = getArguments().getString("agencySymbol");
            companyName = getArguments().getString("agencyName");
            currency = getArguments().getString("agencyCurrency");
            exchange = getArguments().getString("agencyExchange");
            exchangeFull = getArguments().getString("agencyExchangeFull");
            fromPortfolio = getArguments().getBoolean("fromPortfolio", false);
        }

        StockDetailsViewModelFactory factory = new StockDetailsViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(StockDetailsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stock_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stockNameTextView = view.findViewById(R.id.StockName);
        exchangeFullTextView = view.findViewById(R.id.textExchangeFull);
        symbolTextView = view.findViewById(R.id.textSymbol);
        currencyTextView = view.findViewById(R.id.textCurrency);
        exchangeTextView = view.findViewById(R.id.textExchange);
        addToPortfolioButton = view.findViewById(R.id.btnAddToPortfolio);
        weeklyChart = view.findViewById(R.id.weeklyChart);

        if (fromPortfolio) {
            addToPortfolioButton.setVisibility(View.GONE);
        }

        if (companyName != null) {
            stockNameTextView.setText(companyName);
        }
        if (symbol != null) {
            symbolTextView.setText(getString(R.string.symbol_label, symbol));
        }
        if (currency != null) {
            currencyTextView.setText(getString(R.string.currency_label, currency));
        }
        if (exchange != null) {
            exchangeTextView.setText(getString(R.string.exchange_label, exchange));
        }

        addToPortfolioButton.setOnClickListener(v -> addToPortfolio());

        setupChart();

        if (symbol != null && !symbol.isEmpty()) {
            observeStockQuote();
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                observeChartData();
            }, 1500);
        } else {
            Toast.makeText(requireContext(), R.string.invalid_symbol, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupChart() {
        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.setTouchEnabled(true);
        weeklyChart.setDragEnabled(true);
        weeklyChart.setScaleEnabled(false);
        weeklyChart.setPinchZoom(false);
        weeklyChart.setDrawGridBackground(false);
        weeklyChart.setBackgroundColor(Color.parseColor("#1E1E1E"));

        XAxis xAxis = weeklyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#B0B0B0"));
        xAxis.setTextSize(10f);

        YAxis leftAxis = weeklyChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#2E2E2E"));
        leftAxis.setTextColor(Color.parseColor("#B0B0B0"));
        leftAxis.setTextSize(10f);

        weeklyChart.getAxisRight().setEnabled(false);
        weeklyChart.getLegend().setEnabled(false);
        weeklyChart.setExtraBottomOffset(10f);
    }

    private void observeStockQuote() {
        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        addToPortfolioButton.setEnabled(false);
        viewModel.getStockQuote(symbol).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                currentStock = ((Result.Success<StockQuote>) result).getData();
                handleStockQuoteSuccess(currentStock);
            } else {
                handleStockQuoteFailure(((Result.Error) result).getMessage());
            }
        });
    }

    private void observeChartData() {
        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            return;
        }

        viewModel.getChartData(symbol).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                populateChart(((Result.Success<ChartData>) result).getData());
            } else {
                Log.e(TAG, getString(R.string.error_loading_data, ((Result.Error) result).getMessage()));
                Toast.makeText(requireContext(), R.string.unable_to_load_chart, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateChart(ChartData chartData) {
        List<Entry> entries = new ArrayList<>();
        List<String> dates = chartData.getDates();
        List<Float> prices = chartData.getPrices();

        for (int i = 0; i < prices.size(); i++) {
            entries.add(new Entry(i, prices.get(i)));
        }

        boolean isPositiveTrend = prices.get(prices.size() - 1) >= prices.get(0);
        int lineColor = isPositiveTrend ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336");

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.price_label));
        dataSet.setColor(lineColor);
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(lineColor);
        dataSet.setFillAlpha(30);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setHighLightColor(lineColor);

        LineData lineData = new LineData(dataSet);
        weeklyChart.setData(lineData);

        List<String> formattedDates = new ArrayList<>();
        for (String date : dates) {
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
        xAxis.setDrawGridLines(false);

        weeklyChart.animateX(800);
    }

    private void handleStockQuoteSuccess(StockQuote stock) {
        if (getActivity() == null) return;

        requireActivity().runOnUiThread(() -> {
            addToPortfolioButton.setEnabled(true);

            String currencySymbol = getCurrencySymbol(currency);

            String price = stock.getPrice();
            String change = stock.getChange();
            String changePercent = stock.getChangePercent();

            String priceInfo = getString(R.string.price_info,
                    currencySymbol,
                    price,
                    change,
                    changePercent
            );

            exchangeFullTextView.setText(priceInfo);
        });
    }

    private void handleStockQuoteFailure(String errorMessage) {
        if (getActivity() == null) return;

        requireActivity().runOnUiThread(() -> {
            addToPortfolioButton.setEnabled(true);
            Toast.makeText(requireContext(), getString(R.string.error_label, errorMessage), Toast.LENGTH_LONG).show();
            Log.e(TAG, getString(R.string.error_loading_data, errorMessage));
        });
    }

    private void addToPortfolio() {
        if (currentStock == null) {
            Toast.makeText(requireContext(), R.string.data_not_loaded, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), R.string.error_not_logged_in, Toast.LENGTH_LONG).show();
            return;
        }

        showBuyStockDialog();
    }

    private void showBuyStockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.buy_stock_title, companyName));

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView priceInfo = new TextView(requireContext());
        priceInfo.setText(getString(R.string.current_price,
                getCurrencySymbol(currency),
                currentStock.getPrice()));
        priceInfo.setTextSize(16);
        priceInfo.setPadding(0, 0, 0, 20);
        layout.addView(priceInfo);

        final EditText quantityInput = new EditText(requireContext());
        quantityInput.setHint(R.string.quantity_hint);
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(quantityInput);

        builder.setView(layout);

        builder.setPositiveButton(R.string.confirm, null);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String quantityStr = quantityInput.getText().toString();

            if (quantityStr.isEmpty()) {
                Toast.makeText(requireContext(), R.string.insert_quantity, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double quantity = Double.parseDouble(quantityStr);

                if (quantity <= 0) {
                    Toast.makeText(requireContext(), R.string.quantity_must_be_positive, Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                savePurchase(quantity);

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), R.string.invalid_quantity, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePurchase(double quantity) {
        if (currentStock == null) {
            Toast.makeText(requireContext(), R.string.error_missing_price, Toast.LENGTH_SHORT).show();
            return;
        }

        double currentPrice;
        try {
            currentPrice = Double.parseDouble(currentStock.getPrice());
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.error_parsing_price, Toast.LENGTH_SHORT).show();
            return;
        }

        PortfolioStock portfolioStock = new PortfolioStock(
                symbol,
                companyName != null ? companyName : symbol,
                currency != null ? currency : "USD",
                exchange != null ? exchange : "N/A",
                exchangeFull != null ? exchangeFull : "N/A"
        );
        portfolioStock.setQuantity(quantity);
        portfolioStock.setAveragePrice(currentPrice);

        viewModel.addStockToPortfolio(portfolioStock);

        Toast.makeText(requireContext(),
                getString(R.string.purchase_success, String.valueOf(quantity), symbol),
                Toast.LENGTH_SHORT).show();
        addToPortfolioButton.setText(R.string.added_checkmark);
    }

    private String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null) {
            return "$";
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