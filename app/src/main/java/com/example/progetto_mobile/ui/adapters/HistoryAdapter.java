package com.example.progetto_mobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.progetto_mobile.R;
import com.example.progetto_mobile.data.Category;
import com.example.progetto_mobile.data.DayHeader;
import com.example.progetto_mobile.data.Expense;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Costanti per distinguere i due tipi di riga
    private static final int TYPE_HEADER  = 0;
    private static final int TYPE_EXPENSE = 1;

    // Lista mista di DayHeader e Expense
    private List<Object> items = new ArrayList<>();

    private final OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    public HistoryAdapter(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    // Riceve la lista flat di spese e la raggruppa per giorno
    public void setExpenses(List<Expense> expenses) {
        items = groupByDay(expenses);
        notifyDataSetChanged();
    }

    // Raggruppa le spese per giorno e costruisce la lista mista
    private List<Object> groupByDay(List<Expense> expenses) {
        // LinkedHashMap mantiene l'ordine di inserimento
        Map<String, List<Expense>> grouped = new LinkedHashMap<>();

        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Expense expense : expenses) {
            String dayKey = dayFormat.format(new Date(expense.getDate()));
            if (!grouped.containsKey(dayKey)) {
                grouped.put(dayKey, new ArrayList<>());
            }
            grouped.get(dayKey).add(expense);
        }

        // Costruisce la lista finale: header + spese per ogni giorno
        List<Object> result = new ArrayList<>();
        for (Map.Entry<String, List<Expense>> entry : grouped.entrySet()) {
            // Calcola il totale del giorno
            double dayTotal = 0;
            for (Expense e : entry.getValue()) {
                dayTotal += e.getAmount();
            }
            result.add(new DayHeader(entry.getKey(), dayTotal));
            result.addAll(entry.getValue());
        }
        return result;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof DayHeader ? TYPE_HEADER : TYPE_EXPENSE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_day_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((DayHeader) items.get(position));
        } else {
            ((ExpenseViewHolder) holder).bind((Expense) items.get(position));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }
    public List<Expense> getCurrentExpenses() {
        List<Expense> expenses = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Expense) {
                expenses.add((Expense) item);
            }
        }
        return expenses;
    }

    // --- ViewHolder per l'intestazione del giorno ---
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate;
        private final TextView tvTotal;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate  = itemView.findViewById(R.id.tv_day_date);
            tvTotal = itemView.findViewById(R.id.tv_day_total);
        }

        void bind(DayHeader header) {
            tvDate.setText(header.getDateLabel());
            tvTotal.setText(String.format(Locale.getDefault(), "€ %.2f", header.getDayTotal()));
        }


    }

    // --- ViewHolder per la singola spesa (uguale a ExpenseAdapter) ---
    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView tvCategoryIcon;
        private final TextView tvExpenseName;
        private final TextView tvCategoryName;
        private final TextView tvAmount;
        private final TextView tvDatetime;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tv_category_icon);
            tvExpenseName  = itemView.findViewById(R.id.tv_expense_name);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvAmount       = itemView.findViewById(R.id.tv_amount);
            tvDatetime     = itemView.findViewById(R.id.tv_datetime);
        }

        void bind(Expense expense) {
            Context ctx = itemView.getContext();


            int color = ContextCompat.getColor(ctx,
                    Category.fromLabel(expense.getCategory()).getColorRes());
            tvCategoryIcon.getBackground().setTint(color);
            tvCategoryIcon.setImageResource(
                    Category.fromLabel(expense.getCategory()).getIconRes());
            tvCategoryIcon.setColorFilter(
                    android.graphics.Color.WHITE,
                    android.graphics.PorterDuff.Mode.SRC_IN);


            tvExpenseName.setText(expense.getName());
            tvCategoryName.setText(expense.getCategory());
            tvAmount.setText(expense.getFormattedAmount());
            tvDatetime.setText(expense.getFormattedDateTime());

            itemView.setOnClickListener(v -> listener.onExpenseClick(expense));
        }

    }
}