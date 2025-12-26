package it.unimib.CasHub.ui.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.TransactionRecyclerAdapter;
import it.unimib.CasHub.model.CurrencyEntity;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.model.TransactionType;
import it.unimib.CasHub.ui.home.viewmodel.HomepageTransactionViewModel;
import it.unimib.CasHub.ui.home.viewmodel.HomepageTransactionViewModelFactory;

public class HomepageTransactionFragment extends Fragment {

    private PieChart pieChart;
    private RecyclerView recyclerView;
    private TransactionRecyclerAdapter adapter;
    private HomepageTransactionViewModel homepageTransactionViewModel;

    public HomepageTransactionFragment() {
        // Required empty public constructor
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        boolean debugMode = getResources().getBoolean(R.bool.debug);
        homepageTransactionViewModel = new ViewModelProvider(this, new HomepageTransactionViewModelFactory(requireActivity().getApplication(), debugMode)).get(HomepageTransactionViewModel.class);

        homepageTransactionViewModel.getTransactions().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                List<TransactionEntity> transactions = ((Result.Success<List<TransactionEntity>>) result).getData();
                setupPieChart(transactions);
                setupRecyclerView(transactions);
            } else {
                // Handle error
            }
        });

        observeTransactions();

        Button btnAddTransaction = view.findViewById(R.id.btnAddTransaction);
        btnAddTransaction.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_homepageTransactionFragment_to_transactionFragment);
        });
    }

    private void observeTransactions() {
        homepageTransactionViewModel.getTransactions().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                List<TransactionEntity> transactions = ((Result.Success<List<TransactionEntity>>) result).getData();
                if (transactions != null && !transactions.isEmpty()) {
                    adapter.clear();
                    adapter.addAll(transactions);
                    adapter.notifyDataSetChanged();
                } else {
                    showError("No transactions found");
                }
            } else {
                showError(((Result.Error<?>) result).getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    private void setupPieChart(List<TransactionEntity> transactions) {
        Map<TransactionType, Float> categoryTotals = new HashMap<>();
        for (TransactionEntity transaction : transactions) {
            float total = categoryTotals.getOrDefault(transaction.getType(), 0f);
            categoryTotals.put(transaction.getType(), total + (float) transaction.getAmount());
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<TransactionType, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey().toString()));
        }

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#4CAF50"));
        colors.add(Color.parseColor("#2196F3"));
        colors.add(Color.parseColor("#FFC107"));
        colors.add(Color.parseColor("#F44336"));
        colors.add(Color.parseColor("#9C27B0"));
        colors.add(Color.parseColor("#00BCD4"));
        colors.add(Color.parseColor("#FF5722"));
        colors.add(Color.parseColor("#8BC34A"));
        colors.add(Color.parseColor("#607D8B"));
        colors.add(Color.parseColor("#E91E63"));

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

    private void setupRecyclerView(List<TransactionEntity> transactions) {
        adapter = new TransactionRecyclerAdapter(transactions);
        recyclerView.setAdapter(adapter);
    }
}
