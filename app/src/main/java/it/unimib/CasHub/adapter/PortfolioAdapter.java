package it.unimib.CasHub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import it.unimib.CasHub.R;
import it.unimib.CasHub.model.PortfolioStock;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

    private List<PortfolioStock> stocks = new ArrayList<>();
    private OnStockClickListener listener;

    public interface OnStockClickListener {
        void onStockClick(PortfolioStock stock);
    }

    public PortfolioAdapter(OnStockClickListener listener) {
        this.listener = listener;
    }

    public void setStocks(List<PortfolioStock> stocks) {
        this.stocks = stocks;
        notifyDataSetChanged();
    }

    public List<PortfolioStock> getStocks() {
        return stocks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_portfolio_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PortfolioStock stock = stocks.get(position);
        holder.bind(stock);
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCompanyName, tvCurrentValue, tvProfitLoss;
        TextView tvQuantity, tvAveragePrice;

        ViewHolder(View itemView) {
            super(itemView);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvCurrentValue = itemView.findViewById(R.id.tvCurrentValue);
            tvProfitLoss = itemView.findViewById(R.id.tvProfitLoss);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvAveragePrice = itemView.findViewById(R.id.tvAveragePrice);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onStockClick(stocks.get(getAdapterPosition()));
                }
            });
        }

        void bind(PortfolioStock stock) {
            tvCompanyName.setText(stock.getName());
            tvQuantity.setText(String.format(Locale.US, "%.2f", stock.getQuantity()));
            tvAveragePrice.setText(String.format(Locale.US, "%s %.2f",
                    stock.getCurrency(), stock.getAveragePrice()));

            double currentPrice = stock.getAveragePrice() > 0 ? stock.getAveragePrice() : stock.getAveragePrice();
            double totalValue = currentPrice * stock.getQuantity();
            double totalCost = stock.getAveragePrice() * stock.getQuantity();
            double profitLoss = totalValue - totalCost;
            double profitLossPercent = (profitLoss / totalCost) * 100;

            tvCurrentValue.setText(String.format(Locale.US, "%s %.2f",
                    stock.getCurrency(), totalValue));

            String plText = String.format(Locale.US, "%s%.2f (%+.2f%%)",
                    stock.getCurrency(), profitLoss, profitLossPercent);
            tvProfitLoss.setText(plText);

            if (profitLoss >= 0) {
                tvProfitLoss.setTextColor(0xFF4CAF50);
            } else {
                tvProfitLoss.setTextColor(0xFFF44336);
            }
        }
    }

}
