package it.unimib.CasHub.repository.transaction;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.source.transaction.BaseFirebaseTransactionDataSource;
import it.unimib.CasHub.source.transaction.BaseLocalTransactionDataSource;
import it.unimib.CasHub.source.transaction.TransactionCallback;

public class TransactionRepository implements TransactionCallback, ITransactionRepository {

    private final MutableLiveData<Result<List<TransactionEntity>>> transactionsLiveData;
    private final BaseFirebaseTransactionDataSource remoteDataSource;
    private final String userId;

    public TransactionRepository(BaseLocalTransactionDataSource localDataSource,
                                 BaseFirebaseTransactionDataSource remoteDataSource) {
        this.transactionsLiveData = new MutableLiveData<>();
        this.remoteDataSource = remoteDataSource;
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (remoteDataSource != null) {
            remoteDataSource.setCallback(this);
        }
    }

    @Override
    public MutableLiveData<Result<List<TransactionEntity>>> getTransactions() {
        if (remoteDataSource != null) {
            remoteDataSource.getTransactions();
        } else {
            onTransactionsFailure(new Exception("Remote data source is null"));
        }
        return transactionsLiveData;
    }

    @Override
    public void insertTransaction(TransactionEntity transaction) {
        if (transaction != null) {
            transaction.setUserId(userId);
            if (remoteDataSource != null) {
                remoteDataSource.insertTransaction(transaction);
            }
        }
    }

    @Override
    public void deleteTransaction(String transactionId) {
        if (remoteDataSource != null) {
            remoteDataSource.deleteTransaction(transactionId);
        }
    }

    @Override
    public void onTransactionsSuccess(List<TransactionEntity> transactions) {
        List<TransactionEntity> userTransactions = new ArrayList<>();
        if (transactions != null) {
            for (TransactionEntity t : transactions) {
                if (t != null && userId.equals(t.getUserId())) {
                    userTransactions.add(t);
                }
            }
        }
        transactionsLiveData.postValue(new Result.Success<>(userTransactions));
    }

    @Override
    public void onTransactionInserted() {
        if (remoteDataSource != null) {
            remoteDataSource.getTransactions();
        }
    }

    @Override
    public void onTransactionDeleted() {
        if (remoteDataSource != null) {
            remoteDataSource.getTransactions();
        }
    }

    @Override
    public void onTransactionsFailure(Exception exception) {
        String errorMessage = exception != null
                ? exception.getMessage()
                : "Unknown error occurred";
        transactionsLiveData.postValue(new Result.Error<>(errorMessage));
    }
}
