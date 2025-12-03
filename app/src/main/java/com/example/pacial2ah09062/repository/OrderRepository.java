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

    public interface OrdersCallback {
        void onSuccess(List<Order> orders);
        void onFailure(String error);
    }

    public interface OrderDetailsCallback {
        void onSuccess(Order order, List<OrderItem> orderItems);
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
     * Crea un pedido a partir del carrito del usuario con dirección de entrega.
     * Guarda siempre en Room. Si falla la sync con Firebase, el pedido queda con pendingSync=true.
     */
    public void placeOrder(String userEmail, String deliveryAddress, OrderPlacementCallback callback) {
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

                Order order = new Order(orderId, userEmail, "PENDING", deliveryAddress, total);
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

    /**
     * Obtiene todos los pedidos de un usuario ordenados por fecha de creación (más reciente primero)
     * Primero intenta sincronizar desde Firebase, luego devuelve los pedidos locales
     */
    public void getUserOrders(String userEmail, OrdersCallback callback) {
        // Primero intentar sincronizar desde Firebase
        syncOrdersFromFirebase(userEmail, new OrdersCallback() {
            @Override
            public void onSuccess(List<Order> firebaseOrders) {
                // Firebase sync exitoso, devolver pedidos actualizados
                executorService.execute(() -> {
                    try {
                        List<Order> localOrders = orderDAO.getOrdersByUser(userEmail);
                        callback.onSuccess(localOrders != null ? localOrders : new ArrayList<>());
                    } catch (Exception e) {
                        Log.e(TAG, "Error obteniendo pedidos locales después de sync", e);
                        callback.onFailure("Error cargando pedidos: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                // Firebase falló, devolver pedidos locales
                Log.w(TAG, "No se pudieron sincronizar pedidos desde Firebase: " + error);
                executorService.execute(() -> {
                    try {
                        List<Order> orders = orderDAO.getOrdersByUser(userEmail);
                        callback.onSuccess(orders != null ? orders : new ArrayList<>());
                    } catch (Exception e) {
                        Log.e(TAG, "Error obteniendo pedidos del usuario", e);
                        callback.onFailure("Error cargando pedidos: " + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * Obtiene los detalles completos de un pedido específico
     */
    public void getOrderDetails(String orderId, OrderDetailsCallback callback) {
        executorService.execute(() -> {
            try {
                Order order = orderDAO.getOrderById(orderId);
                if (order == null) {
                    callback.onFailure("Pedido no encontrado");
                    return;
                }
                
                List<OrderItem> orderItems = orderDAO.getOrderItems(orderId);
                callback.onSuccess(order, orderItems != null ? orderItems : new ArrayList<>());
            } catch (Exception e) {
                Log.e(TAG, "Error obteniendo detalles del pedido", e);
                callback.onFailure("Error cargando detalles: " + e.getMessage());
            }
        });
    }

    /**
     * Sincroniza pedidos desde Firebase y actualiza la base de datos local
     */
    private void syncOrdersFromFirebase(String userEmail, OrdersCallback callback) {
        orderFirebaseManager.getUserOrdersFromFirebase(userEmail, new OrderFirebaseManager.OrdersListCallback() {
            @Override
            public void onSuccess(List<Order> firebaseOrders) {
                executorService.execute(() -> {
                    try {
                        // Actualizar pedidos existentes en la base de datos local
                        for (Order firebaseOrder : firebaseOrders) {
                            Order localOrder = orderDAO.getOrderById(firebaseOrder.getId());
                            if (localOrder != null) {
                                // Actualizar estado y otros campos desde Firebase
                                localOrder.setStatus(firebaseOrder.getStatus());
                                localOrder.setUpdatedAt(firebaseOrder.getUpdatedAt());
                                localOrder.setPendingSync(false);
                                orderDAO.insertOrder(localOrder); // Room usará REPLACE
                                Log.d(TAG, "Pedido actualizado: " + localOrder.getId() + " -> " + localOrder.getStatus());
                            } else {
                                // Pedido no existe localmente, insertarlo
                                orderDAO.insertOrder(firebaseOrder);
                                Log.d(TAG, "Pedido insertado desde Firebase: " + firebaseOrder.getId());
                            }
                        }
                        callback.onSuccess(firebaseOrders);
                    } catch (Exception e) {
                        Log.e(TAG, "Error sincronizando pedidos desde Firebase", e);
                        callback.onFailure("Error actualizando pedidos locales");
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    /**
     * Fuerza la sincronización de pedidos desde Firebase (para pull-to-refresh)
     */
    public void refreshOrdersFromFirebase(String userEmail, OrdersCallback callback) {
        syncOrdersFromFirebase(userEmail, callback);
    }
}
