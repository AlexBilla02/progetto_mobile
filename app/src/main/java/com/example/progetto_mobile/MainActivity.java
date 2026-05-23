package com.example.progetto_mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.progetto_mobile.databinding.ActivityMainBinding;
import com.example.progetto_mobile.ui.fragments.AddExpenseBottomSheet;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding: genera automaticamente riferimenti a tutti gli elementi del layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupFab();
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