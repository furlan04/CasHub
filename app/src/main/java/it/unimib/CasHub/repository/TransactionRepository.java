package it.unimib.CasHub.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.source.transaction.BaseTransactionDataSource;
import it.unimib.CasHub.source.transaction.TransactionCallback;

public class TransactionRepository implements TransactionCallback {

    private final MutableLiveData<Result<List<TransactionEntity>>> transactionsMutableLiveData;

    private final BaseTransactionDataSource localDataSource;
    private final BaseTransactionDataSource remoteDataSource;

    public TransactionRepository(BaseTransactionDataSource localDataSource, BaseTransactionDataSource remoteDataSource) {
        this.transactionsMutableLiveData = new MutableLiveData<>();
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.localDataSource.setCallback(this);
        this.remoteDataSource.setCallback(this);
    }

    public MutableLiveData<Result<List<TransactionEntity>>> getTransactions() {
        remoteDataSource.getTransactions();
        return transactionsMutableLiveData;
    }

    public void insertTransaction(TransactionEntity transaction) {
        remoteDataSource.insertTransaction(transaction);
    }
    public void deleteTransaction(String transactionId) {
        remoteDataSource.deleteTransaction(transactionId);
    }

    @Override
    public void onTransactionsSuccessFromLocal(List<TransactionEntity> transactions) {
        transactionsMutableLiveData.postValue(new Result.Success<>(transactions));
    }

    @Override
    public void onTransactionsFailureFromLocal(Exception exception) {
        transactionsMutableLiveData.postValue(new Result.Error<>(exception.getMessage()));
    }

    @Override
    public void onTransactionsSuccessFromRemote(List<TransactionEntity> transactions) {
        localDataSource.deleteAll();
        for (TransactionEntity transaction : transactions) {
            localDataSource.insertTransaction(transaction);
        }
        transactionsMutableLiveData.postValue(new Result.Success<>(transactions));
    }

    @Override
    public void onTransactionsFailureFromRemote(Exception exception) {
        // If remote fails, get data from local cache
        localDataSource.getTransactions();
    }

    @Override
    public void onTransactionInsertedFromLocal() {
        // Do nothing, to prevent loops
    }

    @Override
    public void onTransactionDeletedFromLocal() {
        // Do nothing, to prevent loops
    }

    @Override
    public void onTransactionInsertedFromRemote() {
        // After a remote insert, refresh the data from remote
        remoteDataSource.getTransactions();
    }

    @Override
    public void onTransactionDeletedFromRemote() {
        // After a remote delete, refresh the data from remote
        remoteDataSource.getTransactions();
    }
}
