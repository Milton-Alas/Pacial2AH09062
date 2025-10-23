package com.example.pacial2ah09062.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import com.example.pacial2ah09062.database.entity.User;

import java.util.List;

@Dao
public interface UserDAO {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);
    
    @Update
    void updateUser(User user);
    
    @Delete
    void deleteUser(User user);
    
    @Query("SELECT * FROM users")
    List<User> getAllUsers();
    
    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE fullName LIKE '%' || :name || '%'")
    List<User> searchUsersByName(String name);
    
    @Query("SELECT * FROM users WHERE pendingSync = 1")
    List<User> getUsersPendingSync();
    
    @Query("UPDATE users SET pendingSync = :pendingSync WHERE email = :email")
    void updateSyncStatus(String email, boolean pendingSync);
    
    @Query("UPDATE users SET lastUpdated = :timestamp WHERE email = :email")
    void updateLastUpdated(String email, long timestamp);
    
    @Query("DELETE FROM users")
    void deleteAllUsers();
    
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
    
    @Query("DELETE FROM users WHERE email = :email")
    void deleteUserByEmail(String email);
}
