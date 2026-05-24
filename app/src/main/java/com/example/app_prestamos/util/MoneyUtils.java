package com.example.app_prestamos.util;

import com.example.app_prestamos.data.local.entity.CurrencyType;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class MoneyUtils {

    private MoneyUtils() {
        // Clase utilitaria: concentra el formato de dinero de toda la app.
    }

    public static long calculateProfitCents(long principalAmountCents, double profitPercent) {
        return Math.round(principalAmountCents * (profitPercent / 100.0));
    }

    public static long parseToCents(String amountText) {
        if (amountText == null || amountText.trim().isEmpty()) {
            throw new IllegalArgumentException("El monto es obligatorio.");
        }

        double amount = Double.parseDouble(amountText.trim().replace(",", "."));
        if (amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }
        return Math.round(amount * 100);
    }

    public static String formatCents(long amountCents) {
        return formatCents(amountCents, CurrencyType.USD);
    }

    public static String formatCents(long amountCents, String currencyCode) {
        NumberFormat format = NumberFormat.getCurrencyInstance(localeForCurrency(currencyCode));
        format.setCurrency(Currency.getInstance(normalizeCurrencyCode(currencyCode)));
        return format.format(amountCents / 100.0);
    }

    public static String displayName(String currencyCode) {
        switch (normalizeCurrencyCode(currencyCode)) {
            case CurrencyType.PEN:
                return "Soles peruanos";
            case CurrencyType.MXN:
                return "Pesos mexicanos";
            case CurrencyType.COP:
                return "Pesos colombianos";
            case CurrencyType.VES:
                return "Bolivares venezolanos";
            case CurrencyType.EUR:
                return "Euros";
            case CurrencyType.USD:
            default:
                return "Dolares";
        }
    }

    private static Locale localeForCurrency(String currencyCode) {
        switch (normalizeCurrencyCode(currencyCode)) {
            case CurrencyType.PEN:
                return new Locale.Builder().setLanguage("es").setRegion("PE").build();
            case CurrencyType.MXN:
                return new Locale.Builder().setLanguage("es").setRegion("MX").build();
            case CurrencyType.COP:
                return new Locale.Builder().setLanguage("es").setRegion("CO").build();
            case CurrencyType.VES:
                return new Locale.Builder().setLanguage("es").setRegion("VE").build();
            case CurrencyType.EUR:
                return Locale.GERMANY;
            case CurrencyType.USD:
            default:
                return Locale.US;
        }
    }

    private static String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return CurrencyType.USD;
        }
        return currencyCode.trim().toUpperCase(Locale.US);
    }
}
