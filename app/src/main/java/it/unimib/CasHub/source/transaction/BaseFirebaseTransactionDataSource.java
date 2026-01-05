package it.unimib.CasHub.source.transaction;

import java.util.List;

import it.unimib.CasHub.model.TransactionEntity;

public abstract class BaseFirebaseTransactionDataSource {

    protected TransactionCallback callback;

    public void setCallback(TransactionCallback callback) {
        this.callback = callback;
    }

    public abstract void getTransactions();

    public abstract void saveTransactions(List<TransactionEntity> transaction);

    public abstract void insertTransaction(TransactionEntity transaction);

    public abstract void deleteTransaction(String transactionId);
}
