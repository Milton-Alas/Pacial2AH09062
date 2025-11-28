package com.example.pacial2ah09062.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class Order {

    @PrimaryKey
    @NonNull
    private String id;

    private String userEmail;
    private String status; // PENDING, CONFIRMED, PREPARING, DELIVERING, DELIVERED, CANCELLED
    private double total;
    private long createdAt;
    private long updatedAt;
    private boolean pendingSync;

    public Order() {
    }

    public Order(@NonNull String id, String userEmail, String status, double total) {
        long now = System.currentTimeMillis();
        this.id = id;
        this.userEmail = userEmail;
        this.status = status;
        this.total = total;
        this.createdAt = now;
        this.updatedAt = now;
        this.pendingSync = true;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPendingSync() {
        return pendingSync;
    }

    public void setPendingSync(boolean pendingSync) {
        this.pendingSync = pendingSync;
    }
}
