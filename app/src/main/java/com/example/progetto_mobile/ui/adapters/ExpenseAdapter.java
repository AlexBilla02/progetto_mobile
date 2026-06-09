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
import com.example.progetto_mobile.data.Expense;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//classe usata semplicemente per adattare lo stile di ogni spesa nel recycler view
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private final OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
        void onExpenseLongClick(Expense expense);
    }

    public ExpenseAdapter(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(expenses.get(position));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private final ImageView tvCategoryIcon;
        private final TextView tvExpenseName;
        private final TextView tvCategoryName;
        private final TextView tvAmount;
        private final TextView tvAmountConverted;
        private final TextView tvDatetime;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon    = itemView.findViewById(R.id.tv_category_icon);
            tvExpenseName     = itemView.findViewById(R.id.tv_expense_name);
            tvCategoryName    = itemView.findViewById(R.id.tv_category_name);
            tvAmount          = itemView.findViewById(R.id.tv_amount);
            tvAmountConverted = itemView.findViewById(R.id.tv_amount_converted);
            tvDatetime        = itemView.findViewById(R.id.tv_datetime);
        }

        void bind(Expense expense) {
            Context ctx = itemView.getContext();
            String baseCurrency = UserSession.getBaseCurrency(ctx);

            // Icona e colore categoria
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

            // Conversione valuta che mostro solo se la valuta è diversa da quella principale
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
            itemView.setOnLongClickListener(v -> {
                listener.onExpenseLongClick(expense);
                return true;
            });
        }
    }
}