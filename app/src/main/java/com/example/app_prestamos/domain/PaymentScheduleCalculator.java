package com.example.app_prestamos.domain;

import com.example.app_prestamos.data.local.entity.ScheduledPaymentEntity;
import com.example.app_prestamos.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public final class PaymentScheduleCalculator {

    private PaymentScheduleCalculator() {
        // Clase utilitaria: genera cuotas sin depender de pantallas ni base de datos.
    }

    public static List<ScheduledPaymentEntity> buildScheduledPayments(
            long loanId,
            long startDateEpochDay,
            long totalReceivableCents,
            int installmentCount,
            String paymentFrequency,
            long nowMillis
    ) {
        if (installmentCount <= 0) {
            throw new IllegalArgumentException("La cantidad de cuotas debe ser mayor que cero.");
        }

        List<ScheduledPaymentEntity> payments = new ArrayList<>();
        long baseAmount = totalReceivableCents / installmentCount;
        long remainder = totalReceivableCents % installmentCount;

        for (int installment = 1; installment <= installmentCount; installment++) {
            ScheduledPaymentEntity payment = new ScheduledPaymentEntity();
            payment.loanId = loanId;
            payment.installmentNumber = installment;
            payment.amountCents = baseAmount;

            // Reparte los centavos sobrantes en las primeras cuotas para que el total cuadre exacto.
            if (installment <= remainder) {
                payment.amountCents++;
            }

            // La primera cuota queda para el siguiente periodo. Luego se puede hacer configurable.
            payment.dueDateEpochDay = DateUtils.addFrequency(startDateEpochDay, paymentFrequency, installment);
            payment.createdAtMillis = nowMillis;
            payment.updatedAtMillis = nowMillis;
            payments.add(payment);
        }

        return payments;
    }
}
