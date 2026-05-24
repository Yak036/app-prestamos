package com.example.app_prestamos.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "clients",
        indices = {
                @Index(value = {"phone"})
        }
)
public class ClientEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    public String phone;

    public String address;

    public String notes;

    @ColumnInfo(name = "is_active")
    public boolean isActive = true;

    @ColumnInfo(name = "created_at_millis")
    public long createdAtMillis;

    @ColumnInfo(name = "updated_at_millis")
    public long updatedAtMillis;
}
