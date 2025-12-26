package it.unimib.CasHub.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.unimib.CasHub.model.PortfolioStock;

public class PortfolioFirebaseRepository {

    private static final String TAG = PortfolioFirebaseRepository.class.getSimpleName();
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;

    public interface PortfolioCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public PortfolioFirebaseRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.databaseReference = database.getReference();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Aggiunge un titolo al portafoglio dell'utente corrente
     */
    public void addStockToPortfolio(PortfolioStock stock, PortfolioCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("Utente non autenticato");
            return;
        }

        String userId = currentUser.getUid();
        String stockSymbol = stock.getSymbol();

        // Verifica se il titolo esiste già
        checkIfStockExists(userId, stockSymbol, exists -> {
            if (exists) {
                callback.onFailure("Questo titolo è già nel tuo portafoglio");
            } else {
                // Salva il titolo: users/{userId}/portfolio/{symbol}
                databaseReference.child("users")
                        .child(userId)
                        .child("portfolio")
                        .child(stockSymbol)
                        .setValue(stock)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Titolo aggiunto al portafoglio: " + stockSymbol);
                            callback.onSuccess("Titolo aggiunto al portafoglio!");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Errore aggiunta titolo: " + e.getMessage());
                            callback.onFailure("Errore nel salvataggio: " + e.getMessage());
                        });
            }
        });
    }

    /**
     * Verifica se un titolo esiste già nel portafoglio
     */
    private void checkIfStockExists(String userId, String symbol, ExistsCallback callback) {
        databaseReference.child("users")
                .child(userId)
                .child("portfolio")
                .child(symbol)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onResult(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Errore verifica esistenza: " + error.getMessage());
                        callback.onResult(false);
                    }
                });
    }

    /**
     * Rimuove un titolo dal portafoglio
     */
    public void removeStockFromPortfolio(String symbol, PortfolioCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("Utente non autenticato");
            return;
        }

        String userId = currentUser.getUid();

        databaseReference.child("users")
                .child(userId)
                .child("portfolio")
                .child(symbol)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Titolo rimosso dal portafoglio: " + symbol);
                    callback.onSuccess("Titolo rimosso dal portafoglio");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Errore rimozione titolo: " + e.getMessage());
                    callback.onFailure("Errore nella rimozione: " + e.getMessage());
                });
    }

    private interface ExistsCallback {
        void onResult(boolean exists);
    }
}
