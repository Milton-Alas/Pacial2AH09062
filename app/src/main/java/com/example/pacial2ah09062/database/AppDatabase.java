package com.example.pacial2ah09062.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.pacial2ah09062.database.dao.CartDAO;
import com.example.pacial2ah09062.database.dao.OrderDAO;
import com.example.pacial2ah09062.database.dao.ProductDAO;
import com.example.pacial2ah09062.database.dao.UserDAO;
import com.example.pacial2ah09062.database.entity.CartItem;
import com.example.pacial2ah09062.database.entity.Order;
import com.example.pacial2ah09062.database.entity.OrderItem;
import com.example.pacial2ah09062.database.entity.Product;
import com.example.pacial2ah09062.database.entity.User;

@Database(entities = {User.class, Product.class, Order.class, OrderItem.class, CartItem.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "app_database";
    private static volatile AppDatabase INSTANCE;

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
