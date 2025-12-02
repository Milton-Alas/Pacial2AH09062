package com.example.pacial2ah09062.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.pacial2ah09062.database.entity.Order;
import com.example.pacial2ah09062.database.entity.OrderItem;

import java.util.List;

@Dao
public interface OrderDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrder(Order order);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrderItems(List<OrderItem> items);

    @Transaction
    default void insertOrderWithItems(Order order, List<OrderItem> items) {
        insertOrder(order);
        insertOrderItems(items);
    }

    @Query("SELECT * FROM orders WHERE userEmail = :email ORDER BY createdAt DESC")
    List<Order> getOrdersByUser(String email);

    @Query("SELECT * FROM orders WHERE pendingSync = 1")
    List<Order> getPendingSyncOrders();

    @Query("UPDATE orders SET pendingSync = :pendingSync, status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    void updateOrderSyncStatus(String orderId, boolean pendingSync, String status, long updatedAt);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    List<OrderItem> getItemsForOrder(String orderId);

    @Query("SELECT * FROM orders WHERE id = :orderId")
    Order getOrderById(String orderId);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    List<OrderItem> getOrderItems(String orderId);
}
