package it.unimib.CasHub.ui.login.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.CasHub.repository.transaction.ITransactionRepository;
import it.unimib.CasHub.repository.transaction.TransactionRepository;
import it.unimib.CasHub.repository.user.IUserRepository;

/**
 * Custom ViewModelProvider to be able to have a custom constructor
 * for the UserViewModel class.
 */
public class UserViewModelFactory implements ViewModelProvider.Factory {

    private final IUserRepository userRepository;
    private final ITransactionRepository transactionRepository;


    public UserViewModelFactory(IUserRepository userRepository, ITransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UserViewModel(userRepository, transactionRepository);
    }
}
