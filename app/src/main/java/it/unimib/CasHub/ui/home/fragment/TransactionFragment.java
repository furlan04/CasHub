package it.unimib.CasHub.ui.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.CurrencySpinnerAdapter;
import it.unimib.CasHub.model.CurrencyEntity;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.model.TransactionType;
import it.unimib.CasHub.repository.ForexRepository;
import it.unimib.CasHub.ui.home.viewmodel.HomepageTransactionViewModel;
import it.unimib.CasHub.ui.home.viewmodel.HomepageTransactionViewModelFactory;
import it.unimib.CasHub.ui.home.viewmodel.TransactionViewModel;
import it.unimib.CasHub.ui.home.viewmodel.TransactionViewModelFactory;
import it.unimib.CasHub.utils.ServiceLocator;

public class TransactionFragment extends Fragment {

    private Spinner spinnerValuta;
    private Spinner spinnerCategoria;
    private CurrencySpinnerAdapter currencyAdapter;
    private TransactionViewModel viewModel;
    private HomepageTransactionViewModel homepageTransactionViewModel;

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
        homepageTransactionViewModel = new ViewModelProvider(this, new HomepageTransactionViewModelFactory(requireActivity().getApplication(), getResources().getBoolean(R.bool.debug))).get(HomepageTransactionViewModel.class);
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
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        Button btnConferma = view.findViewById(R.id.btnConferma);

        List<CurrencyEntity> currencyList = new ArrayList<>();
        currencyAdapter = new CurrencySpinnerAdapter(requireContext(), currencyList);
        spinnerValuta.setAdapter(currencyAdapter);

        ArrayAdapter<TransactionType> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, TransactionType.values());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoryAdapter);

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

        btnConferma.setOnClickListener(v -> {
            saveTransaction(etQuantita, spinnerValuta, spinnerCategoria);
        });
    }

    private void observeCurrencies() {
        viewModel.getCurrencies().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                List<CurrencyEntity> currencies = ((Result.Success<List<CurrencyEntity>>) result).getData();
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

    private void saveTransaction(EditText etQuantita, Spinner spinnerValuta, Spinner spinnerCategoria) {
        String amountString = etQuantita.getText().toString();
        if (amountString.isEmpty()) {
            Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountString);
        CurrencyEntity selectedCurrency = (CurrencyEntity) spinnerValuta.getSelectedItem();
        TransactionType selectedType = (TransactionType) spinnerCategoria.getSelectedItem();

        if (selectedCurrency == null) {
            Toast.makeText(getContext(), "Please select a currency", Toast.LENGTH_SHORT).show();
            return;
        }

        TransactionEntity transaction = new TransactionEntity(amount, selectedType, selectedCurrency.getCode());
        homepageTransactionViewModel.insertTransaction(transaction);

        Toast.makeText(getContext(), "Transaction saved", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    private void showError(String message) {
        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
        currencyAdapter.clear();
        currencyAdapter.notifyDataSetChanged();
    }

    public CurrencyEntity getSelectedCurrency() {
        if (spinnerValuta != null && currencyAdapter != null) {
            int position = spinnerValuta.getSelectedItemPosition();
            if (position >= 0 && position < currencyAdapter.getCount()) {
                return currencyAdapter.getItem(position);
            }
        }
        return null;
    }
}
