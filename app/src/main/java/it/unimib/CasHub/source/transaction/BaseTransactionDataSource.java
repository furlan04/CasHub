package it.unimib.CasHub.source.transaction;

import it.unimib.CasHub.model.TransactionEntity;

public abstract class BaseTransactionDataSource {

    protected TransactionCallback callback;

    public void setCallback(TransactionCallback callback) {
        this.callback = callback;
    }

    public abstract void getTransactions();

    public abstract void insertTransaction(TransactionEntity transaction);
    public abstract void deleteTransaction(String transactionId);
}
