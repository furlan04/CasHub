package it.unimib.CasHub.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import it.unimib.CasHub.model.TransactionEntity;

@Dao
public interface TransactionDao {
    @Insert
    void insertTransaction(TransactionEntity transaction);

    @Query("SELECT * FROM transactions")
    List<TransactionEntity> getAllTransactions();
}
