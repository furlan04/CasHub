package it.unimib.CasHub.repository.transaction;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;

public interface ITransactionRepository {
    MutableLiveData<Result<List<TransactionEntity>>> getTransactions();
    void insertTransaction(TransactionEntity transaction);
    void deleteTransaction(String transactionId);
}
