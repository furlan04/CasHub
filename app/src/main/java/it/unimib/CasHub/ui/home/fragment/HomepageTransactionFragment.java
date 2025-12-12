package it.unimib.CasHub.ui.home.fragment;

import android.graphics.Color;
import android.widget.Button;
import androidx.navigation.Navigation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;

import it.unimib.CasHub.R;

public class HomepageTransactionFragment extends Fragment {

    public HomepageTransactionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 1️INFLATE DEL LAYOUT
        View view = inflater.inflate(R.layout.fragment_homepage_transaction, container, false);

        // PIECHART
        PieChart pieChart = view.findViewById(R.id.pieChartTransaction);

        // DATI DI TEST
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(45f, "Cibo"));
        entries.add(new PieEntry(25f, "Affitto"));
        entries.add(new PieEntry(15f, "Trasporti"));
        entries.add(new PieEntry(15f, "Altro"));

        // COLORI PERSONALIZZATI
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#4CAF50")); // Verde
        colors.add(Color.parseColor("#2196F3")); // Blu
        colors.add(Color.parseColor("#FFC107")); // Giallo
        colors.add(Color.parseColor("#F44336")); // Rosso

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        // CONFIGURAZIONE GRAFICO
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(55f);

        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);

        // Legenda
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);

        //Animazione
        pieChart.animateY(1200, Easing.EaseInOutQuad);

        //SET DEI DATI
        pieChart.setData(data);
        pieChart.invalidate();

        // Bottone per aggiungere transazione
        Button btnAddTransaction = view.findViewById(R.id.btnAddTransaction);
        btnAddTransaction.setOnClickListener(v -> {
            // Naviga verso AddTransactionFragment
            Navigation.findNavController(v)
                    .navigate(R.id.action_homepageTransactionFragment_to_addTransactionFragment);
        });

        return view;
    }
}
