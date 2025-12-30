package it.unimib.CasHub.source.user;

import java.util.Set;

import it.unimib.CasHub.model.User;
import it.unimib.CasHub.repository.user.UserResponseCallback;

public abstract class BaseUserDataRemoteDataSource {
    protected UserResponseCallback userResponseCallback;

    public void setUserResponseCallback(UserResponseCallback userResponseCallback) {
        this.userResponseCallback = userResponseCallback;
    }

    public abstract void saveUserData(User user);
}
