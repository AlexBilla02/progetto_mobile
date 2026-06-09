package com.example.progetto_mobile.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Expense.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ExpenseDao expenseDao();

    private static volatile AppDatabase instance;


    public static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mywallet_database"
                            ).fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}