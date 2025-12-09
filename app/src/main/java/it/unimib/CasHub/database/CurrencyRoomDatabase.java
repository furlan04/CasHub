package it.unimib.CasHub.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import it.unimib.CasHub.model.CurrencyEntity;

@Database(entities = {CurrencyEntity.class}, version = 1, exportSchema = false)
public abstract class CurrencyRoomDatabase extends RoomDatabase {

    public abstract CurrencyDao currencyDao();

    private static volatile CurrencyRoomDatabase INSTANCE;

    public static CurrencyRoomDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (CurrencyRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    CurrencyRoomDatabase.class,
                                    "currencies_database")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}