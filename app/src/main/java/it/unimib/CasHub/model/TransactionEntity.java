package it.unimib.CasHub.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.database.Exclude;

import it.unimib.CasHub.database.TransactionTypeConverter;

@Entity(tableName = "transactions")
@TypeConverters(TransactionTypeConverter.class)
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    @Exclude  // ✅ Metti @Exclude sul campo, non sul getter
    private int id;

    @Exclude  // ✅ Metti @Exclude sul campo, non sul getter
    private String firebaseId;

    private double amount;
    private String name;
    private String type;
    private String currency;

    public TransactionEntity() { }

    public TransactionEntity(String name, double amount, String type, String currency) {
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

    // ✅ Rimuovi @Exclude dal getter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // ✅ Rimuovi @Exclude dal getter
    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}