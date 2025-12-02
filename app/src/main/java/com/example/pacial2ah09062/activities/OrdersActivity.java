package com.example.pacial2ah09062.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.entity.Order;
import com.example.pacial2ah09062.repository.OrderRepository;
import com.example.pacial2ah09062.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity implements OrdersAdapter.OnOrderClickListener {

    private RecyclerView recyclerOrders;
    private LinearLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefresh;
    private OrdersAdapter adapter;

    private OrderRepository orderRepository;
    private PreferenceManager preferenceManager;
    private String currentUserEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initViews();
        initRepository();
        loadOrders();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Pedidos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerOrders = findViewById(R.id.recyclerOrders);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(new ArrayList<>(), this);
        recyclerOrders.setAdapter(adapter);

        // Configurar SwipeRefreshLayout
        swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );
        swipeRefresh.setOnRefreshListener(this::refreshOrders);
    }

    private void initRepository() {
        orderRepository = OrderRepository.getInstance(this);
        preferenceManager = new PreferenceManager(this);
        currentUserEmail = preferenceManager.getCurrentUserEmail();

        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Debe iniciar sesión para ver sus pedidos", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadOrders() {
        orderRepository.getUserOrders(currentUserEmail, new OrderRepository.OrdersCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    if (orders.isEmpty()) {
                        showEmptyState();
                    } else {
                        showOrdersList(orders);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(OrdersActivity.this, error, Toast.LENGTH_LONG).show();
                    showEmptyState();
                });
            }
        });
    }

    private void refreshOrders() {
        swipeRefresh.setRefreshing(true);
        orderRepository.refreshOrdersFromFirebase(currentUserEmail, new OrderRepository.OrdersCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(OrdersActivity.this, "Pedidos actualizados", Toast.LENGTH_SHORT).show();
                    loadOrders(); // Recargar desde local para mostrar cambios
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(OrdersActivity.this, "Error sincronizando: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showEmptyState() {
        recyclerOrders.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void showOrdersList(List<Order> orders) {
        recyclerOrders.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        adapter.setOrders(orders);
    }

    @Override
    public void onViewDetailClicked(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("ORDER_ID", order.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar pedidos cuando regresamos de otra pantalla (por si cambió el estado)
        loadOrders();
    }
}