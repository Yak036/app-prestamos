package com.example.app_prestamos.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.app_prestamos.data.local.dao.ClientDao;
import com.example.app_prestamos.data.local.dao.LoanDao;
import com.example.app_prestamos.data.local.dao.ScheduledPaymentDao;
import com.example.app_prestamos.data.local.entity.ClientEntity;
import com.example.app_prestamos.data.local.entity.LoanEntity;
import com.example.app_prestamos.data.local.entity.ScheduledPaymentEntity;

@Database(
        entities = {
                ClientEntity.class,
                LoanEntity.class,
                ScheduledPaymentEntity.class
        },
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "app_prestamos.db";

    private static volatile AppDatabase instance;

    public abstract ClientDao clientDao();

    public abstract LoanDao loanDao();

    public abstract ScheduledPaymentDao scheduledPaymentDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    // Usa applicationContext para evitar filtrar una Activity en memoria.
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .build();
                }
            }
        }
        return instance;
    }
}
