package com.example.app_prestamos.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.app_prestamos.data.local.entity.LoanEntity;
import com.example.app_prestamos.data.local.model.LoanListItem;

import java.util.List;

@Dao
public interface LoanDao {

    @Insert
    long insert(LoanEntity loan);

    @Update
    int update(LoanEntity loan);

    @Query("SELECT * FROM loans WHERE id = :loanId LIMIT 1")
    LoanEntity getById(long loanId);

    @Query("SELECT * FROM loans WHERE client_id = :clientId ORDER BY created_at_millis DESC")
    List<LoanEntity> getLoansByClient(long clientId);

    @Query("SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY created_at_millis DESC")
    List<LoanEntity> getActiveLoans();

    @Query(
            "SELECT " +
                    "loans.id AS loan_id, " +
                    "clients.first_name AS first_name, " +
                    "clients.last_name AS last_name, " +
                    "loans.principal_amount_cents AS principal_amount_cents, " +
                    "loans.total_receivable_cents AS total_receivable_cents, " +
                    "loans.currency_code AS currency_code, " +
                    "loans.profit_percent AS profit_percent, " +
                    "loans.installment_count AS installment_count, " +
                    "loans.payment_frequency AS payment_frequency " +
                    "FROM loans " +
                    "INNER JOIN clients ON clients.id = loans.client_id " +
                    "WHERE loans.status = 'ACTIVE' " +
                    "ORDER BY loans.created_at_millis DESC"
    )
    List<LoanListItem> getActiveLoanListItems();

    @Query("UPDATE loans SET status = :status, updated_at_millis = :updatedAtMillis WHERE id = :loanId")
    int updateStatus(long loanId, String status, long updatedAtMillis);
}
