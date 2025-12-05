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

import it.unimib.CasHub.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends Fragment {
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
        Spinner spinnerValuta = view.findViewById(R.id.spinnerValuta);

        String[] valute = {"€", "$", "£", "CHF", "¥", "C$"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                valute
        );
        spinnerValuta.setAdapter(adapter);

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
}