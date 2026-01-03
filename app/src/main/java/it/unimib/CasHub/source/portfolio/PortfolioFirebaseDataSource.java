package it.unimib.CasHub.source.portfolio;

import static it.unimib.CasHub.utils.Constants.*;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.model.PortfolioStock;

public class PortfolioFirebaseDataSource {
    private final DatabaseReference databaseReference;
    private final String TAG = PortfolioFirebaseDataSource.class.getSimpleName();
    private final FirebaseAuth firebaseAuth;
    private IPortfolioCallback callback;

    public PortfolioFirebaseDataSource(IPortfolioCallback callback) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.databaseReference = database.getReference();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.callback = callback;
    }

    public void getPortfolio() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        Log.d(TAG, "userId: " + userId);

        databaseReference.child(FIREBASE_USERS_COLLECTION)
                .child(userId)
                .child("portfolio")  // 👈 NUOVO path
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        List<PortfolioStock> portfolio = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            PortfolioStock stock = ds.getValue(PortfolioStock.class);
                            portfolio.add(stock);
                        }
                        callback.onPortfolioSuccess(portfolio);
                    } else {
                        Log.e(TAG, "Errore nel recupero del portfolio.", task.getException());
                        callback.onPortfolioFailure(task.getException());
                    }
                });
    }

    public void addStock(PortfolioStock stock) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onPortfolioFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = currentUser.getUid();
        String stockId = stock.getSymbol();
        String stockName = stock.getName();

        databaseReference
                .child(FIREBASE_USERS_COLLECTION)
                .child(userId)
                .child("portfolio")
                .child(stockId)
                .setValue(stock)
                .addOnSuccessListener(unused -> Log.i(TAG, "Stock aggiunto: " + stockName))
                .addOnFailureListener(e -> callback.onPortfolioFailure(e));
    }

    public void removeStock(String symbol) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        databaseReference
                .child(FIREBASE_USERS_COLLECTION)
                .child(userId)
                .child("portfolio")
                .child(symbol)
                .removeValue();
    }
}
