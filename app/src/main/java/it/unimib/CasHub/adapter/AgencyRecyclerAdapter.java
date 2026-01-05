package it.unimib.CasHub.adapter;

import static android.view.View.INVISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.Agency;

public class AgencyRecyclerAdapter extends RecyclerView.Adapter<AgencyRecyclerAdapter.ViewHolder> {

    private final List<Agency> agencyList;
    private OnAgencyClickListener listener;

    // Interfaccia per il click
    public interface OnAgencyClickListener {
        void onAgencyClick(Agency agency);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView companyNameTextView;
        private final TextView textExchangeFullTextView;

        public ViewHolder(View view) {
            super(view);
            companyNameTextView = (TextView) view.findViewById(R.id.textCompanyName);
            textExchangeFullTextView = (TextView) view.findViewById(R.id.textExchangeFull);

        }
        public TextView getCompanyNameTextView() {
            return companyNameTextView;

        }
        public TextView getTextExchangeFullTextView() {
            return textExchangeFullTextView;
        }
    }

    public AgencyRecyclerAdapter(List<Agency> agencyList, OnAgencyClickListener listener) {

        this.agencyList = agencyList;
        this.listener = listener;
    }

    public void setOnAgencyClickListener(OnAgencyClickListener listener) {
        this.listener = listener;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_agency, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Agency agency = agencyList.get(position);
        viewHolder.companyNameTextView.setText(agency.getName());
        viewHolder.textExchangeFullTextView.setText(agency.getExchangeFullName());

        // Gestisci il click sulla card
        viewHolder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAgencyClick(agency);
            }
        });
    }

    @Override
    public int getItemCount() {
        return agencyList.size();
    }

}