package com.example.pacial2ah09062.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.AppDatabase;
import com.example.pacial2ah09062.database.dao.ProductDAO;
import com.example.pacial2ah09062.database.entity.Order;
import com.example.pacial2ah09062.database.entity.OrderItem;
import com.example.pacial2ah09062.database.entity.Product;
import com.example.pacial2ah09062.repository.OrderRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderStatus, tvOrderDate, tvDeliveryAddress, tvOrderTotal;
    private RecyclerView recyclerOrderItems;
    private OrderDetailAdapter adapter;

    private OrderRepository orderRepository;
    private ProductDAO productDAO;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat;
    
    private String orderId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();
        initRepository();
        getOrderIdFromIntent();
        loadOrderDetails();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalle del Pedido");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);

        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderDetailAdapter(new ArrayList<>());
        recyclerOrderItems.setAdapter(adapter);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    private void initRepository() {
        orderRepository = OrderRepository.getInstance(this);
        AppDatabase db = AppDatabase.getInstance(this);
        productDAO = db.productDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void getOrderIdFromIntent() {
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Error: ID de pedido no válido", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadOrderDetails() {
        orderRepository.getOrderDetails(orderId, new OrderRepository.OrderDetailsCallback() {
            @Override
            public void onSuccess(Order order, List<OrderItem> orderItems) {
                runOnUiThread(() -> displayOrderDetails(order, orderItems));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(OrderDetailActivity.this, error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void displayOrderDetails(Order order, List<OrderItem> orderItems) {
        // Mostrar información básica del pedido
        displayBasicOrderInfo(order);

        // Cargar y mostrar productos
        loadOrderProducts(orderItems);
    }

    private void displayBasicOrderInfo(Order order) {
        // ID del pedido (versión corta)
        String shortId = order.getId().length() > 8 ? 
            order.getId().substring(0, 8) + "..." : order.getId();
        tvOrderId.setText("#" + shortId);

        // Estado con traducción y color
        String statusText = getStatusText(order.getStatus());
        tvOrderStatus.setText(statusText);
        tvOrderStatus.setTextColor(getStatusColor(order.getStatus()));

        // Fecha formateada
        Date date = new Date(order.getCreatedAt());
        tvOrderDate.setText(dateFormat.format(date));

        // Dirección de entrega
        tvDeliveryAddress.setText(order.getDeliveryAddress());

        // Total
        tvOrderTotal.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotal()));
    }

    private void loadOrderProducts(List<OrderItem> orderItems) {
        executorService.execute(() -> {
            List<OrderDetailAdapter.OrderItemDetail> itemDetails = new ArrayList<>();

            for (OrderItem orderItem : orderItems) {
                Product product = productDAO.getProductById(orderItem.getProductId());
                OrderDetailAdapter.OrderItemDetail detail = 
                    new OrderDetailAdapter.OrderItemDetail(orderItem, product);
                itemDetails.add(detail);
            }

            runOnUiThread(() -> adapter.setOrderItems(itemDetails));
        });
    }

    private String getStatusText(String status) {
        switch (status.toUpperCase()) {
            case "PENDING":
                return "Pendiente";
            case "PREPARING":
                return "En preparación";
            case "ON_WAY":
                return "En camino";
            case "DELIVERED":
                return "Entregado";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status.toUpperCase()) {
            case "PENDING":
                return 0xFFFF9800; // Orange
            case "PREPARING":
                return 0xFF2196F3; // Blue
            case "ON_WAY":
                return 0xFF9C27B0; // Purple
            case "DELIVERED":
                return 0xFF4CAF50; // Green
            default:
                return 0xFF757575; // Gray
        }
    }
}