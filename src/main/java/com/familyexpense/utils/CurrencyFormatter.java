package com.familyexpense.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private static final Locale INDIA = new Locale("en", "IN");
    private static final NumberFormat INR_FORMAT = NumberFormat.getCurrencyInstance(INDIA);

    public static String format(double amount) {
        // Use ₹ symbol with 2 decimal places
        return String.format("₹%,.2f", amount);
    }

    public static String formatCompact(double amount) {
        if (amount >= 100000) {
            return String.format("₹%.1fL", amount / 100000);
        } else if (amount >= 1000) {
            return String.format("₹%.1fK", amount / 1000);
        }
        return String.format("₹%.0f", amount);
    }

    public static String formatNoDecimal(double amount) {
        return String.format("₹%,.0f", amount);
    }

    public static double parse(String text) {
        try {
            // Remove ₹ and commas
            String cleaned = text.replace("₹", "").replace(",", "").trim();
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
