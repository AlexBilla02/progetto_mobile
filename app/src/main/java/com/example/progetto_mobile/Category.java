package com.example.progetto_mobile;

import com.example.progetto_mobile.R;

public enum Category {
    CIBO,
    SPESA,
    TRASPORTI,
    SVAGO,
    SALUTE,
    TECH,
    ABBIGLIAMENTO,
    ALTRO;

    public String getLabel() {
        switch (this) {
            case CIBO:          return "Cibo";
            case SPESA:         return "Spesa";
            case TRASPORTI:     return "Trasporti";
            case SVAGO:         return "Svago";
            case SALUTE:        return "Salute";
            case TECH:          return "Tech";
            case ABBIGLIAMENTO: return "Abbigliamento";
            default:            return "Altro";
        }
    }

    public int getColorRes() {
        switch (this) {
            case CIBO:          return R.color.category_food;
            case SPESA:         return R.color.category_grocery;
            case TRASPORTI:     return R.color.category_transport;
            case SVAGO:         return R.color.category_entertainment;
            case SALUTE:        return R.color.category_health;
            case TECH:          return R.color.category_tech;
            case ABBIGLIAMENTO: return R.color.category_clothing;
            default:            return R.color.category_other;
        }
    }

    // Usato per popolare il menu a tendina nel form
    public static String[] getLabels() {
        Category[] values = values();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].getLabel();
        }
        return labels;
    }

    public static Category fromLabel(String label) {
        for (Category c : values()) {
            if (c.getLabel().equals(label)) return c;
        }
        return ALTRO;
    }
}