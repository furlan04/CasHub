package it.unimib.CasHub.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.TransactionEntity;

public class TransactionRecyclerAdapter extends RecyclerView.Adapter<TransactionRecyclerAdapter.TransactionViewHolder> {

    private final List<TransactionEntity> transactionList;
    private final OnDeleteButtonClickListener onDeleteButtonClickListener;

    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClicked(TransactionEntity transaction);
    }

    public TransactionRecyclerAdapter(List<TransactionEntity> transactionList, OnDeleteButtonClickListener onDeleteButtonClickListener) {
        this.transactionList = transactionList;
        this.onDeleteButtonClickListener = onDeleteButtonClickListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity transaction = transactionList.get(position);
        holder.bind(transaction, onDeleteButtonClickListener);
    }

    public void clear() {
        transactionList.clear();
    }

    public void addAll(List<TransactionEntity> transactions) {
        transactionList.addAll(transactions);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final TextView amountTextView;
        private final TextView typeTextView;
        private final TextView currencyTextView;
        private final Button deleteButton;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            amountTextView = itemView.findViewById(R.id.textViewAmount);
            typeTextView = itemView.findViewById(R.id.textViewType);
            currencyTextView = itemView.findViewById(R.id.textViewCurrency);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(TransactionEntity transaction, OnDeleteButtonClickListener listener) {
            nameTextView.setText(transaction.getName());
            amountTextView.setText(String.valueOf(transaction.getAmount()));
            if (transaction.getAmount() < 0) {
                amountTextView.setTextColor(Color.RED);
            } else {
                amountTextView.setTextColor(Color.GREEN);
            }
            typeTextView.setText(transaction.getType().toString());
            currencyTextView.setText(transaction.getCurrency());
            deleteButton.setOnClickListener(v -> listener.onDeleteButtonClicked(transaction));
        }
    }
}
