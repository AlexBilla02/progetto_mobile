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

import com.example.progetto_mobile.ExchangeRateManager;
import com.example.progetto_mobile.R;
import com.example.progetto_mobile.UserSession;
import com.example.progetto_mobile.data.Category;
import com.example.progetto_mobile.data.DayHeader;
import com.example.progetto_mobile.data.Expense;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//ragionamento simile a quello di ExpenseAdapter, però qui ho ogni volta l'header col giorno e il totale
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

    public void setExpenses(List<Expense> expenses) {
        items = groupByDay(expenses);
        notifyDataSetChanged();
    }

    // Raggruppa le spese per giorno e costruisce la lista mista
    private List<Object> groupByDay(List<Expense> expenses) {
        Map<String, List<Expense>> grouped = new LinkedHashMap<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Expense expense : expenses) {
            String dayKey = dayFormat.format(new Date(expense.getDate()));
            if (!grouped.containsKey(dayKey)) {
                grouped.put(dayKey, new ArrayList<>());
            }
            grouped.get(dayKey).add(expense);
        }

        List<Object> result = new ArrayList<>();
        for (Map.Entry<String, List<Expense>> entry : grouped.entrySet()) {
            double dayTotal = 0;
            for (Expense e : entry.getValue()) {
                // Se amountBase è 0 (spese vecchie), usa amount come fallback
                double base = e.getAmountBase() > 0 ? e.getAmountBase() : e.getAmount();
                dayTotal += base;
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

    // il headerviewholder racchiude l'header del giorno col totale
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
            String baseCurrency = UserSession.getBaseCurrency(itemView.getContext());
            tvTotal.setText(String.format(Locale.getDefault(),
                    "%.2f %s", header.getDayTotal(), baseCurrency));
        }


    }

    // Questo invece è come l'expenseAdapter
    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView tvCategoryIcon;
        private final TextView tvExpenseName;
        private final TextView tvCategoryName;
        private final TextView tvAmount;
        private final TextView tvAmountConverted;

        private final TextView tvDatetime;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tv_category_icon);
            tvExpenseName  = itemView.findViewById(R.id.tv_expense_name);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvAmount       = itemView.findViewById(R.id.tv_amount);
            tvAmountConverted = itemView.findViewById(R.id.tv_amount_converted);
            tvDatetime     = itemView.findViewById(R.id.tv_datetime);
        }

        void bind(Expense expense) {
            Context ctx = itemView.getContext();
            String baseCurrency = UserSession.getBaseCurrency(ctx); // ← dichiarato qui



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

            if (!expense.getCurrency().equals(baseCurrency)) {
                ExchangeRateManager manager = ExchangeRateManager.getInstance(ctx);
                if (manager.hasCache()) {
                    double base = ExchangeRateManager.convertToBase(
                            expense.getAmount(),
                            expense.getCurrency(),
                            baseCurrency,
                            manager.getCachedRates());
                    tvAmountConverted.setText(String.format(
                            Locale.getDefault(), "≈ %.2f %s", base, baseCurrency));
                    tvAmountConverted.setVisibility(View.VISIBLE);
                } else {
                    manager.getRates(new ExchangeRateManager.RatesCallback() {
                        @Override
                        public void onRatesReady(JSONObject rates) {
                            double base = ExchangeRateManager.convertToBase(
                                    expense.getAmount(),
                                    expense.getCurrency(),
                                    baseCurrency,
                                    rates);
                            new android.os.Handler(
                                    android.os.Looper.getMainLooper()).post(() -> {
                                tvAmountConverted.setText(String.format(
                                        Locale.getDefault(),
                                        "≈ %.2f %s", base, baseCurrency));
                                tvAmountConverted.setVisibility(View.VISIBLE);
                            });
                        }

                        @Override
                        public void onFailure() {
                            new android.os.Handler(
                                    android.os.Looper.getMainLooper()).post(() ->
                                    tvAmountConverted.setVisibility(View.GONE));
                        }
                    });
                }
            } else {
                tvAmountConverted.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onExpenseClick(expense));
        }

    }
}