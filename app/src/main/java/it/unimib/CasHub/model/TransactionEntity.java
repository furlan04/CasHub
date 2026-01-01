package it.unimib.CasHub.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import it.unimib.CasHub.database.TransactionTypeConverter;

@IgnoreExtraProperties
@Entity(tableName = "transactions")
@TypeConverters(TransactionTypeConverter.class)
public class TransactionEntity {
    @PrimaryKey
    @NonNull
    private String id = "";
    private double amount;
    private String name;
    private TransactionType type;
    private String currency;

    public TransactionEntity() {
        // Required for Firestore deserialization
    }

    public TransactionEntity(String name, double amount, TransactionType type, String currency) {
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.currency = currency;
    }

    public TransactionEntity(TransactionEntity other) {
        this.id = other.id;
        this.name = other.name;
        this.amount = other.amount;
        this.type = other.type;
        this.currency = other.currency;
    }

    @Exclude // This will exclude the id field from Firestore serialization/deserialization
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public void setType(TransactionType type) {
        this.type = type;
    }


    public TransactionType getType() {
        return type;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }
}
