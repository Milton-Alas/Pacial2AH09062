package com.example.pacial2ah09062.repository;

import android.content.Context;
import android.util.Log;

import com.example.pacial2ah09062.database.AppDatabase;
import com.example.pacial2ah09062.database.dao.CartDAO;
import com.example.pacial2ah09062.database.dao.OrderDAO;
import com.example.pacial2ah09062.database.dao.ProductDAO;
import com.example.pacial2ah09062.database.entity.CartItem;
import com.example.pacial2ah09062.database.entity.Order;
import com.example.pacial2ah09062.database.entity.OrderItem;
import com.example.pacial2ah09062.database.entity.Product;
import com.example.pacial2ah09062.firebase.OrderFirebaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderRepository {

    private static final String TAG = "OrderRepository";

    public interface OrderPlacementCallback {
        void onSuccess(Order order, boolean syncedWithServer);
        void onFailure(String error);
    }

    private static volatile OrderRepository INSTANCE;

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;
    private final OrderFirebaseManager orderFirebaseManager;
    private final ExecutorService executorService;

    private OrderRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        this.orderDAO = db.orderDAO();
        this.cartDAO = db.cartDAO();
        this.productDAO = db.productDAO();
        this.orderFirebaseManager = new OrderFirebaseManager();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public static OrderRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (OrderRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OrderRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Crea un pedido a partir del carrito del usuario.
     * Guarda siempre en Room. Si falla la sync con Firebase, el pedido queda con pendingSync=true.
     */
    public void placeOrder(String userEmail, OrderPlacementCallback callback) {
        executorService.execute(() -> {
            try {
                List<CartItem> cartItems = cartDAO.getCartItemsByUser(userEmail);
                if (cartItems == null || cartItems.isEmpty()) {
                    callback.onFailure("El carrito está vacío");
                    return;
                }

                String orderId = UUID.randomUUID().toString();
                long now = System.currentTimeMillis();

                List<OrderItem> orderItems = new ArrayList<>();
                double total = 0.0;

                for (CartItem cartItem : cartItems) {
                    Product product = productDAO.getProductById(cartItem.getProductId());
                    if (product == null) {
                        continue;
                    }
                    double unitPrice = product.getPrice();
                    int quantity = cartItem.getQuantity();
                    double subtotal = unitPrice * quantity;
                    total += subtotal;

                    OrderItem orderItem = new OrderItem(orderId, product.getId(), quantity, unitPrice);
                    orderItems.add(orderItem);
                }

                if (orderItems.isEmpty()) {
                    callback.onFailure("No se pudieron convertir los productos del carrito en ítems de pedido");
                    return;
                }

                Order order = new Order(orderId, userEmail, "PENDING", total);
                order.setCreatedAt(now);
                order.setUpdatedAt(now);
                order.setPendingSync(true);

                // Guardar en Room
                orderDAO.insertOrderWithItems(order, orderItems);
                // Vaciar carrito
                cartDAO.clearCart(userEmail);

                // Intentar sincronizar con Firebase
                orderFirebaseManager.createOrder(order, orderItems, new OrderFirebaseManager.OrderCallback() {
                    @Override
                    public void onSuccess(Order firebaseOrder) {
                        long updatedAt = System.currentTimeMillis();
                        orderDAO.updateOrderSyncStatus(orderId, false, "CONFIRMED", updatedAt);
                        order.setPendingSync(false);
                        order.setStatus("CONFIRMED");
                        order.setUpdatedAt(updatedAt);
                        callback.onSuccess(order, true);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.w(TAG, "Pedido guardado localmente pero no sincronizado: " + error);
                        // Pedido queda con pendingSync=true
                        callback.onSuccess(order, false);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error creando pedido", e);
                callback.onFailure("Error creando pedido: " + e.getMessage());
            }
        });
    }
}
