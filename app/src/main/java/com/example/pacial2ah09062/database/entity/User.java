package com.example.pacial2ah09062.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    
    @PrimaryKey
    @NonNull
    private String email;
    
    private String fullName;
    private String password;
    private boolean pendingSync; // Para manejo offline
    private long lastUpdated;
    
    public User() {
    }
    
    public User(@NonNull String email, String fullName, String password) {
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.pendingSync = false;
        this.lastUpdated = System.currentTimeMillis();
    }
    
    // Getters y Setters
    @NonNull
    public String getEmail() {
        return email;
    }
    
    public void setEmail(@NonNull String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isPendingSync() {
        return pendingSync;
    }
    
    public void setPendingSync(boolean pendingSync) {
        this.pendingSync = pendingSync;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", pendingSync=" + pendingSync +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
