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
import androidx.core.content.ContextCompat;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    private PieChart pieChartExpenses, pieChartBalance;
    private Button btnShowExpenses, btnShowBalance;

    private RecyclerView recyclerView;
    private TransactionRecyclerAdapter adapter;
    private HomepageTransactionViewModel homepageTransactionViewModel;
    private CurrencyListViewModel currencyListViewModel;
    private RatesConversionViewModel ratesConversionViewModel;

    private Spinner categorySpinner, baseCurrencySpinner;
    private CategorySpinnerAdapter categoryAdapter;
    private CurrencySpinnerAdapter baseCurrencyAdapter;

    private List<TransactionEntity> allTransactions = new ArrayList<>();
    private TextView balanceTextView;

    private LiveData<Result<List<TransactionEntity>>> currentConversionLiveData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_homepage_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pieChartExpenses = view.findViewById(R.id.pieChartExpenses);
        pieChartBalance = view.findViewById(R.id.pieChartBalance);
        btnShowExpenses = view.findViewById(R.id.btnShowExpenses);
        btnShowBalance = view.findViewById(R.id.btnShowBalance);

        recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        categorySpinner = view.findViewById(R.id.spinnerCategory);
        baseCurrencySpinner = view.findViewById(R.id.spinnerBaseCurrency);
        balanceTextView = view.findViewById(R.id.balanceTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionRecyclerAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Spinner categorie
        List<TransactionType> categories = new ArrayList<>();
        categories.add(null);
        categories.addAll(Arrays.asList(TransactionType.values()));
        categoryAdapter = new CategorySpinnerAdapter(requireContext(), categories);
        categorySpinner.setAdapter(categoryAdapter);

        baseCurrencyAdapter = new CurrencySpinnerAdapter(requireContext(), new ArrayList<>());
        baseCurrencySpinner.setAdapter(baseCurrencyAdapter);

        ForexRepository forexRepository = ServiceLocator.getInstance().getForexRepository(
                requireActivity().getApplication(), getResources().getBoolean(R.bool.debug));

        currencyListViewModel = new ViewModelProvider(this, new CurrencyListViewModelFactory(forexRepository))
                .get(CurrencyListViewModel.class);

        ratesConversionViewModel = new ViewModelProvider(this, new RatesConversionViewModelFactory(forexRepository))
                .get(RatesConversionViewModel.class);

        homepageTransactionViewModel = new ViewModelProvider(requireActivity(),
                new HomepageTransactionViewModelFactory(requireActivity().getApplication(),
                        getResources().getBoolean(R.bool.debug)))
                .get(HomepageTransactionViewModel.class);

        homepageTransactionViewModel.getTransactions().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                allTransactions = ((Result.Success<List<TransactionEntity>>) result).getData();
                updateDisplayedTransactions();
            }
        });

        currencyListViewModel.getCurrencies().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                baseCurrencyAdapter.clear();
                baseCurrencyAdapter.addAll(((Result.Success<List<CurrencyEntity>>) result).getData());
                baseCurrencyAdapter.notifyDataSetChanged();
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
        CurrencyEntity baseCurrency = (CurrencyEntity) baseCurrencySpinner.getSelectedItem();
        TransactionType selectedCategory = (TransactionType) categorySpinner.getSelectedItem();

        if (baseCurrency == null) {
            adapter.clear();
            adapter.notifyDataSetChanged();
            pieChartExpenses.clear();
            pieChartBalance.clear();
            return;
        }

        if (currentConversionLiveData != null) {
            currentConversionLiveData.removeObservers(getViewLifecycleOwner());
        }

        List<TransactionEntity> filtered = new ArrayList<>();
        for (TransactionEntity t : allTransactions) {
            if (selectedCategory == null || t.getType().equals(selectedCategory.toString())) {
                filtered.add(t);
            }
        }

        adapter.clear();
        adapter.addAll(filtered);
        adapter.notifyDataSetChanged();

        currentConversionLiveData = ratesConversionViewModel.getBasedList(filtered, baseCurrency.getCode());
        currentConversionLiveData.observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                List<TransactionEntity> converted = ((Result.Success<List<TransactionEntity>>) result).getData();

                double balance = 0.0;
                for (TransactionEntity t : converted) balance += t.getAmount();
                balanceTextView.setText(String.format("Saldo: %.2f %s", balance, baseCurrency.getCode()));

                setupBalanceChart(converted);
                setupExpensesChart(converted);

            } else if (result instanceof Result.Error) {
                Toast.makeText(getContext(), "Errore: " + ((Result.Error<?>) result).getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showExpensesChart() {
        pieChartExpenses.setVisibility(View.VISIBLE);
        pieChartBalance.setVisibility(View.GONE);
        btnShowExpenses.setVisibility(View.GONE);
        btnShowBalance.setVisibility(View.VISIBLE);
    }

    private void showBalanceChart() {
        pieChartExpenses.setVisibility(View.GONE);
        pieChartBalance.setVisibility(View.VISIBLE);
        btnShowExpenses.setVisibility(View.VISIBLE);
        btnShowBalance.setVisibility(View.GONE);
    }

    private void setupBalanceChart(List<TransactionEntity> list) {
        float income = 0f, expenses = 0f;
        for (TransactionEntity t : list) {
            if (t.getAmount() >= 0) income += t.getAmount();
            else expenses += Math.abs(t.getAmount());
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry(income, "Entrate"));
        if (expenses > 0) entries.add(new PieEntry(expenses, "Uscite"));

        ArrayList<Integer> colors = new ArrayList<>();
        if (income > 0) colors.add(Color.GREEN);
        if (expenses > 0) colors.add(Color.RED);

        setupPieChartGeneric(pieChartBalance, entries, colors);
    }

    private void setupExpensesChart(List<TransactionEntity> list) {
        // Somma per categoria
        HashMap<String, Float> map = new HashMap<>();
        for (TransactionEntity t : list) {
            if (t.getAmount() < 0) {
                String key = t.getType();
                float value = Math.abs((float) t.getAmount());
                map.put(key, map.getOrDefault(key, 0f) + value);
            }
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        int[] palette = {Color.RED, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.GREEN};

        int i = 0;
        for (String k : map.keySet()) {
            entries.add(new PieEntry(map.get(k), k));
            colors.add(palette[i % palette.length]);
            i++;
        }

        setupPieChartGeneric(pieChartExpenses, entries, colors);
    }

    private void setupPieChartGeneric(PieChart chart, ArrayList<PieEntry> entries, ArrayList<Integer> colors) {
        if (entries.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        // Mostra il valore reale quando tocchi la fetta
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f", value);
            }
        });

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setHoleRadius(45f);
        chart.setTransparentCircleRadius(55f);

        chart.setDrawEntryLabels(false);

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);

        chart.getDescription().setEnabled(false);
        chart.animateY(1200, Easing.EaseInOutQuad);
        chart.invalidate();
    }

    @Override
    public void onDeleteButtonClicked(TransactionEntity transaction) {
        homepageTransactionViewModel.deleteTransaction(transaction.getId());

    }
}
