package com.example.pacial2ah09062.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;

import com.example.pacial2ah09062.database.dao.CartDAO;
import com.example.pacial2ah09062.database.dao.OrderDAO;
import com.example.pacial2ah09062.database.dao.ProductDAO;
import com.example.pacial2ah09062.database.dao.UserDAO;
import com.example.pacial2ah09062.database.entity.CartItem;
import com.example.pacial2ah09062.database.entity.Order;
import com.example.pacial2ah09062.database.entity.OrderItem;
import com.example.pacial2ah09062.database.entity.Product;
import com.example.pacial2ah09062.database.entity.User;

@Database(entities = {User.class, Product.class, Order.class, OrderItem.class, CartItem.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "app_database";
    private static volatile AppDatabase INSTANCE;

    // Migración de versión 2 a 3: agregar campo deliveryAddress a la tabla orders
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE orders ADD COLUMN deliveryAddress TEXT");
        }
    };

    // Migración de versión 1 a 3: para usuarios con versión muy antigua
    static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Si la tabla orders existe, agregar el campo deliveryAddress
            database.execSQL("ALTER TABLE orders ADD COLUMN deliveryAddress TEXT");
        }
    };

    public abstract UserDAO userDAO();

    public abstract ProductDAO productDAO();

    public abstract OrderDAO orderDAO();

    public abstract CartDAO cartDAO();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addMigrations(MIGRATION_2_3, MIGRATION_1_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
