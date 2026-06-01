package com.example.progetto_mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.progetto_mobile.databinding.ActivityMainBinding;
import com.example.progetto_mobile.ui.fragments.AddExpenseBottomSheet;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding: genera automaticamente riferimenti a tutti gli elementi del layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        if (UserSession.isDarkMode(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(binding.getRoot());

        setupNavigation();
        setupFab();
        ExchangeRateManager.getInstance(this).getRates(
                new ExchangeRateManager.RatesCallback() {
                    @Override
                    public void onRatesReady(JSONObject rates) {
                        // Tassi pronti in cache — niente da fare
                        android.util.Log.d("ExchangeRate", "Tassi caricati");
                    }
                    @Override
                    public void onFailure() {
                        // Nessun internet — userà la cache precedente se disponibile
                        android.util.Log.d("ExchangeRate", "Tassi non disponibili");
                    }
                }
        );
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        navController = navHostFragment.getNavController();

        // Collega la BottomNavigation al NavController automaticamente
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }

    private void setupFab() {
        binding.fabAddExpense.setOnClickListener(v -> {
            new AddExpenseBottomSheet().show(
                    getSupportFragmentManager(),
                    "AddExpense"
            );
        });
    }
}