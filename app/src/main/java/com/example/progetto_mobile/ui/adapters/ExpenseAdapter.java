package com.example.progetto_mobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto_mobile.R;
import com.example.progetto_mobile.data.Category;
import com.example.progetto_mobile.data.Expense;

import java.util.ArrayList;
import java.util.List;

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

        private final TextView tvCategoryIcon;
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
            tvDatetime         = itemView.findViewById(R.id.tv_datetime);
        }

        void bind(Expense expense) {
            Context ctx = itemView.getContext();

            // Prima lettera della categoria come icona nel cerchio
            String initial = expense.getCategory().substring(0, 1).toUpperCase();
            tvCategoryIcon.setText(initial);

            // Colore del cerchio in base alla categoria
            int color = ContextCompat.getColor(ctx, Category.fromLabel(expense.getCategory()).getColorRes());
            tvCategoryIcon.getBackground().setTint(color);

            tvExpenseName.setText(expense.getName());
            tvCategoryName.setText(expense.getCategory());
            tvAmount.setText(expense.getFormattedAmount());
            tvDatetime.setText(expense.getFormattedDateTime());

            itemView.setOnClickListener(v -> listener.onExpenseClick(expense));
            itemView.setOnLongClickListener(v -> {
                listener.onExpenseLongClick(expense);
                return true;
            });
        }
    }
}