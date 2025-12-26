package it.unimib.CasHub.ui.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.CategorySpinnerAdapter;
import it.unimib.CasHub.adapter.CurrencySpinnerAdapter;
import it.unimib.CasHub.adapter.TransactionRecyclerAdapter;
import it.unimib.CasHub.model.CurrencyEntity;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.model.TransactionType;
import it.unimib.CasHub.repository.ForexRepository;
import it.unimib.CasHub.ui.home.viewmodel.CurrencyListViewModel;
import it.unimib.CasHub.ui.home.viewmodel.CurrencyListViewModelFactory;
import it.unimib.CasHub.ui.home.viewmodel.HomepageTransactionViewModel;
import it.unimib.CasHub.ui.home.viewmodel.HomepageTransactionViewModelFactory;
import it.unimib.CasHub.ui.home.viewmodel.RatesConversionViewModel;
import it.unimib.CasHub.ui.home.viewmodel.RatesConversionViewModelFactory;
import it.unimib.CasHub.utils.ServiceLocator;

public class HomepageTransactionFragment extends Fragment implements TransactionRecyclerAdapter.OnDeleteButtonClickListener {

    private PieChart pieChart;
    private RecyclerView recyclerView;
    private TransactionRecyclerAdapter adapter;
    private HomepageTransactionViewModel homepageTransactionViewModel;
    private CurrencyListViewModel currencyListViewModel;
    private RatesConversionViewModel ratesConversionViewModel;
    private Spinner categorySpinner;
    private Spinner baseCurrencySpinner;
    private CategorySpinnerAdapter categoryAdapter;
    private CurrencySpinnerAdapter baseCurrencyAdapter;
    private List<TransactionEntity> allTransactions = new ArrayList<>();
    private TextView balanceTextView;

    public HomepageTransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ForexRepository forexRepository = ServiceLocator.getInstance().getForexRepository(requireActivity().getApplication(), getResources().getBoolean(R.bool.debug));
        currencyListViewModel = new ViewModelProvider(this, new CurrencyListViewModelFactory(forexRepository)).get(CurrencyListViewModel.class);
        ratesConversionViewModel = new ViewModelProvider(this, new RatesConversionViewModelFactory(forexRepository)).get(RatesConversionViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_homepage_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pieChart = view.findViewById(R.id.pieChartTransaction);
        recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        categorySpinner = view.findViewById(R.id.spinnerCategory);
        baseCurrencySpinner = view.findViewById(R.id.spinnerBaseCurrency);
        balanceTextView = view.findViewById(R.id.balanceTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionRecyclerAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        List<TransactionType> categories = new ArrayList<>();
        categories.add(null);
        categories.addAll(Arrays.asList(TransactionType.values()));
        categoryAdapter = new CategorySpinnerAdapter(requireContext(), categories);
        categorySpinner.setAdapter(categoryAdapter);

        baseCurrencyAdapter = new CurrencySpinnerAdapter(requireContext(), new ArrayList<>());
        baseCurrencySpinner.setAdapter(baseCurrencyAdapter);

        boolean debugMode = getResources().getBoolean(R.bool.debug);
        homepageTransactionViewModel = new ViewModelProvider(requireActivity(), new HomepageTransactionViewModelFactory(requireActivity().getApplication(), debugMode)).get(HomepageTransactionViewModel.class);

        homepageTransactionViewModel.getTransactions().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                allTransactions = ((Result.Success<List<TransactionEntity>>) result).getData();
                updateDisplayedTransactions();
            } else {
                showError(((Result.Error<?>) result).getMessage());
            }
        });

        currencyListViewModel.getCurrencies().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                List<CurrencyEntity> currencies = ((Result.Success<List<CurrencyEntity>>) result).getData();
                baseCurrencyAdapter.clear();
                baseCurrencyAdapter.addAll(currencies);
                baseCurrencyAdapter.notifyDataSetChanged();
            } else {
                showError(((Result.Error<?>) result).getMessage());
            }
        });

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDisplayedTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        categorySpinner.setOnItemSelectedListener(listener);
        baseCurrencySpinner.setOnItemSelectedListener(listener);

        Button btnAddTransaction = view.findViewById(R.id.btnAddTransaction);
        btnAddTransaction.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_homepageTransactionFragment_to_transactionFragment);
        });
    }

    private void updateDisplayedTransactions() {
        TransactionType selectedCategory = (TransactionType) categorySpinner.getSelectedItem();
        CurrencyEntity baseCurrency = (CurrencyEntity) baseCurrencySpinner.getSelectedItem();

        if (baseCurrency == null) {
            balanceTextView.setText("");
            adapter.clear();
            adapter.notifyDataSetChanged();
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        List<TransactionEntity> filteredTransactions = allTransactions.stream()
                .filter(t -> selectedCategory == null || t.getType() == selectedCategory)
                .collect(Collectors.toList());

        // Mostra subito le transazioni originali
        adapter.clear();
        adapter.addAll(filteredTransactions);
        adapter.notifyDataSetChanged();

        // Converti per calcolare il saldo e il grafico
        LiveData<Result<List<TransactionEntity>>> conversionResult =
                ratesConversionViewModel.getBasedList(filteredTransactions, baseCurrency.getCode());

        conversionResult.observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                List<TransactionEntity> basedTransactions =
                        ((Result.Success<List<TransactionEntity>>) result).getData();

                double balance = 0.0;
                for (TransactionEntity transaction : basedTransactions) {
                    balance += transaction.getAmount();
                }

                balanceTextView.setText(String.format("Saldo: %.2f %s",
                        balance, baseCurrency.getCode()));

                // Aggiorna il grafico con i valori convertiti
                setupPieChart(basedTransactions);

            } else if (result instanceof Result.Error) {
                showError(((Result.Error<?>) result).getMessage());
            }
        });
    }

    @Override
    public void onDeleteButtonClicked(TransactionEntity transaction) {
        int id = transaction.getId();
        homepageTransactionViewModel.deleteTransaction(id);
    }

    private void showError(String message) {
        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    private void setupPieChart(List<TransactionEntity> transactions) {
        float totalIncome = 0f;
        float totalExpenses = 0f;

        for (TransactionEntity transaction : transactions) {
            if (transaction.getAmount() >= 0) {
                totalIncome += (float) transaction.getAmount();
            } else {
                totalExpenses += (float) Math.abs(transaction.getAmount());
            }
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (totalIncome > 0) {
            entries.add(new PieEntry(totalIncome, "Entrate"));
            colors.add(Color.GREEN);
        }
        if (totalExpenses > 0) {
            entries.add(new PieEntry(totalExpenses, "Uscite"));
            colors.add(Color.RED);
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(55f);

        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);

        pieChart.animateY(1200, Easing.EaseInOutQuad);

        pieChart.setData(data);
        pieChart.invalidate();
    }
}
