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
import com.example.progetto_mobile.data.Category;
import com.example.progetto_mobile.data.Expense;
import com.example.progetto_mobile.HomeViewModel;
import com.example.progetto_mobile.databinding.BottomSheetExpenseFormBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseFormBottomSheet extends BottomSheetDialogFragment {
    public static final String ARG_EXPENSE_ID       = "edit_expense_id";
    public static final String ARG_EXPENSE_NAME     = "edit_expense_name";
    public static final String ARG_EXPENSE_CATEGORY = "edit_expense_category";
    public static final String ARG_EXPENSE_AMOUNT   = "edit_expense_amount";
    public static final String ARG_EXPENSE_CURRENCY = "edit_expense_currency";
    public static final String ARG_EXPENSE_DATE     = "edit_expense_date";
    public static final String ARG_EXPENSE_NOTE     = "edit_expense_note";
    private BottomSheetExpenseFormBinding binding;
    private HomeViewModel viewModel;
    private final Calendar selectedDate = Calendar.getInstance();

    private Expense expenseToEdit = null; // null = nuova spesa, non null = modifica

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
        // Controlla se siamo in modalità modifica

        if (getArguments() != null) {
            Bundle args = getArguments();
            expenseToEdit = new Expense(
                    args.getString(ARG_EXPENSE_NAME),
                    args.getString(ARG_EXPENSE_CATEGORY),
                    args.getDouble(ARG_EXPENSE_AMOUNT),
                    args.getString(ARG_EXPENSE_CURRENCY),
                    args.getLong(ARG_EXPENSE_DATE),
                    args.getString(ARG_EXPENSE_NOTE)
            );
            expenseToEdit.setId(args.getLong(ARG_EXPENSE_ID));
        }
        setupCategoryDropdown();
        setupCurrencyDropdown();
        setupDatePicker();
        setupTimePicker();
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
        // Se modifica, pre-seleziona la categoria esistente
        if (expenseToEdit != null) {
            binding.acvCategory.setText(expenseToEdit.getCategory(), false);
        }
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
        if (expenseToEdit != null) {
            binding.acvCurrency.setText(expenseToEdit.getCurrency(), false);
        }
    }

    private void setupDatePicker() {
        updateDateField();

        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.tilDate.setEndIconOnClickListener(v -> showDatePicker());
        if (expenseToEdit != null) {
            selectedDate.setTimeInMillis(expenseToEdit.getDate());
            updateDateField();
        }
    }
    private void setupTimePicker() {
        updateTimeField(); // mostra orario attuale come default

        binding.etTime.setOnClickListener(v -> showTimePicker());
        binding.tilTime.setEndIconOnClickListener(v -> showTimePicker());
        if (expenseToEdit != null) {
            selectedDate.setTimeInMillis(expenseToEdit.getDate());
            updateTimeField();
        }
    }
    private void showDatePicker() {
        new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    // Aggiorna solo anno/mese/giorno, l'ora rimane quella del time picker
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, day);
                    updateDateField();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
    private void showTimePicker() {
        int hour   = selectedDate.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDate.get(Calendar.MINUTE);

        new android.app.TimePickerDialog(
                requireContext(),
                (timePicker, selectedHour, selectedMinute) -> {
                    selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedDate.set(Calendar.MINUTE, selectedMinute);
                    updateTimeField();
                },
                hour, minute,
                true // formato 24h
        ).show();
    }
    private void updateDateField() {
        String formatted = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(selectedDate.getTime());
        binding.etDate.setText(formatted);
    }
    private void updateTimeField() {
        String formatted = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(selectedDate.getTime());
        binding.etTime.setText(formatted);
    }

    private void setupSaveButton() {
        // Cambia il testo del bottone se siamo in modalità modifica
        if (expenseToEdit != null) {
            binding.btnSave.setText("Aggiorna spesa");
            binding.etName.setText(expenseToEdit.getName());
            binding.etAmount.setText(String.valueOf(expenseToEdit.getAmount()));
            binding.etNote.setText(expenseToEdit.getNote());
        }

        binding.btnSave.setOnClickListener(v -> {
            if (!validateForm()) return;

            String name      = binding.etName.getText().toString().trim();
            String catLabel  = binding.acvCategory.getText().toString();
            String amountStr = binding.etAmount.getText().toString().trim();
            String currency  = binding.acvCurrency.getText().toString();
            String note      = binding.etNote.getText().toString().trim();
            double amount    = Double.parseDouble(amountStr.replace(",", "."));

            if (expenseToEdit != null) {
                // Modalità modifica — aggiorna la spesa esistente
                expenseToEdit.setName(name);
                expenseToEdit.setCategory(catLabel);
                expenseToEdit.setAmount(amount);
                expenseToEdit.setCurrency(currency);
                expenseToEdit.setDate(selectedDate.getTimeInMillis());
                expenseToEdit.setNote(note);
                viewModel.updateExpense(expenseToEdit);
            } else {
                // Modalità inserimento — crea nuova spesa
                Expense expense = new Expense(name, catLabel, amount, currency,
                        selectedDate.getTimeInMillis(), note);
                viewModel.addExpense(expense);
            }

            Toast.makeText(requireContext(),
                    expenseToEdit != null ? "Spesa aggiornata!" : "Spesa salvata!",
                    Toast.LENGTH_SHORT).show();
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
    public static AddExpenseFormBottomSheet newInstance(Expense expense) {
        AddExpenseFormBottomSheet sheet = new AddExpenseFormBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_EXPENSE_ID,           expense.getId());
        args.putString(ARG_EXPENSE_NAME,       expense.getName());
        args.putString(ARG_EXPENSE_CATEGORY,   expense.getCategory());
        args.putDouble(ARG_EXPENSE_AMOUNT,     expense.getAmount());
        args.putString(ARG_EXPENSE_CURRENCY,   expense.getCurrency());
        args.putLong(ARG_EXPENSE_DATE,         expense.getDate());
        args.putString(ARG_EXPENSE_NOTE,       expense.getNote());
        sheet.setArguments(args);
        return sheet;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}