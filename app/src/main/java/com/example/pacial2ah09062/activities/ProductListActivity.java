package com.example.pacial2ah09062.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.AppDatabase;
import com.example.pacial2ah09062.database.dao.CartDAO;
import com.example.pacial2ah09062.database.entity.CartItem;
import com.example.pacial2ah09062.database.entity.Product;
import com.example.pacial2ah09062.repository.ProductRepository;
import com.example.pacial2ah09062.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductListActivity extends AppCompatActivity implements ProductListAdapter.OnProductClickListener {

    private RecyclerView recyclerProducts;
    private ProductListAdapter adapter;

    private ProductRepository productRepository;
    private CartDAO cartDAO;
    private PreferenceManager preferenceManager;
    private ExecutorService executorService;

    private String currentUserEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Productos");
            // No mostrar botón de retroceso ya que es la pantalla principal
        }

        recyclerProducts = findViewById(R.id.recyclerProducts);
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductListAdapter(new ArrayList<>(), this);
        recyclerProducts.setAdapter(adapter);

        AppDatabase db = AppDatabase.getInstance(this);
        cartDAO = db.cartDAO();
        preferenceManager = new PreferenceManager(this);
        executorService = Executors.newSingleThreadExecutor();
        productRepository = ProductRepository.getInstance(this);

        currentUserEmail = preferenceManager.getCurrentUserEmail();
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Debe iniciar sesión para ver productos", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Configurar manejo del botón atrás
        setupBackPressedCallback();
        
        loadProducts();
    }


    private void loadProducts() {
        productRepository.getProducts(new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                runOnUiThread(() -> adapter.setProducts(products));
            }

            @Override
            public void onFailure(String error) {
                // Si no hay datos remotos ni locales, usamos productos de ejemplo como fallback
                if (adapter.getItemCount() == 0) {
                    executorService.execute(() -> {
                        List<Product> sample = createSampleProducts();
                        AppDatabase db = AppDatabase.getInstance(ProductListActivity.this);
                        db.productDAO().insertProducts(sample);
                        runOnUiThread(() -> adapter.setProducts(sample));
                    });
                }
                runOnUiThread(() -> Toast.makeText(ProductListActivity.this, error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private List<Product> createSampleProducts() {
        List<Product> sample = new ArrayList<>();
        sample.add(new Product("1", "Hamburguesa Clásica", "Carne 100% res, lechuga, tomate y salsa especial", 4.99, "products/hamburguesa_clasica.jpg", true));
        sample.add(new Product("2", "Papas Fritas", "Porción mediana de papas fritas crujientes", 1.99, "products/papas_fritas.jpg", true));
        sample.add(new Product("3", "Combo ChivoEats", "Hamburguesa, papas y bebida", 6.99, "products/combo_chivoeats.jpg", true));
        return sample;
    }

    @Override
    public void onAddToCartClicked(Product product) {
        if (currentUserEmail == null) {
            Toast.makeText(this, "Error: usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            List<CartItem> currentCart = cartDAO.getCartItemsByUser(currentUserEmail);
            CartItem existing = null;
            for (CartItem item : currentCart) {
                if (product.getId().equals(item.getProductId())) {
                    existing = item;
                    break;
                }
            }

            if (existing == null) {
                existing = new CartItem(currentUserEmail, product.getId(), 1);
            } else {
                existing.setQuantity(existing.getQuantity() + 1);
            }

            cartDAO.insertOrUpdate(existing);

            runOnUiThread(() -> Toast.makeText(ProductListActivity.this, "Agregado al carrito", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cart) {
            Intent intent = new Intent(ProductListActivity.this, CartActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            Intent intent = new Intent(ProductListActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_orders) {
            Intent intent = new Intent(ProductListActivity.this, OrdersActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(ProductListActivity.this, HomeActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        preferenceManager.logout();
        Intent intent = new Intent(ProductListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBackPressedCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Mostrar mensaje informativo en lugar de salir
                Toast.makeText(ProductListActivity.this, "Use el menú para navegar o cerrar sesión", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
