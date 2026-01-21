package it.unimib.CasHub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.PortfolioStock;

public class SellStockAdapter extends RecyclerView.Adapter<SellStockAdapter.VH> {

    public interface OnStockClickListener {
        void onStockClick(PortfolioStock stock);
    }

    private final List<PortfolioStock> stocks;
    private final OnStockClickListener listener;

    public SellStockAdapter(List<PortfolioStock> stocks, OnStockClickListener listener) {
        this.stocks = stocks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sell_stock, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PortfolioStock stock = stocks.get(position);


        holder.tvName.setText(stock.getName());
        holder.tvMeta.setText(stock.getSymbol() + " • " + stock.getQuantity() + " azioni");

        holder.itemView.setOnClickListener(v -> listener.onStockClick(stock));
    }

    @Override
    public int getItemCount() {
        return stocks == null ? 0 : stocks.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvMeta;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}
