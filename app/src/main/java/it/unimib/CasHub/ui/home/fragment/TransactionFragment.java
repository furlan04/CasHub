package it.unimib.CasHub.ui.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.CurrencySpinnerAdapter;
import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.repository.ForexRepository;
import it.unimib.CasHub.utils.ServiceLocator;
import it.unimib.CasHub.ui.home.viewmodel.TransactionViewModel;
import it.unimib.CasHub.ui.home.viewmodel.TransactionViewModelFactory;

public class TransactionFragment extends Fragment {

    private Spinner spinnerValuta;
    private CurrencySpinnerAdapter currencyAdapter;
    private TransactionViewModel viewModel;

    public TransactionFragment() {
        // Required empty public constructor
    }

    public static TransactionFragment newInstance() {
        return new TransactionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ForexRepository forexRepository = ServiceLocator.getInstance().getForexRepository(requireActivity().getApplication(), getResources().getBoolean(R.bool.debug));
        viewModel = new ViewModelProvider(this, new TransactionViewModelFactory(forexRepository)).get(TransactionViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnEntrata = view.findViewById(R.id.btnEntrata);
        Button btnUscita = view.findViewById(R.id.btnUscita);
        EditText etNome = view.findViewById(R.id.etNome);
        EditText etQuantita = view.findViewById(R.id.etQuantita);
        spinnerValuta = view.findViewById(R.id.spinnerValuta);

        List<Currency> currencyList = new ArrayList<>();
        currencyAdapter = new CurrencySpinnerAdapter(requireContext(), currencyList);
        spinnerValuta.setAdapter(currencyAdapter);

        observeCurrencies();

        btnEntrata.setOnClickListener(v -> {
            btnEntrata.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
            btnEntrata.setTextColor(Color.WHITE);
            btnUscita.setBackgroundColor(Color.parseColor("#DDDDDD"));
            btnUscita.setTextColor(Color.BLACK);
        });

        btnUscita.setOnClickListener(v -> {
            btnUscita.setBackgroundColor(Color.parseColor("#F44336")); // Rosso
            btnUscita.setTextColor(Color.WHITE);
            btnEntrata.setBackgroundColor(Color.parseColor("#DDDDDD"));
            btnEntrata.setTextColor(Color.BLACK);
        });
    }

    private void observeCurrencies() {
        // TODO: Get last update time from SharedPreferences
        viewModel.getCurrencies(0).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                List<Currency> currencies = ((Result.Success<List<Currency>>) result).getData();
                if (currencies != null && !currencies.isEmpty()) {
                    currencyAdapter.clear();
                    currencyAdapter.addAll(currencies);
                    currencyAdapter.notifyDataSetChanged();
                } else {
                    showError("No currencies found");
                }
            } else {
                showError(((Result.Error<?>) result).getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
        // Fallback with default currencies
        List<Currency> defaultCurrencies = new ArrayList<>();
        defaultCurrencies.add(new Currency("EUR", "Euro"));
        defaultCurrencies.add(new Currency("USD", "United States Dollar"));
        currencyAdapter.clear();
        currencyAdapter.addAll(defaultCurrencies);
        currencyAdapter.notifyDataSetChanged();
    }

    public Currency getSelectedCurrency() {
        if (spinnerValuta != null && currencyAdapter != null) {
            int position = spinnerValuta.getSelectedItemPosition();
            if (position >= 0 && position < currencyAdapter.getCount()) {
                return currencyAdapter.getItem(position);
            }
        }
        return null;
    }
}
