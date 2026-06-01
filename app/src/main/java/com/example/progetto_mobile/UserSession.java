package com.example.progetto_mobile;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserSession {

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static String getCurrentUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return "Utente";
        String name = user.getDisplayName();
        return (name != null && !name.isEmpty()) ? name : user.getEmail();
    }
    public static String getBaseCurrency(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                "user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("base_currency", "EUR"); // default EUR
    }

    public static void setBaseCurrency(Context context, String currency) {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("base_currency", currency)
                .apply();
    }
}