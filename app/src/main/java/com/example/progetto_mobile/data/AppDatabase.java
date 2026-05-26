package com.example.progetto_mobile.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Expense.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Metodo astratto che Room implementa automaticamente
    public abstract ExpenseDao expenseDao();

    // Singleton — una sola istanza in tutta l'app
    private static volatile AppDatabase instance;

    // ExecutorService per eseguire operazioni DB in background
    // (Room non permette operazioni sul thread principale)
    public static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "spendwise_database"
                            ).fallbackToDestructiveMigration()  // ← aggiunta
                            .build();
                }
            }
        }
        return instance;
    }
}