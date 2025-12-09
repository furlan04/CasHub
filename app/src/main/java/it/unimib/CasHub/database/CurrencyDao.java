package it.unimib.CasHub.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.unimib.CasHub.model.CurrencyEntity;

@Dao
public interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCurrency(CurrencyEntity currency);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllCurrencies(List<CurrencyEntity> list);

    @Query("SELECT * FROM currency_table ORDER BY code ASC")
    List<CurrencyEntity> getAllCurrencies();

    @Query("SELECT * FROM currency_table WHERE code = :code LIMIT 1")
    CurrencyEntity getCurrencyByCode(String code);

    @Query("DELETE FROM currency_table")
    void deleteAllCurrencies();
}