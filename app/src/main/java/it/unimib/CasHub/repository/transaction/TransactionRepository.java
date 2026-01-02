package it.unimib.CasHub.repository.transaction;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;

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
    private List<TransactionEntity> currentUserCache = new ArrayList<>();
    private boolean isSyncing = false;
    private String userId;

    public TransactionRepository(BaseLocalTransactionDataSource localDataSource,
                                 BaseFirebaseTransactionDataSource remoteDataSource) {
        this.transactionsLiveData = new MutableLiveData<>();
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
                transaction.setUserId(userId);
                localDataSource.insertTransaction(transaction);
            }
        } catch (Exception e) {
            onTransactionsFailure(e);
        }
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
        List<TransactionEntity> currentUserTransactions = new ArrayList<>();
        for (TransactionEntity t : localTransactions) {
            if (t != null && t.getUserId().equals(userId)) {
                currentUserTransactions.add(t);
            }
        }
        syncRemoteToLocal(currentUserTransactions);
    }

    private void handleLocalChange(List<TransactionEntity> transactions) {
        List<TransactionEntity> localList = transactions != null
                ? new ArrayList<>(transactions)
                : new ArrayList<>();

        List<TransactionEntity> localListForUser = new ArrayList<>();

        for (TransactionEntity t : localList) {
            if (t != null && t.getUserId().equals(userId)) {
                localListForUser.add(t);
            }
        }

        if (remoteDataSource != null) {
            try {
                remoteDataSource.saveTransactions(localListForUser);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        transactionsLiveData.postValue(new Result.Success<>(localListForUser));
    }

    private void syncRemoteToLocal(List<TransactionEntity> localTransactions) {
        // Filtra le transazioni locali dell'utente corrente
        currentUserCache = new ArrayList<>();
        for (TransactionEntity t : localTransactions) {
            if (t != null && t.getUserId().equals(userId)) {
                currentUserCache.add(t);
            }
        }

        Set<Integer> localIds = new HashSet<>();
        for (TransactionEntity t : currentUserCache) {
            localIds.add(t.getId());
        }

        Set<Integer> remoteIds = new HashSet<>();
        for (TransactionEntity t : remoteCache) {
            if (t != null) {
                remoteIds.add(t.getId());
            }
        }

        boolean hasChanges = false;

        if (!remoteCache.isEmpty()) {
            // Aggiungi le transazioni remote che non sono presenti localmente
            for (TransactionEntity remote : remoteCache) {
                if (remote != null && !localIds.contains(remote.getId())) {
                    try {
                        remote.setUserId(userId);
                        localDataSource.insertTransaction(remote);
                        currentUserCache.add(remote);
                        hasChanges = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Controlla se ci sono transazioni locali non presenti in remoto
            for (TransactionEntity local : currentUserCache) {
                if (local != null && !remoteIds.contains(local.getId())) {
                    hasChanges = true;
                }
            }
        } else {
            hasChanges = true;
        }

        if (hasChanges && remoteDataSource != null) {
            try {
                remoteDataSource.saveTransactions(currentUserCache);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        isSyncing = false;
        // Ora currentUserCache contiene SOLO le transazioni dell'utente corrente
        transactionsLiveData.postValue(new Result.Success<>(currentUserCache));
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
    public void onTransactionsFailure(Exception exception) {
        isSyncing = false;
        String errorMessage = exception != null
                ? exception.getMessage()
                : "Unknown error occurred";
        transactionsLiveData.postValue(new Result.Error<>(errorMessage));
    }
}