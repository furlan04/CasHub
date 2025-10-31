package it.unimib.CasHub.model;

import com.google.firebase.auth.FirebaseUser;
import java.io.Serializable;

public class User implements Serializable {

    private String uid;
    private String displayName;
    private String email;
    public User() {
    }
    public User(String uid, String displayName, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
    }

    public static User fromFirebaseUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return null;
        return new User(
                firebaseUser.getUid(),
                firebaseUser.getDisplayName(),
                firebaseUser.getEmail()
        );
    }

    // Getters e Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}