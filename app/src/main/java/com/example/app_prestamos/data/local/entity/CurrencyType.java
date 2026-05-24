package com.example.app_prestamos.data.local.entity;

public final class CurrencyType {

    public static final String PEN = "PEN"; // Soles peruanos
    public static final String MXN = "MXN"; // Pesos mexicanos
    public static final String COP = "COP"; // Pesos colombianos
    public static final String VES = "VES"; // Bolivares venezolanos
    public static final String USD = "USD"; // Dolares
    public static final String EUR = "EUR"; // Euros

    private CurrencyType() {
        // Clase utilitaria: mantiene las monedas soportadas en un solo lugar.
    }
}
