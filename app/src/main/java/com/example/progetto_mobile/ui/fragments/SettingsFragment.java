package com.example.progetto_mobile.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.example.progetto_mobile.AuthActivity;
import com.example.progetto_mobile.R;
import com.example.progetto_mobile.UserSession;
import com.example.progetto_mobile.databinding.FragmentSettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupProfile();
        setupEditProfile();
        setupBudget();
        setupCurrency();
        setupDarkMode();
        setupLogout();
    }

    private void setupProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String name  = user.getDisplayName();
        String email = user.getEmail();

        if (name != null && !name.isEmpty()) {
            binding.tvUsername.setText(name);
            binding.tvAvatar.setText(name.substring(0, 1).toUpperCase());
        } else if (email != null) {
            binding.tvUsername.setText(email.split("@")[0]);
            binding.tvAvatar.setText(email.substring(0, 1).toUpperCase());
        }

        if (email != null) {
            binding.tvEmail.setText(email);
        }

        binding.tvAvatar.getBackground().setTint(
                androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        com.example.progetto_mobile.R.color.primary));
    }

    private void setupEditProfile() {
        binding.btnEditProfile.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Funzione in arrivo", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBudget() {
        updateBudgetLabel();

        binding.btnSetBudget.setOnClickListener(v -> showBudgetDialog());
    }

    private void showBudgetDialog() {
        String baseCurrency = UserSession.getBaseCurrency(requireContext());
        double current = UserSession.getMonthlyBudget(requireContext());

        EditText input = new EditText(requireContext());
        input.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Es. 500");
        if (current > 0) {
            input.setText(String.format(Locale.getDefault(), "%.2f", current));
        }
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.budget))
                .setMessage(getString(R.string.add_budget) + baseCurrency)
                .setView(input)
                .setPositiveButton(R.string.save_budget, (dialog, which) -> {
                    String val = input.getText().toString().trim();
                    if (!val.isEmpty()) {
                        double budget = Double.parseDouble(val.replace(",", "."));
                        UserSession.setMonthlyBudget(requireContext(), budget);
                        updateBudgetLabel();
                        Toast.makeText(requireContext(),
                                R.string.budget_saved, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.annulla, null)
                .show();
    }

    private void updateBudgetLabel() {
        double budget = UserSession.getMonthlyBudget(requireContext());
        String baseCurrency = UserSession.getBaseCurrency(requireContext());
        if (budget > 0) {
            binding.tvBudgetValue.setText(String.format(
                    Locale.getDefault(), getString(R.string.monthly_budget), budget, baseCurrency));
        } else {
            binding.tvBudgetValue.setText(R.string.not_set);
        }
    }
    private void setupCurrency() {
        updateCurrencyLabel();
        binding.btnSetCurrency.setOnClickListener(v -> showCurrencyDialog());
    }

    private void showCurrencyDialog() {
        String[] currencies = {"EUR", "USD", "GBP", "CHF", "JPY"};
        String current = UserSession.getBaseCurrency(requireContext());

        int currentIndex = 0;
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(current)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.main_currency)
                .setSingleChoiceItems(currencies, currentIndex, null)
                .setPositiveButton(R.string.save_budget, (dialog, which) -> {
                    int selected = ((AlertDialog) dialog).getListView()
                            .getCheckedItemPosition();
                    String newCurrency = currencies[selected];
                    UserSession.setBaseCurrency(requireContext(), newCurrency);
                    updateCurrencyLabel();
                    Toast.makeText(requireContext(),
                            getString(R.string.currency_set) + newCurrency,
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.annulla, null)
                .show();
    }

    private void updateCurrencyLabel() {
        binding.tvCurrencyValue.setText(
                UserSession.getBaseCurrency(requireContext()));
    }
    private void setupDarkMode() {
        // Imposta lo stato attuale del toggle
        binding.switchDarkMode.setChecked(UserSession.isDarkMode(requireContext()));

        binding.switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            UserSession.setDarkMode(requireContext(), isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }

    private void setupLogout() {
        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.logout)
                    .setMessage(R.string.ask_logout)
                    .setPositiveButton(R.string.logout, (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(requireActivity(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.annulla, null)
                    .show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}