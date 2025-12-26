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
    private String name;
    private TransactionType type;
    private String currency;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public TransactionType getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }
}
