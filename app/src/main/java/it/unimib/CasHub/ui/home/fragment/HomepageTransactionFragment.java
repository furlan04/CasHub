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
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.TransactionRecyclerAdapter;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.ui.home.viewmodel.HomepageTransactionViewModel;
import it.unimib.CasHub.ui.home.viewmodel.HomepageTransactionViewModelFactory;

public class HomepageTransactionFragment extends Fragment implements TransactionRecyclerAdapter.OnDeleteButtonClickListener {

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
        adapter = new TransactionRecyclerAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        boolean debugMode = getResources().getBoolean(R.bool.debug);
        homepageTransactionViewModel = new ViewModelProvider(requireActivity(), new HomepageTransactionViewModelFactory(requireActivity().getApplication(), debugMode)).get(HomepageTransactionViewModel.class);

        homepageTransactionViewModel.getTransactions().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                List<TransactionEntity> transactions = ((Result.Success<List<TransactionEntity>>) result).getData();
                setupPieChart(transactions);
                adapter.clear();
                adapter.addAll(transactions);
                adapter.notifyDataSetChanged();
            } else {
                showError(((Result.Error<?>) result).getMessage());
            }
        });

        Button btnAddTransaction = view.findViewById(R.id.btnAddTransaction);
        btnAddTransaction.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_homepageTransactionFragment_to_transactionFragment);
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
