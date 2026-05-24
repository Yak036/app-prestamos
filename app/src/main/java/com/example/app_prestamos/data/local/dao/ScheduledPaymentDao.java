package com.example.app_prestamos.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.app_prestamos.data.local.entity.ScheduledPaymentEntity;
import com.example.app_prestamos.data.local.model.CollectionDueItem;

import java.util.List;

@Dao
public interface ScheduledPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrReplace(ScheduledPaymentEntity payment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertOrReplaceAll(List<ScheduledPaymentEntity> payments);

    @Update
    int update(ScheduledPaymentEntity payment);

    @Query("SELECT * FROM scheduled_payments WHERE id = :paymentId LIMIT 1")
    ScheduledPaymentEntity getById(long paymentId);

    @Query("SELECT * FROM scheduled_payments WHERE loan_id = :loanId ORDER BY installment_number ASC")
    List<ScheduledPaymentEntity> getPaymentsByLoan(long loanId);

    @Query(
            "SELECT * FROM scheduled_payments " +
                    "WHERE status = 'PENDING' AND due_date_epoch_day <= :todayEpochDay " +
                    "ORDER BY due_date_epoch_day ASC"
    )
    List<ScheduledPaymentEntity> getPendingPaymentsUntil(long todayEpochDay);

    @Query(
            "SELECT " +
                    "scheduled_payments.id AS payment_id, " +
                    "loans.id AS loan_id, " +
                    "clients.id AS client_id, " +
                    "clients.first_name AS first_name, " +
                    "clients.last_name AS last_name, " +
                    "clients.phone AS phone, " +
                    "scheduled_payments.installment_number AS installment_number, " +
                    "loans.installment_count AS installment_count, " +
                    "scheduled_payments.amount_cents AS amount_cents, " +
                    "loans.currency_code AS currency_code, " +
                    "scheduled_payments.due_date_epoch_day AS due_date_epoch_day, " +
                    "loans.payment_frequency AS payment_frequency " +
                    "FROM scheduled_payments " +
                    "INNER JOIN loans ON loans.id = scheduled_payments.loan_id " +
                    "INNER JOIN clients ON clients.id = loans.client_id " +
                    "WHERE scheduled_payments.status = 'PENDING' " +
                    "AND scheduled_payments.due_date_epoch_day = :selectedEpochDay " +
                    "AND loans.status = 'ACTIVE' " +
                    "AND clients.is_active = 1 " +
                    "ORDER BY clients.first_name ASC, scheduled_payments.installment_number ASC"
    )
    List<CollectionDueItem> getCollectionsDueOn(long selectedEpochDay);

    @Query(
            "UPDATE scheduled_payments " +
                    "SET status = 'PAID', paid_at_millis = :paidAtMillis, updated_at_millis = :updatedAtMillis " +
                    "WHERE id = :paymentId"
    )
    int markAsPaid(long paymentId, long paidAtMillis, long updatedAtMillis);

    @Query(
            "UPDATE scheduled_payments " +
                    "SET status = 'SKIPPED', paid_at_millis = NULL, updated_at_millis = :updatedAtMillis " +
                    "WHERE id = :paymentId"
    )
    int markAsSkipped(long paymentId, long updatedAtMillis);

    @Query(
            "UPDATE scheduled_payments " +
                    "SET status = 'PENDING', paid_at_millis = NULL, updated_at_millis = :updatedAtMillis " +
                    "WHERE id = :paymentId"
    )
    int markAsPending(long paymentId, long updatedAtMillis);

    @Query(
            "SELECT " +
                    "scheduled_payments.id AS payment_id, " +
                    "loans.id AS loan_id, " +
                    "clients.id AS client_id, " +
                    "clients.first_name AS first_name, " +
                    "clients.last_name AS last_name, " +
                    "clients.phone AS phone, " +
                    "scheduled_payments.installment_number AS installment_number, " +
                    "loans.installment_count AS installment_count, " +
                    "scheduled_payments.amount_cents AS amount_cents, " +
                    "loans.currency_code AS currency_code, " +
                    "scheduled_payments.due_date_epoch_day AS due_date_epoch_day, " +
                    "loans.payment_frequency AS payment_frequency " +
                    "FROM scheduled_payments " +
                    "INNER JOIN loans ON loans.id = scheduled_payments.loan_id " +
                    "INNER JOIN clients ON clients.id = loans.client_id " +
                    "WHERE scheduled_payments.status = 'PENDING' " +
                    "AND scheduled_payments.due_date_epoch_day <= :todayEpochDay " +
                    "AND loans.status = 'ACTIVE' " +
                    "AND clients.is_active = 1 " +
                    "ORDER BY scheduled_payments.due_date_epoch_day ASC, clients.first_name ASC"
    )
    List<CollectionDueItem> getCollectionsDueUntil(long todayEpochDay);
}
