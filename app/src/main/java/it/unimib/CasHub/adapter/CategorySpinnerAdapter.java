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

import it.unimib.CasHub.model.TransactionType;

public class CategorySpinnerAdapter extends ArrayAdapter<TransactionType> {

    public CategorySpinnerAdapter(@NonNull Context context, @NonNull List<TransactionType> categories) {
        super(context, 0, categories);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        TransactionType category = getItem(position);
        if (category != null) {
            textView.setText(category.name());
        } else {
            textView.setText("Qualunque");
        }

        return convertView;
    }
}
