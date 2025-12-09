package it.unimib.CasHub.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import it.unimib.CasHub.model.RateEntity;

@Database(entities = {RateEntity.class}, version = 1, exportSchema = false)
public abstract class RatesRoomDatabase extends RoomDatabase {

    public abstract RatesDao ratesDao();

    private static volatile RatesRoomDatabase INSTANCE;

    public static RatesRoomDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (RatesRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RatesRoomDatabase.class, "rates_database")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}