package it.unimib.CasHub.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.unimib.CasHub.model.RateEntity;

@Dao
public interface RatesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRate(RateEntity rate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRates(List<RateEntity> rates);

    @Query("SELECT * FROM rates_table")
    List<RateEntity> getAllRates();

    @Query("SELECT * FROM rates_table WHERE currencyCode = :code LIMIT 1")
    RateEntity getRateByCode(String code);

    @Query("DELETE FROM rates_table")
    void deleteAllRates();
}