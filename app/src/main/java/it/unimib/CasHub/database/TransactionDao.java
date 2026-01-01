package it.unimib.CasHub.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.unimib.CasHub.model.TransactionEntity;

@Dao
public interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransaction(TransactionEntity transaction);

    @Query("SELECT * FROM transactions")
    List<TransactionEntity> getAllTransactions();
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    void deleteTransaction(String transactionId);

    @Query("DELETE FROM transactions")
    void deleteAll();
}
