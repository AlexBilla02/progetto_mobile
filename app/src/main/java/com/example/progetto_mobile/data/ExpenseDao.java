package com.example.progetto_mobile.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import androidx.room.Query;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(Expense expense);

    @Delete
    void delete(Expense expense);

    @Update
    void update(Expense expense);

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses(String userId);

    // Ultime N spese
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    LiveData<List<Expense>> getRecentExpenses(String userId, int limit);

    //spese in un intervallo di date
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesBetween(String userId, long from, long to);

    //Queste due per restituire i totali
    @Query("SELECT SUM(amountBase) FROM expenses WHERE userId = :userId AND date >= :from")
    LiveData<Double> getTotalFrom(String userId, long from);
    @Query("SELECT SUM(amountBase) FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to")
    LiveData<Double> getTotalBetween(String userId, long from, long to);

    // Totale per categoria in un mese
    @Query("SELECT category, SUM(amountBase) as total FROM expenses " +
            "WHERE userId = :userId AND date BETWEEN :from AND :to " +
            "GROUP BY category")
    LiveData<List<CategoryTotal>> getTotalByCategory(String userId, long from, long to);

    // Totale giornaliero in un mese
    @Query("SELECT date / 86400000 as day, SUM(amountBase) as total FROM expenses " +
            "WHERE userId = :userId AND date BETWEEN :from AND :to " +
            "GROUP BY day ORDER BY day")
    LiveData<List<DailyTotal>> getDailyTotals(String userId, long from, long to);

    // Lettura sincrona — usata solo per il ricalcolo, non per la UI
    @Query("SELECT * FROM expenses WHERE userId = :userId")
    List<Expense> getAllExpensesForUserSync(String userId);

    // Aggiornamento multiplo in un colpo solo
    @Update
    void updateAll(List<Expense> expenses);

}
