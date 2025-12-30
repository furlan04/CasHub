package it.unimib.CasHub.ui.login.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.User;
import it.unimib.CasHub.repository.user.IUserRepository;

public class UserViewModel extends ViewModel {

    private final IUserRepository userRepository;
    private MutableLiveData<Result> userMutableLiveData;

    private boolean authenticationError;

    public UserViewModel(IUserRepository userRepository) {
        this.userRepository = userRepository;
        authenticationError = false;
    }

    public MutableLiveData<Result> getUserMutableLiveDataLogin(
            String email, String password) {
        getUserDataLogin(email, password);
        return userMutableLiveData;
    }
    public MutableLiveData<Result> getUserMutableLiveDataRegistration(
            String name, String email, String password) {
        getUserDataRegistration(name, email, password);
        return userMutableLiveData;
    }

    // Aggiungi questo metodo per "pulire" il risultato nel Fragment dopo aver mostrato l'errore
    public void resetUserMutableLiveData() {
        if (userMutableLiveData != null) {
            userMutableLiveData.setValue(null);
        }
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

    public void getUserRegistration(String name, String email, String password) {
        userRepository.getUserRegistration(name, email, password);
    }


    public boolean isAuthenticationError() {
        return authenticationError;
    }

    public void setAuthenticationError(boolean authenticationError) {
        this.authenticationError = authenticationError;
    }

    private void getUserDataLogin(String email, String password) {
        userMutableLiveData = userRepository.getUserLogin(email, password);
    }
    private void getUserDataRegistration(String name, String email, String password) {
        userMutableLiveData = userRepository.getUserRegistration(name, email, password);
    }

    private void getUserData(String token) {
        userMutableLiveData = userRepository.getGoogleUser(token);
    }
}
