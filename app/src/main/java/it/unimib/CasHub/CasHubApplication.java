package it.unimib.CasHub;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class CasHubApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
