package com.example.pacial2ah09062.firebase;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.pacial2ah09062.database.entity.Order;
import com.example.pacial2ah09062.database.entity.OrderItem;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderFirebaseManager {

    private static final String TAG = "OrderFirebaseManager";
    private static final String ORDERS_COLLECTION = "orders";
    private static final String ITEMS_SUBCOLLECTION = "items";
    private static final long FIREBASE_TIMEOUT_MS = 5000; // 5 segundos

    private final FirebaseFirestore db;
    private final Handler timeoutHandler;

    public OrderFirebaseManager() {
        db = FirebaseFirestore.getInstance();
        timeoutHandler = new Handler(Looper.getMainLooper());
    }

    public interface OrderCallback {
        void onSuccess(Order order);
        void onFailure(String error);
    }

    public interface OrdersListCallback {
        void onSuccess(List<Order> orders);
        void onFailure(String error);
    }

    /**
     * Crea un pedido en Firestore con sus ítems en una subcolección.
     */
    public void createOrder(Order order, List<OrderItem> items, OrderCallback callback) {
        final boolean[] callbackExecuted = {false};

        Runnable timeoutRunnable = () -> {
            synchronized (callbackExecuted) {
                if (!callbackExecuted[0]) {
                    callbackExecuted[0] = true;
                    Log.w(TAG, "Timeout creando pedido en Firebase: " + order.getId());
                    callback.onFailure("Sin conexión a internet - pedido guardado localmente");
                }
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable, FIREBASE_TIMEOUT_MS);

        WriteBatch batch = db.batch();
        DocumentReference orderRef = db.collection(ORDERS_COLLECTION).document(order.getId());

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userEmail", order.getUserEmail());
        orderData.put("status", order.getStatus());
        orderData.put("deliveryAddress", order.getDeliveryAddress());
        orderData.put("total", order.getTotal());
        orderData.put("createdAt", order.getCreatedAt());
        orderData.put("updatedAt", order.getUpdatedAt());

        batch.set(orderRef, orderData);

        for (OrderItem item : items) {
            DocumentReference itemRef = orderRef.collection(ITEMS_SUBCOLLECTION).document();
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("productId", item.getProductId());
            itemData.put("quantity", item.getQuantity());
            itemData.put("unitPrice", item.getUnitPrice());
            itemData.put("subtotal", item.getSubtotal());
            batch.set(itemRef, itemData);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.d(TAG, "Pedido creado en Firebase: " + order.getId());
                            callback.onSuccess(order);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.e(TAG, "Error creando pedido en Firebase", e);
                            callback.onFailure(e.getMessage());
                        }
                    }
                });
    }

    /**
     * Obtiene todos los pedidos de un usuario desde Firebase
     */
    public void getUserOrdersFromFirebase(String userEmail, OrdersListCallback callback) {
        final boolean[] callbackExecuted = {false};

        Runnable timeoutRunnable = () -> {
            synchronized (callbackExecuted) {
                if (!callbackExecuted[0]) {
                    callbackExecuted[0] = true;
                    Log.w(TAG, "Timeout obteniendo pedidos de Firebase para: " + userEmail);
                    callback.onFailure("Sin conexión a internet");
                }
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable, FIREBASE_TIMEOUT_MS);

        db.collection(ORDERS_COLLECTION)
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            
                            List<Order> orders = new ArrayList<>();
                            queryDocumentSnapshots.forEach(document -> {
                                try {
                                    Order order = new Order();
                                    order.setId(document.getId());
                                    order.setUserEmail(document.getString("userEmail"));
                                    order.setStatus(document.getString("status"));
                                    order.setDeliveryAddress(document.getString("deliveryAddress"));
                                    
                                    Double total = document.getDouble("total");
                                    order.setTotal(total != null ? total : 0.0);
                                    
                                    Long createdAt = document.getLong("createdAt");
                                    order.setCreatedAt(createdAt != null ? createdAt : System.currentTimeMillis());
                                    
                                    Long updatedAt = document.getLong("updatedAt");
                                    order.setUpdatedAt(updatedAt != null ? updatedAt : System.currentTimeMillis());
                                    
                                    order.setPendingSync(false); // Viene de Firebase, está sincronizado
                                    
                                    orders.add(order);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parseando pedido desde Firebase", e);
                                }
                            });
                            
                            // Ordenar por createdAt descendente (más reciente primero)
                            orders.sort((o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
                            
                            Log.d(TAG, "Obtenidos " + orders.size() + " pedidos desde Firebase");
                            callback.onSuccess(orders);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.e(TAG, "Error obteniendo pedidos desde Firebase", e);
                            callback.onFailure(e.getMessage());
                        }
                    }
                });
    }
}
