package it.unimib.CasHub.ui.home.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.adapter.CurrencySpinnerAdapter;
import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.repository.ForexRepositoryInterface;
import it.unimib.CasHub.service.ServiceLocator;
import it.unimib.CasHub.utils.ResponseCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends Fragment {
    
    private Spinner spinnerValuta;
    private CurrencySpinnerAdapter currencyAdapter;
    private ForexRepositoryInterface forexRepository;
    
    public TransactionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance
     * @return A new instance of fragment TransactionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TransactionFragment newInstance() {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        Button btnEntrata = view.findViewById(R.id.btnEntrata);
        Button btnUscita = view.findViewById(R.id.btnUscita);
        EditText etNome = view.findViewById(R.id.etNome);
        EditText etQuantita = view.findViewById(R.id.etQuantita);
        spinnerValuta = view.findViewById(R.id.spinnerValuta);

        // Inizializza l'adapter con lista vuota
        List<Currency> currencyList = new ArrayList<>();
        currencyAdapter = new CurrencySpinnerAdapter(requireContext(), currencyList);
        spinnerValuta.setAdapter(currencyAdapter);

        // Inizializza il repository dopo che la view è stata creata
        ServiceLocator serviceLocator = ServiceLocator.getInstance();
        if (getContext() != null) {
            forexRepository = serviceLocator.getForexRepository(getContext().getApplicationContext());
        }

        // Carica le valute dal repository
        loadCurrencies();

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

        return view;
    }

    private void loadCurrencies() {
        if (forexRepository == null) {
            // Fallback: usa valute di default se il repository non è disponibile
            List<Currency> defaultCurrencies = new ArrayList<>();
            defaultCurrencies.add(new Currency("EUR", "Euro"));
            defaultCurrencies.add(new Currency("USD", "United States Dollar"));
            defaultCurrencies.add(new Currency("GBP", "British Pound"));
            defaultCurrencies.add(new Currency("JPY", "Japanese Yen"));
            defaultCurrencies.add(new Currency("CHF", "Swiss Franc"));
            
            currencyAdapter.clear();
            currencyAdapter.addAll(defaultCurrencies);
            currencyAdapter.notifyDataSetChanged();
            return;
        }

        forexRepository.getCurrencies(new ResponseCallback() {
            @Override
            public void onCurrencyListSuccess(List<Currency> currencyList, long lastUpdate) {
                // Aggiorna l'adapter sul thread principale
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currencyAdapter.clear();
                        if (currencyList != null && !currencyList.isEmpty()) {
                            currencyAdapter.addAll(currencyList);
                        } else {
                            // Fallback se la lista è vuota
                            currencyAdapter.add(new Currency("EUR", "Euro"));
                            currencyAdapter.add(new Currency("USD", "United States Dollar"));
                        }
                        currencyAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onRatesSuccess(it.unimib.CasHub.model.ForexAPIResponse rates, long lastUpdate) {
                // Non usato in questo contesto
            }

            @Override
            public void onFailure(String errorMessage) {
                // Fallback: usa valute di default in caso di errore
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        List<Currency> defaultCurrencies = new ArrayList<>();
                        defaultCurrencies.add(new Currency("EUR", "Euro"));
                        defaultCurrencies.add(new Currency("USD", "United States Dollar"));
                        defaultCurrencies.add(new Currency("GBP", "British Pound"));
                        defaultCurrencies.add(new Currency("JPY", "Japanese Yen"));
                        
                        currencyAdapter.clear();
                        currencyAdapter.addAll(defaultCurrencies);
                        currencyAdapter.notifyDataSetChanged();
                        
                        Toast.makeText(getContext(), "Errore nel caricamento valute: " + errorMessage, 
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    /**
     * Ottiene la valuta selezionata nello spinner
     * @return La valuta selezionata o null se nessuna è selezionata
     */
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