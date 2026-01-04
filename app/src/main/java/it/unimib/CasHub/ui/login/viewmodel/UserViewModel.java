package it.unimib.CasHub.ui.login.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.User;
import it.unimib.CasHub.repository.transaction.ITransactionRepository;
import it.unimib.CasHub.repository.transaction.TransactionRepository;
import it.unimib.CasHub.repository.user.IUserRepository;

public class UserViewModel extends ViewModel {

    private final IUserRepository userRepository;
    private MutableLiveData<Result> userMutableLiveData;

    private boolean authenticationError;

    public UserViewModel(IUserRepository userRepository) {
        this.userRepository = userRepository;
        authenticationError = false;
    }

    public MutableLiveData<Result> getUserMutableLiveData(
            String name, String email, String password, boolean isUserRegistered) {
        //if (userMutableLiveData == null) {
            getUserData(name, email, password, isUserRegistered);
        //}
        return userMutableLiveData;
    }

    public MutableLiveData<Result> getGoogleUserMutableLiveData(String token) {
        if (userMutableLiveData == null) {
            getUserData(token);
        }
        return userMutableLiveData;
    }

    public User getLoggedUser() {
        return userRepository.getLoggedUser();
    }

    public MutableLiveData<Result> logout() {
        if (userMutableLiveData == null) {
            userMutableLiveData = userRepository.logout();
        } else {
            userRepository.logout();
        }

        return userMutableLiveData;
    }

    public void getUser(String name, String email, String password, boolean isUserRegistered) {
        userRepository.getUser(name, email, password, isUserRegistered);
    }

    public boolean isAuthenticationError() {
        return authenticationError;
    }

    public void setAuthenticationError(boolean authenticationError) {
        this.authenticationError = authenticationError;
    }

    private void getUserData(String name, String email, String password, boolean isUserRegistered) {
        userMutableLiveData = userRepository.getUser(name, email, password, isUserRegistered);
    }

    private void getUserData(String token) {
        userMutableLiveData = userRepository.getGoogleUser(token);
    }
}
