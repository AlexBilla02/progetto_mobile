package com.example.progetto_mobile;

import android.content.pm.ActivityInfo;
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // ViewBinding genera automaticamente riferimenti a tutti gli elementi del layout
        // al posto di prendere i riferimenti con R.getElement... come a lezione
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        if (UserSession.isDarkMode(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(binding.getRoot());

        setupNavigation();
        setupFab();
        //recupero i tassi di conversione da ExchangeRateManager
        ExchangeRateManager.getInstance(this).getRates(
                new ExchangeRateManager.RatesCallback() {
                    @Override
                    public void onRatesReady(JSONObject rates) {
                        android.util.Log.d("ExchangeRate", "Tassi caricati");
                    }
                    @Override
                    public void onFailure() {
                        android.util.Log.d("ExchangeRate", "Tassi non disponibili");
                    }
                }
        );
    }

    private void setupNavigation() {
        //imposto il fragment in basso per la navigazione
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.homeFragment || id == R.id.historyFragment) {
                showFab();
            } else {
                hideFab();
            }
        });
    }
    private void showFab() { binding.fabAddExpense.show(); }
    private void hideFab() { binding.fabAddExpense.hide(); }

    private void setupFab() {
        binding.fabAddExpense.setOnClickListener(v -> {
            new AddExpenseBottomSheet().show(
                    getSupportFragmentManager(),
                    "AddExpense"
            );
        });
    }
}