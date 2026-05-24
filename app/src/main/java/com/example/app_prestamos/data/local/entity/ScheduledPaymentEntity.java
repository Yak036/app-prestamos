package com.example.app_prestamos.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "scheduled_payments",
        foreignKeys = {
                @ForeignKey(
                        entity = LoanEntity.class,
                        parentColumns = "id",
                        childColumns = "loan_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"loan_id"}),
                @Index(value = {"due_date_epoch_day"}),
                @Index(value = {"loan_id", "installment_number"}, unique = true),
                @Index(value = {"status"})
        }
)
public class ScheduledPaymentEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "loan_id")
    public long loanId;

    @ColumnInfo(name = "installment_number")
    public int installmentNumber;

    // Fecha exacta de la cuota. Sirve para calendario, dashboard y notificaciones.
    @ColumnInfo(name = "due_date_epoch_day")
    public long dueDateEpochDay;

    @ColumnInfo(name = "amount_cents")
    public long amountCents;

    public String status = PaymentStatus.PENDING;

    @ColumnInfo(name = "paid_at_millis")
    public Long paidAtMillis;

    public String notes;

    @ColumnInfo(name = "created_at_millis")
    public long createdAtMillis;

    @ColumnInfo(name = "updated_at_millis")
    public long updatedAtMillis;
}
