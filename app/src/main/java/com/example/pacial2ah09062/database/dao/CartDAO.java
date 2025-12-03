package com.example.pacial2ah09062.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pacial2ah09062.database.entity.CartItem;

import java.util.List;

@Dao
public interface CartDAO {

    @Query("SELECT * FROM cart_items WHERE userEmail = :email")
    List<CartItem> getCartItemsByUser(String email);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(CartItem item);

    @Query("DELETE FROM cart_items WHERE id = :id")
    void deleteItem(long id);

    @Query("DELETE FROM cart_items WHERE userEmail = :email")
    void clearCart(String email);
}
