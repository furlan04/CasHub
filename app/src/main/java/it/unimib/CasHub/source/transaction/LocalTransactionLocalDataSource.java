package it.unimib.CasHub.source.transaction;

import java.util.List;

import it.unimib.CasHub.database.TransactionDao;
import it.unimib.CasHub.database.TransactionRoomDatabase;
import it.unimib.CasHub.model.TransactionEntity;

public class LocalTransactionLocalDataSource extends BaseLocalTransactionDataSource {

    private final TransactionDao transactionDao;

    public LocalTransactionLocalDataSource(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    @Override
    public void getTransactions() {
        TransactionRoomDatabase.databaseWriteExecutor.execute(() -> {
            List<TransactionEntity> transactions = transactionDao.getAllTransactions();
            callback.onTransactionsSuccess(transactions);
        });
    }

    @Override
    public void insertTransaction(TransactionEntity transaction) {
        TransactionRoomDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.insertTransaction(transaction);
            callback.onTransactionInserted();
        });
    }
    @Override
    public void deleteTransaction(int transactionId) {
        TransactionRoomDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.deleteTransaction(transactionId);
            callback.onTransactionDeleted();
        });
    }
}
