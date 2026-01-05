package it.unimib.CasHub.ui.home.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.repository.transaction.TransactionRepository;

public class HomepageTransactionViewModel extends ViewModel {

    private final TransactionRepository transactionRepository;
    private MutableLiveData<Result<List<TransactionEntity>>> transactionsLiveData;

    public HomepageTransactionViewModel(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public MutableLiveData<Result<List<TransactionEntity>>> getTransactions() {
        if (transactionsLiveData == null) {
            transactionsLiveData = transactionRepository.getTransactions();
        }
        return transactionsLiveData;
    }

    public void insertTransaction(TransactionEntity transaction) {
        transactionRepository.insertTransaction(transaction);
    }
    public void deleteTransaction(String transactionId) {
        transactionRepository.deleteTransaction(transactionId);
    }
}
