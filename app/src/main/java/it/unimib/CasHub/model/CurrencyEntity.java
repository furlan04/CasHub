package it.unimib.CasHub.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "currency_table")
public class CurrencyEntity {

    @PrimaryKey
    @NonNull
    public String code;

    public String name;

    public CurrencyEntity(@NonNull String code, String name) {
        this.code = code;
        this.name = name;
    }
}