package com.example.progetto_mobile.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.progetto_mobile.Category;
import com.example.progetto_mobile.Expense;
import com.example.progetto_mobile.HomeViewModel;
import com.example.progetto_mobile.databinding.BottomSheetExpenseFormBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseFormBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetExpenseFormBinding binding;
    private HomeViewModel viewModel;
    private final Calendar selectedDate = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetExpenseFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Il ViewModel è condiviso con HomeFragment (stessa Activity)
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupCategoryDropdown();
        setupCurrencyDropdown();
        setupDatePicker();
        setupSaveButton();
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                Category.getLabels()
        );
        binding.acvCategory.setAdapter(adapter);
        // Imposta "Altro" come default
        binding.acvCategory.setText(Category.ALTRO.getLabel(), false);
    }

    private void setupCurrencyDropdown() {
        String[] currencies = {"EUR", "USD", "GBP", "CHF", "JPY"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                currencies
        );
        binding.acvCurrency.setAdapter(adapter);
        binding.acvCurrency.setText("EUR", false); // default EUR
    }

    private void setupDatePicker() {
        // Mostra la data di oggi come default
        updateDateField();

        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.tilDate.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    updateDateField();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateField() {
        String formatted = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(selectedDate.getTime());
        binding.etDate.setText(formatted);
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            if (!validateForm()) return;

            String name     = binding.etName.getText().toString().trim();
            String catLabel = binding.acvCategory.getText().toString();
            String amountStr= binding.etAmount.getText().toString().trim();
            String currency = binding.acvCurrency.getText().toString();
            String note     = binding.etNote.getText().toString().trim();

            double amount = Double.parseDouble(amountStr.replace(",", "."));
            Category category = Category.fromLabel(catLabel);

            // Usa la data selezionata + l'ora corrente
            Calendar now = Calendar.getInstance();
            selectedDate.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
            selectedDate.set(Calendar.MINUTE, now.get(Calendar.MINUTE));

            Expense expense = new Expense(name, category, amount, currency,
                    selectedDate.getTimeInMillis(), note);

            viewModel.addExpense(expense);

            Toast.makeText(requireContext(), "Spesa salvata!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        if (binding.etName.getText().toString().trim().isEmpty()) {
            binding.tilName.setError("Inserisci un nome");
            valid = false;
        } else {
            binding.tilName.setError(null);
        }

        if (binding.etAmount.getText().toString().trim().isEmpty()) {
            binding.tilAmount.setError("Inserisci un importo");
            valid = false;
        } else {
            binding.tilAmount.setError(null);
        }

        return valid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}