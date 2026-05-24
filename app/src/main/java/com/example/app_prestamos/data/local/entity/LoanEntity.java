package com.example.app_prestamos.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "loans",
        foreignKeys = {
                @ForeignKey(
                        entity = ClientEntity.class,
                        parentColumns = "id",
                        childColumns = "client_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index(value = {"client_id"}),
                @Index(value = {"status"}),
                @Index(value = {"payment_frequency"}),
                @Index(value = {"currency_code"})
        }
)
public class LoanEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "client_id")
    public long clientId;

    @ColumnInfo(name = "currency_code")
    public String currencyCode = CurrencyType.USD;

    // Los montos se guardan en centavos para evitar errores de decimales con dinero.
    @ColumnInfo(name = "principal_amount_cents")
    public long principalAmountCents;

    @ColumnInfo(name = "profit_percent")
    public double profitPercent;

    @ColumnInfo(name = "profit_amount_cents")
    public long profitAmountCents;

    @ColumnInfo(name = "total_receivable_cents")
    public long totalReceivableCents;

    @ColumnInfo(name = "installment_count")
    public int installmentCount;

    @ColumnInfo(name = "installment_amount_cents")
    public long installmentAmountCents;

    @ColumnInfo(name = "payment_frequency")
    public String paymentFrequency = PaymentFrequency.DAILY;

    @ColumnInfo(name = "start_date_epoch_day")
    public long startDateEpochDay;

    @ColumnInfo(name = "expected_end_date_epoch_day")
    public Long expectedEndDateEpochDay;

    public String status = LoanStatus.ACTIVE;

    public String notes;

    @ColumnInfo(name = "created_at_millis")
    public long createdAtMillis;

    @ColumnInfo(name = "updated_at_millis")
    public long updatedAtMillis;
}
