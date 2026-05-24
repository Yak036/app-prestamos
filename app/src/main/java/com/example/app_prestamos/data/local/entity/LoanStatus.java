package com.example.app_prestamos.data.local.entity;

public final class LoanStatus {

    public static final String ACTIVE = "ACTIVE";
    public static final String PAID = "PAID";
    public static final String CANCELLED = "CANCELLED";

    private LoanStatus() {
        // Clase utilitaria: evita crear instancias de estados fijos.
    }
}
