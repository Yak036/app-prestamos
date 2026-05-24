package com.example.app_prestamos.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.app_prestamos.data.local.entity.LoanEntity;

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

    @Query("UPDATE loans SET status = :status, updated_at_millis = :updatedAtMillis WHERE id = :loanId")
    int updateStatus(long loanId, String status, long updatedAtMillis);
}
