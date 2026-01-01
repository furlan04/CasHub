package it.unimib.CasHub.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unimib.CasHub.adapter.TransactionRecyclerAdapter;
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
    public void onTransactionsSuccess(List<TransactionEntity> transactions) {
        transactionsMutableLiveData.postValue(new Result.Success<>(transactions));
    }

    @Override
    public void onTransactionsFailure(Exception exception) {
        transactionsMutableLiveData.postValue(new Result.Error<>(exception.getMessage()));
    }

    @Override
    public void onTransactionInserted() {
        remoteDataSource.getTransactions();
    }
    @Override
    public void onTransactionDeleted() {
        remoteDataSource.getTransactions();
    }
}
