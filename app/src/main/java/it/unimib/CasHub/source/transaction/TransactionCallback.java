package it.unimib.CasHub.source.transaction;

import java.util.List;

import it.unimib.CasHub.model.TransactionEntity;

public interface TransactionCallback {
    void onTransactionsSuccess(List<TransactionEntity> transactions);
    void onTransactionsFailure(Exception exception);
    void onTransactionInserted();
    void onTransactionDeleted();
    void onAllTransactionsDeleted();
}
