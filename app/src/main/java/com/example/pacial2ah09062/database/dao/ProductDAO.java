package com.example.pacial2ah09062.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.pacial2ah09062.database.entity.Product;

import java.util.List;

@Dao
public interface ProductDAO {

    @Query("SELECT * FROM products ORDER BY name ASC")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getProductById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProduct(Product product);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<Product> products);

    @Query("DELETE FROM products")
    void clearAllProducts();

    @Transaction
    default void replaceAll(List<Product> products) {
        clearAllProducts();
        insertProducts(products);
    }
}
