package com.example.app_prestamos.data.local.entity;

public final class PaymentStatus {

    public static final String PENDING = "PENDING";
    public static final String PAID = "PAID";
    public static final String SKIPPED = "SKIPPED";

    private PaymentStatus() {
        // Clase utilitaria: evita escribir estados distintos para la misma idea.
    }
}
