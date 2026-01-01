package it.unimib.CasHub.source.transaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.model.TransactionType;

public class TransactionAPIDataSource extends BaseTransactionDataSource {

    private final FirebaseFirestore db;

    public TransactionAPIDataSource() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void getTransactions() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onTransactionsFailureFromRemote(new Exception("User not logged in"));
            return;
        }
        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("transactions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TransactionEntity> transactionList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                TransactionEntity transaction = new TransactionEntity();
                                transaction.setId(document.getId());
                                transaction.setName(document.getString("name"));
                                transaction.setAmount(document.getDouble("amount"));
                                transaction.setCurrency(document.getString("currency"));

                                String typeStr = document.getString("type");
                                if (typeStr != null) {
                                    transaction.setType(TransactionType.valueOf(typeStr));
                                }

                                transactionList.add(transaction);
                            } catch (Exception e) {
                                // Log the error or handle corrupted data
                            }
                        }
                        callback.onTransactionsSuccessFromRemote(transactionList);
                    } else {
                        callback.onTransactionsFailureFromRemote(task.getException());
                    }
                });
    }

    @Override
    public void insertTransaction(TransactionEntity transaction) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onTransactionsFailureFromRemote(new Exception("User not logged in"));
            return;
        }
        String userId = currentUser.getUid();

        DocumentReference newTransactionRef = db.collection("users").document(userId).collection("transactions").document();
        transaction.setId(newTransactionRef.getId());

        newTransactionRef.set(transaction)
                .addOnSuccessListener(aVoid -> callback.onTransactionInsertedFromRemote())
                .addOnFailureListener(e -> callback.onTransactionsFailureFromRemote(e));
    }

    @Override
    public void deleteTransaction(String transactionId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onTransactionsFailureFromRemote(new Exception("User not logged in"));
            return;
        }
        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("transactions").document(transactionId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onTransactionDeletedFromRemote())
                .addOnFailureListener(e -> callback.onTransactionsFailureFromRemote(e));
    }

    @Override
    public void deleteAll() {
        // Not implemented for remote data source
    }
}
