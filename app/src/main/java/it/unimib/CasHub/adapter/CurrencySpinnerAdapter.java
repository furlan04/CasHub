package it.unimib.CasHub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.Currency;

public class CurrencySpinnerAdapter extends ArrayAdapter<Currency> {

    private final LayoutInflater inflater;

    public CurrencySpinnerAdapter(@NonNull Context context, @NonNull List<Currency> currencies) {
        super(context, android.R.layout.simple_spinner_item, currencies);
        this.inflater = LayoutInflater.from(context);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        Currency currency = getItem(position);
        if (currency != null) {
            textView.setText(currency.getCode());
        }
        
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        Currency currency = getItem(position);
        if (currency != null) {
            // Mostra codice e nome nel dropdown (es: "EUR - Euro")
            textView.setText(currency.getCode() + " - " + currency.getName());
        }
        
        return view;
    }
}

