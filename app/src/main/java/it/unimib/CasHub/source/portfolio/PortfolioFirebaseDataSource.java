package it.unimib.CasHub.source.portfolio;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import it.unimib.CasHub.model.PortfolioStock;

public class PortfolioFirebaseDataSource extends BasePortfolioDataSource {

    private static final String TAG = PortfolioFirebaseDataSource.class.getSimpleName();
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;

    public PortfolioFirebaseDataSource() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.databaseReference = database.getReference();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void getPortfolio() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onPortfolioFailure(new Exception("User not authenticated"));
            return;
        }

        databaseReference.child("users")
                .child(currentUser.getUid())
                .child("portfolio")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<PortfolioStock> portfolio = new ArrayList<>();
                        for (DataSnapshot stockSnapshot : snapshot.getChildren()) {
                            PortfolioStock stock = stockSnapshot.getValue(PortfolioStock.class);
                            portfolio.add(stock);
                        }
                        callback.onPortfolioSuccess(portfolio);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onPortfolioFailure(error.toException());
                    }
                });
    }

    @Override
    public void savePortfolioSnapshot(double totalValue) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference historyRef = databaseReference
                .child("users")
                .child(currentUser.getUid())
                .child("portfolioHistory");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateKey = dateFormat.format(new Date());

        historyRef.child(dateKey).setValue(totalValue)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Snapshot saved: " + dateKey + " = " + totalValue))
                .addOnFailureListener(e -> callback.onPortfolioFailure(e));
    }

    @Override
    public void removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onPortfolioFailure(new Exception("User not authenticated"));
            return;
        }

        String safeSymbol = getSafeSymbol(stock.getSymbol());

        DatabaseReference stockRef = databaseReference
                .child("users")
                .child(currentUser.getUid())
                .child("portfolio")
                .child(safeSymbol);

        double newQuantity = stock.getQuantity() - quantityToRemove;

        if (newQuantity <= 0) {
            stockRef.removeValue()
                    .addOnSuccessListener(aVoid -> callback.onStockRemoved())
                    .addOnFailureListener(e -> callback.onPortfolioFailure(e));
        } else {
            stock.setQuantity(newQuantity);
            stockRef.setValue(stock)
                    .addOnSuccessListener(aVoid -> callback.onStockUpdated())
                    .addOnFailureListener(e -> callback.onPortfolioFailure(e));
        }
    }

    @Override
    public void getPortfolioHistory() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onPortfolioFailure(new Exception("User not authenticated"));
            return;
        }

        databaseReference.child("users")
                .child(currentUser.getUid())
                .child("portfolioHistory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<DataSnapshot> history = new ArrayList<>();
                        for(DataSnapshot data : snapshot.getChildren()) {
                            history.add(data);
                        }
                        callback.onHistorySuccess(history);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onPortfolioFailure(error.toException());
                    }
                });
    }

    @Override
    public void addStockToPortfolio(PortfolioStock newPurchase) {
        try {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) {
                callback.onPortfolioFailure(new Exception("User not authenticated"));
                return;
            }

            String safeSymbol = getSafeSymbol(newPurchase.getSymbol());

            DatabaseReference stockRef = databaseReference
                    .child("users")
                    .child(currentUser.getUid())
                    .child("portfolio")
                    .child(safeSymbol);

            stockRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        PortfolioStock existingStock = snapshot.getValue(PortfolioStock.class);
                        if (existingStock != null) {
                            double newQuantityToAdd = newPurchase.getQuantity();
                            double priceOfNewPurchase = newPurchase.getAveragePrice();

                            double oldQuantity = existingStock.getQuantity();
                            double oldAvgPrice = existingStock.getAveragePrice();

                            double totalQuantity = oldQuantity + newQuantityToAdd;
                            double newAvgPrice = ((oldAvgPrice * oldQuantity) + (priceOfNewPurchase * newQuantityToAdd)) / totalQuantity;

                            existingStock.setQuantity(totalQuantity);
                            existingStock.setAveragePrice(newAvgPrice);

                            stockRef.setValue(existingStock)
                                    .addOnSuccessListener(aVoid -> callback.onStockUpdated())
                                    .addOnFailureListener(e -> callback.onPortfolioFailure(e));
                        } else {
                            stockRef.setValue(newPurchase)
                                    .addOnSuccessListener(aVoid -> callback.onStockAdded())
                                    .addOnFailureListener(e -> callback.onPortfolioFailure(e));
                        }
                    } else {
                        stockRef.setValue(newPurchase)
                                .addOnSuccessListener(aVoid -> callback.onStockAdded())
                                .addOnFailureListener(e -> callback.onPortfolioFailure(e));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onPortfolioFailure(error.toException());
                }
            });
        }catch (Exception e){
            callback.onPortfolioFailure(e);
        }
    }

    @Override
    public void updateStockInPortfolio(PortfolioStock stock) {
        try {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) return;

            String safeSymbol = getSafeSymbol(stock.getSymbol());

            databaseReference
                    .child("users")
                    .child(currentUser.getUid())
                    .child("portfolio")
                    .child(safeSymbol)
                    .setValue(stock)
                    .addOnSuccessListener(aVoid -> callback.onStockUpdated())
                    .addOnFailureListener(e -> callback.onPortfolioFailure(e));
        } catch (Exception e) {
            callback.onPortfolioFailure(e);
        }
    }

    private String getSafeSymbol(String symbol) {
        return symbol.replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_");
    }
}
