package it.unimib.CasHub.source.transaction;

import it.unimib.CasHub.model.TransactionEntity;

public class TransactionAPIDataSource extends BaseTransactionDataSource {

    @Override
    public void getTransactions() {
        // For now, we don't have a remote source for transactions
        // so we call the failure callback.
        callback.onTransactionsFailureFromRemote(new Exception("Remote transaction source not implemented yet."));
    }

    @Override
    public void insertTransaction(TransactionEntity transaction) {
        // For now, we don't have a remote source for transactions
        // so we call the failure callback.
        callback.onTransactionsFailureFromRemote(new Exception("Remote transaction source not implemented yet."));
    }
}
