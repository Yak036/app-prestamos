package com.example.app_prestamos.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.app_prestamos.data.local.entity.ClientEntity;

import java.util.List;

@Dao
public interface ClientDao {

    @Insert
    long insert(ClientEntity client);

    @Update
    int update(ClientEntity client);

    @Query("SELECT * FROM clients WHERE id = :clientId LIMIT 1")
    ClientEntity getById(long clientId);

    @Query("SELECT * FROM clients WHERE is_active = 1 ORDER BY first_name ASC, last_name ASC")
    List<ClientEntity> getActiveClients();

    @Query(
            "SELECT * FROM clients " +
                    "WHERE first_name LIKE '%' || :query || '%' " +
                    "OR last_name LIKE '%' || :query || '%' " +
                    "OR phone LIKE '%' || :query || '%' " +
                    "ORDER BY first_name ASC, last_name ASC"
    )
    List<ClientEntity> search(String query);

    @Query("UPDATE clients SET is_active = 0, updated_at_millis = :updatedAtMillis WHERE id = :clientId")
    int deactivate(long clientId, long updatedAtMillis);
}
