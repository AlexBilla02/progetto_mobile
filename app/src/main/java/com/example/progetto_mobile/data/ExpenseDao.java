package com.example.progetto_mobile.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import androidx.room.Query;

import java.util.List;

@Dao  // dice a Room che questa interfaccia è un DAO
public interface ExpenseDao {

    @Insert
    void insert(Expense expense);

    @Delete
    void delete(Expense expense);

    @Update
    void update(Expense expense);
    // LiveData: Room aggiorna automaticamente la lista quando cambia il database
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses(String userId);

    // Ultime N spese per la home
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    LiveData<List<Expense>> getRecentExpenses(String userId, int limit);

    // Spese in un intervallo di date (per lo storico con filtri)
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesBetween(String userId, long from, long to);

    // Totale speso in un intervallo (per la home e le stats)
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date >= :from")
    LiveData<Double> getTotalFrom(String userId, long from);

}
