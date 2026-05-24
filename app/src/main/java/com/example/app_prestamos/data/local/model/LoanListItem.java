package com.example.app_prestamos.data.local.model;

import androidx.room.ColumnInfo;

public class LoanListItem {

    @ColumnInfo(name = "loan_id")
    public long loanId;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name = "principal_amount_cents")
    public long principalAmountCents;

    @ColumnInfo(name = "total_receivable_cents")
    public long totalReceivableCents;

    @ColumnInfo(name = "currency_code")
    public String currencyCode;

    @ColumnInfo(name = "profit_percent")
    public double profitPercent;

    @ColumnInfo(name = "installment_count")
    public int installmentCount;

    @ColumnInfo(name = "payment_frequency")
    public String paymentFrequency;
}
