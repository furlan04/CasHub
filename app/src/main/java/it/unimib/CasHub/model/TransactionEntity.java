package it.unimib.CasHub.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import it.unimib.CasHub.database.TransactionTypeConverter;

@Entity(tableName = "transactions")
@TypeConverters(TransactionTypeConverter.class)
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double amount;
    private TransactionType type;
    private String currency;

    public TransactionEntity(double amount, TransactionType type, String currency) {
        this.amount = amount;
        this.type = type;
        this.currency = currency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }
}
