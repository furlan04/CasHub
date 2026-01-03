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

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.model.PortfolioStock;

public class PortfolioFirebaseRepository {

    private static final String TAG = PortfolioFirebaseRepository.class.getSimpleName();
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;


    public interface PortfolioListCallback {
        void onPortfolioListSuccess(List<PortfolioStock> portfolio);
        void onPortfolioListFailure(String errorMessage);
    }

    public interface PortfolioCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }



    public PortfolioFirebaseRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.databaseReference = database.getReference();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public void testMethod() {
        Log.e(TAG, "🔥🔥🔥 TEST METHOD CHIAMATO 🔥🔥🔥");
    }
    public void addStockToPortfolio(PortfolioStock stock, PortfolioCallback callback) {
        Log.d(TAG, "🟢 REPO: Entrato in addStockToPortfolio");

        try {
            Log.d(TAG, "🟢 firebaseAuth: " + (firebaseAuth != null ? "OK" : "NULL"));

            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            Log.d(TAG, "🟢 currentUser: " + (currentUser != null ? "OK" : "NULL"));

            if (currentUser == null) {
                Log.e(TAG, "🔴 User NULL!");
                callback.onFailure("Utente non autenticato");
                return;
            }

            String userId = currentUser.getUid();
            String stockSymbol = stock.getSymbol();

            Log.d(TAG, "🟢 userId: " + userId);
            Log.d(TAG, "🟢 stockSymbol: " + stockSymbol);

            String safeSymbol = stockSymbol.replace(".", "_")
                    .replace("#", "_")
                    .replace("$", "_")
                    .replace("[", "_")
                    .replace("]", "_");

            Log.d(TAG, "Symbol originale: " + stockSymbol);
            Log.d(TAG, "Symbol safe: " + safeSymbol);

            Log.d(TAG, "🟢 databaseReference: " + (databaseReference != null ? "OK" : "NULL"));

            databaseReference.child("users")
                    .child(userId)
                    .child("portfolio")
                    .child(safeSymbol)
                    .setValue(stock)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Stock salvato su Firebase!");
                        callback.onSuccess("Titolo aggiunto al portafoglio!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Firebase Errore: " + e.getMessage());
                        e.printStackTrace();
                        callback.onFailure("Errore: " + e.getMessage());
                    });

            Log.d(TAG, "🟢 setValue chiamato (asincrono)");

        } catch (Exception e) {
            Log.e(TAG, "💥 EXCEPTION in addStockToPortfolio: " + e.getMessage());
            e.printStackTrace();
            callback.onFailure("Errore: " + e.getMessage());
        }
    }





    private void updateStockQuantity(String userId, String symbol, PortfolioStock newStock, PortfolioCallback callback) {
        databaseReference.child("users")
                .child(userId)
                .child("portfolio")
                .child(symbol)
                .get()
                .addOnSuccessListener(snapshot -> {
                    PortfolioStock existingStock = snapshot.getValue(PortfolioStock.class);
                    if (existingStock != null) {
                        // Somma quantità
                        double newQty = existingStock.getQuantity() + newStock.getQuantity();
                        existingStock.setQuantity(newQty);

                        // Ricalcola prezzo medio
                        double totalValue = (existingStock.getAveragePrice() * existingStock.getQuantity())
                                + (newStock.getAveragePrice() * newStock.getQuantity());
                        existingStock.setAveragePrice(totalValue / newQty);

                        // Salva
                        databaseReference.child("users")
                                .child(userId)
                                .child("portfolio")
                                .child(symbol)
                                .setValue(existingStock)
                                .addOnSuccessListener(v -> callback.onSuccess("Quantità aggiornata!"))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    }
                });
    }



    public void getPortfolioList(PortfolioListCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onPortfolioListFailure("Utente non autenticato");
            return;
        }

        String userId = currentUser.getUid();

        databaseReference.child("users")
                .child(userId)
                .child("portfolio")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<PortfolioStock> portfolio = new ArrayList<>();
                        for (DataSnapshot stockSnapshot : snapshot.getChildren()) {
                            PortfolioStock stock = stockSnapshot.getValue(PortfolioStock.class);
                            if (stock != null) {
                                portfolio.add(stock);
                            }
                        }
                        callback.onPortfolioListSuccess(portfolio);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Errore get portfolio: " + error.getMessage());
                        callback.onPortfolioListFailure(error.getMessage());
                    }
                });
    }


    public void removeStockFromPortfolio(String symbol, PortfolioCallback callback) {

    }


    private void checkIfStockExists(String userId, String symbol, ExistsCallback callback) {

    }

    private interface ExistsCallback {
        void onResult(boolean exists);
    }
}
