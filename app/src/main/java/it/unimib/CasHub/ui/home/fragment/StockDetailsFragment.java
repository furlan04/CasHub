package it.unimib.CasHub.ui.home.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.Agency;

public class StockDetailsFragment extends Fragment {

    private static final String ARG_AGENCY = "agency";
    private Agency agency;

    public StockDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            agency = (Agency) getArguments().getSerializable(ARG_AGENCY);
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

        // Trova le view e popola con i dati dell'agency
        TextView stockName = view.findViewById(R.id.StockName);
        TextView exchangeFull = view.findViewById(R.id.textExchangeFull);

        if (agency != null) {
            stockName.setText(agency.getName());
            exchangeFull.setText(agency.getExchangeFullName());
            // Aggiungi altri campi secondo necessità
        }
    }
}