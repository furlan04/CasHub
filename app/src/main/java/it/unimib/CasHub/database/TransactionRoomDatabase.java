package it.unimib.CasHub.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unimib.CasHub.model.TransactionEntity;

@Database(entities = {TransactionEntity.class}, version = 1, exportSchema = false)
@TypeConverters({TransactionTypeConverter.class})
public abstract class TransactionRoomDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();

    private static volatile TransactionRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static TransactionRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TransactionRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TransactionRoomDatabase.class, "transactions_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
