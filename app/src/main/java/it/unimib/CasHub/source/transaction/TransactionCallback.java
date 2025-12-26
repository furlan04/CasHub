package it.unimib.CasHub.source.transaction;

import java.util.List;

import it.unimib.CasHub.model.TransactionEntity;

public interface TransactionCallback {
    void onTransactionsSuccessFromLocal(List<TransactionEntity> transactions);
    void onTransactionsFailureFromLocal(Exception exception);
    void onTransactionsFailureFromRemote(Exception exception);
    void onTransactionInserted();
}
