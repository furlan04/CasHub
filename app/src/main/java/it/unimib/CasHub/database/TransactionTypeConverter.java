package it.unimib.CasHub.database;

import androidx.room.TypeConverter;

import it.unimib.CasHub.model.TransactionType;

public class TransactionTypeConverter {
    @TypeConverter
    public static TransactionType fromString(String value) {
        return value == null ? null : TransactionType.valueOf(value);
    }

    @TypeConverter
    public static String toString(TransactionType value) {
        return value == null ? null : value.name();
    }
}
