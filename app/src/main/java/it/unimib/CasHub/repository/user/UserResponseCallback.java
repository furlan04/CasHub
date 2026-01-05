package it.unimib.CasHub.repository.user;

import it.unimib.CasHub.model.User;
public interface UserResponseCallback {

    void onSuccessFromAuthentication(User user);
    void onFailureFromAuthentication(String message);
    void onSuccessFromRemoteDatabase(User user);
    void onFailureFromRemoteDatabase(String message);
    void onSuccessLogout();
}
