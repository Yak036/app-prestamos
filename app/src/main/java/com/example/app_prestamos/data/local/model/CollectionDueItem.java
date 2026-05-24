package com.example.app_prestamos.data.local.model;

import androidx.room.ColumnInfo;

public class CollectionDueItem {

    @ColumnInfo(name = "payment_id")
    public long paymentId;

    @ColumnInfo(name = "loan_id")
    public long loanId;

    @ColumnInfo(name = "client_id")
    public long clientId;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    public String phone;

    @ColumnInfo(name = "installment_number")
    public int installmentNumber;

    @ColumnInfo(name = "installment_count")
    public int installmentCount;

    @ColumnInfo(name = "amount_cents")
    public long amountCents;

    @ColumnInfo(name = "currency_code")
    public String currencyCode;

    @ColumnInfo(name = "due_date_epoch_day")
    public long dueDateEpochDay;

    @ColumnInfo(name = "payment_frequency")
    public String paymentFrequency;
}
