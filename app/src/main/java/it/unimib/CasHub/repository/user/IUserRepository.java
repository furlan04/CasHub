package it.unimib.CasHub.repository.user;

import androidx.lifecycle.MutableLiveData;

import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.User;


import java.util.Set;

public interface IUserRepository {
    MutableLiveData<Result> getUserLogin(String email, String password);
    MutableLiveData<Result> getUserRegistration(String name, String email, String password);
    MutableLiveData<Result> getGoogleUser(String idToken);
    MutableLiveData<Result> logout();
    User getLoggedUser();
    void signUp(String name, String email, String password);
    void signIn(String email, String password);
    void signInWithGoogle(String token);
}
