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
import com.example.pacial2ah09062.database.dao.CartDAO;
import com.example.pacial2ah09062.database.dao.ProductDAO;
import com.example.pacial2ah09062.database.entity.CartItem;
import com.example.pacial2ah09062.database.entity.Product;
import com.example.pacial2ah09062.model.CartItemWithProduct;
import com.example.pacial2ah09062.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity implements CartListAdapter.OnCartItemActionListener {

    private RecyclerView recyclerCart;
    private TextView tvCartTotal;
    private MaterialButton btnPlaceOrder;

    private CartListAdapter adapter;
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private PreferenceManager preferenceManager;
    private ExecutorService executorService;
    private String currentUserEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Carrito");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerCart = findViewById(R.id.recyclerCart);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartListAdapter(new ArrayList<>(), this);
        recyclerCart.setAdapter(adapter);

        AppDatabase db = AppDatabase.getInstance(this);
        cartDAO = db.cartDAO();
        productDAO = db.productDAO();
        preferenceManager = new PreferenceManager(this);
        executorService = Executors.newSingleThreadExecutor();

        currentUserEmail = preferenceManager.getCurrentUserEmail();
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Debe iniciar sesión para ver el carrito", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnPlaceOrder.setOnClickListener(v -> {
            // Fase 2: solo mostramos un mensaje; la creación real de órdenes se hará en la fase 3
            Toast.makeText(CartActivity.this, "Función de realizar pedido se implementará en la siguiente fase", Toast.LENGTH_LONG).show();
        });

        loadCart();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadCart() {
        executorService.execute(() -> {
            List<CartItem> cartItems = cartDAO.getCartItemsByUser(currentUserEmail);
            List<CartItemWithProduct> itemsWithProduct = new ArrayList<>();
            double total = 0.0;

            for (CartItem item : cartItems) {
                Product product = productDAO.getProductById(item.getProductId());
                if (product != null) {
                    CartItemWithProduct cwp = new CartItemWithProduct(item, product);
                    itemsWithProduct.add(cwp);
                    total += cwp.getSubtotal();
                }
            }

            double finalTotal = total;
            runOnUiThread(() -> {
                adapter.setItems(itemsWithProduct);
                tvCartTotal.setText(String.format(Locale.getDefault(), "Total: $ %.2f", finalTotal));
            });
        });
    }

    @Override
    public void onRemoveItemClicked(CartItemWithProduct item) {
        executorService.execute(() -> {
            cartDAO.deleteItem(item.getCartItem().getId());
            loadCart();
        });
    }
}
