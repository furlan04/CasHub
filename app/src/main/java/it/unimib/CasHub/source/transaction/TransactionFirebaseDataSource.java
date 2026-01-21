package it.unimib.CasHub.source.transaction;

import static it.unimib.CasHub.utils.Constants.*;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.model.TransactionEntity;



public class TransactionFirebaseDataSource extends BaseFirebaseTransactionDataSource {

    private final DatabaseReference databaseReference;
    private final String TAG = TransactionFirebaseDataSource.class.getSimpleName();
    private final FirebaseAuth firebaseAuth;

    public TransactionFirebaseDataSource() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.databaseReference = database.getReference();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void getTransactions() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        databaseReference
                .child(FIREBASE_USERS_COLLECTION)
                .child(userId)
                .child(FIREBASE_TRANSACTIONS_COLLECTION)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<TransactionEntity> transactions = new ArrayList<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            TransactionEntity transaction = ds.getValue(TransactionEntity.class);
                            if (transaction != null) {
                                transaction.setFirebaseKey(ds.getKey());
                                transactions.add(transaction);
                            }
                        }

                        callback.onTransactionsSuccess(transactions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onTransactionsFailure(error.toException());
                    }
                });
    }

    @Override
    public void saveTransactions(List<TransactionEntity> transaction) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onTransactionsFailure(new Exception("User not authenticated"));
            return;
        }

        String userId = currentUser.getUid();

        databaseReference
                .child(FIREBASE_USERS_COLLECTION)
                .child(userId)
                .child(FIREBASE_TRANSACTIONS_COLLECTION)
                .setValue(new ArrayList<>(transaction)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onTransactionInserted();
                    }
                });

    }

    @Override
    public void insertTransaction(TransactionEntity transaction) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onTransactionsFailure(new Exception("User not authenticated"));
            return;
        }
        String userId = currentUser.getUid();
        databaseReference
                .child(FIREBASE_USERS_COLLECTION)
                .child(userId)
                .child(FIREBASE_TRANSACTIONS_COLLECTION)
                .push().setValue(transaction).addOnSuccessListener(aVoid -> {
                    callback.onTransactionInserted();
                }).addOnFailureListener(e -> {
                    callback.onTransactionsFailure(e);
                });
    }

    @Override
    public void deleteTransaction(String transactionId) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onTransactionsFailure(new Exception("User not authenticated"));
            return;
        }
        String userId = currentUser.getUid();
        databaseReference
                .child(FIREBASE_USERS_COLLECTION)
                .child(userId)
                .child(FIREBASE_TRANSACTIONS_COLLECTION)
                .child(transactionId)
                .removeValue().addOnSuccessListener(aVoid -> {
                    callback.onTransactionDeleted();
                }).addOnFailureListener(e -> {
                    callback.onTransactionsFailure(e);
                });
    }
}
