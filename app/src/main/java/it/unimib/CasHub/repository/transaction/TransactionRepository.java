package it.unimib.CasHub.repository.transaction;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.source.transaction.BaseFirebaseTransactionDataSource;
import it.unimib.CasHub.source.transaction.BaseLocalTransactionDataSource;
import it.unimib.CasHub.source.transaction.TransactionCallback;

public class TransactionRepository implements TransactionCallback, ITransactionRepository {

    private final MutableLiveData<Result<List<TransactionEntity>>> transactionsLiveData;
    private final BaseLocalTransactionDataSource localDataSource;
    private final BaseFirebaseTransactionDataSource remoteDataSource;

    private enum State {
        FETCH_REMOTE,
        FETCH_LOCAL,
        LOCAL_CHANGE,
        SYNCING
    }

    private State state = State.FETCH_REMOTE;
    private List<TransactionEntity> remoteCache = new ArrayList<>();
    private boolean isSyncing = false;

    public TransactionRepository(BaseLocalTransactionDataSource localDataSource,
                                 BaseFirebaseTransactionDataSource remoteDataSource) {
        this.transactionsLiveData = new MutableLiveData<>();
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;

        if (localDataSource != null) {
            localDataSource.setCallback(this);
        }
        if (remoteDataSource != null) {
            remoteDataSource.setCallback(this);
        }
    }

    @Override
    public MutableLiveData<Result<List<TransactionEntity>>> getTransactions() {
        if (isSyncing) {
            return transactionsLiveData;
        }

        isSyncing = true;
        state = State.FETCH_REMOTE;

        try {
            if (remoteDataSource != null) {
                remoteDataSource.getTransactions();
            } else {
                onTransactionsFailure(new Exception("Remote data source is null"));
            }
        } catch (Exception e) {
            onTransactionsFailure(e);
        }

        return transactionsLiveData;
    }
    @Override
    public void insertTransaction(TransactionEntity transaction) {
        if (transaction == null) {
            return;
        }

        state = State.LOCAL_CHANGE;

        try {
            if (localDataSource != null) {
                localDataSource.insertTransaction(transaction);
            }
        } catch (Exception e) {
            onTransactionsFailure(e);
        }
    }
    @Override
    public void deleteAll(){
        localDataSource.deleteAllTransactions();
        onAllTransactionsDeleted();
    }
    @Override
    public void deleteTransaction(int transactionId) {
        if (transactionId < 0) {
            return;
        }

        state = State.LOCAL_CHANGE;

        try {
            if (localDataSource != null) {
                localDataSource.deleteTransaction(transactionId);
            }
        } catch (Exception e) {
            onTransactionsFailure(e);
        }
    }

    @Override
    public void onTransactionsSuccess(List<TransactionEntity> transactions) {
        try {
            switch (state) {
                case FETCH_REMOTE:
                    handleRemoteFetch(transactions);
                    break;

                case FETCH_LOCAL:
                    handleLocalFetch(transactions);
                    break;

                case LOCAL_CHANGE:
                    handleLocalChange(transactions);
                    break;

                case SYNCING:
                    break;
            }
        } catch (Exception e) {
            onTransactionsFailure(e);
        }
    }

    private void handleRemoteFetch(List<TransactionEntity> transactions) {
        remoteCache = transactions != null ? new ArrayList<>(transactions) : new ArrayList<>();
        state = State.FETCH_LOCAL;

        if (localDataSource != null) {
            localDataSource.getTransactions();
        } else {
            isSyncing = false;
            transactionsLiveData.postValue(new Result.Success<>(remoteCache));
        }
    }

    private void handleLocalFetch(List<TransactionEntity> localTransactions) {
        state = State.SYNCING;
        syncRemoteToLocal(localTransactions);
    }

    private void handleLocalChange(List<TransactionEntity> transactions) {
        List<TransactionEntity> localList = transactions != null
                ? new ArrayList<>(transactions)
                : new ArrayList<>();

        if (remoteDataSource != null) {
            try {
                remoteDataSource.saveTransactions(localList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        transactionsLiveData.postValue(new Result.Success<>(localList));
    }

    private void syncRemoteToLocal(List<TransactionEntity> localTransactions) {
        deleteAll();

        List<TransactionEntity> remote = new ArrayList<>(remoteCache);

        for (TransactionEntity t : remote) {
            if (t != null) {
                insertTransaction(t);
            }
        }

//        List<TransactionEntity> localList = localTransactions != null
//                ? new ArrayList<>(localTransactions)
//                : new ArrayList<>();
//
//        Set<Integer> localIds = new HashSet<>();
//        for (TransactionEntity t : localList) {
//            if (t != null) {
//                localIds.add(t.getId());
//            }
//        }
//
//        Set<Integer> remoteIds = new HashSet<>();
//        for (TransactionEntity t : remoteCache) {
//            if (t != null) {
//                remoteIds.add(t.getId());
//            }
//        }
//
//
//        boolean hasChanges = false;
//        if (!remoteCache.isEmpty()) {
//            for (TransactionEntity remote : remoteCache) {
//                if (remote != null
//                        && !localIds.contains(remote.getId())) {
//                    try {
//                        localDataSource.insertTransaction(remote);
//                        localList.add(remote);
//                        hasChanges = true;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            for (TransactionEntity local : localList) {
//                if (local != null
//                        && !remoteIds.contains(local.getId())) {
//                    hasChanges = true;
//                }
//            }
//        } else {
//            hasChanges = true;
//        }
//
//        if (hasChanges && remoteDataSource != null) {
//            try {
//                remoteDataSource.saveTransactions(localList);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        isSyncing = false;
        transactionsLiveData.postValue(new Result.Success<>(remote));
    }

    @Override
    public void onTransactionInserted() {
        if (localDataSource != null) {
            try {
                localDataSource.getTransactions();
            } catch (Exception e) {
                onTransactionsFailure(e);
            }
        }
    }

    @Override
    public void onTransactionDeleted() {
        if (localDataSource != null) {
            try {
                localDataSource.getTransactions();
            } catch (Exception e) {
                onTransactionsFailure(e);
            }
        }
    }

    @Override
    public void onAllTransactionsDeleted() {
        transactionsLiveData.postValue(new Result.Success<>(new ArrayList<>()));
    }

    @Override
    public void onTransactionsFailure(Exception exception) {
        isSyncing = false;
        String errorMessage = exception != null
                ? exception.getMessage()
                : "Unknown error occurred";
        transactionsLiveData.postValue(new Result.Error<>(errorMessage));
    }
}