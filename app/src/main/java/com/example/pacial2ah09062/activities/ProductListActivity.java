package com.example.pacial2ah09062.activities;

import android.os.Bundle;
import android.view.View;
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
import com.example.pacial2ah09062.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductListActivity extends AppCompatActivity implements ProductListAdapter.OnProductClickListener {

    private RecyclerView recyclerProducts;
    private ProductListAdapter adapter;

    private ProductDAO productDAO;
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerProducts = findViewById(R.id.recyclerProducts);
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductListAdapter(new ArrayList<>(), this);
        recyclerProducts.setAdapter(adapter);

        AppDatabase db = AppDatabase.getInstance(this);
        productDAO = db.productDAO();
        cartDAO = db.cartDAO();
        preferenceManager = new PreferenceManager(this);
        executorService = Executors.newSingleThreadExecutor();

        currentUserEmail = preferenceManager.getCurrentUserEmail();
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Debe iniciar sesión para ver productos", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadProducts();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadProducts() {
        executorService.execute(() -> {
            List<Product> products = productDAO.getAllProducts();
            if (products == null || products.isEmpty()) {
                products = createSampleProducts();
                productDAO.insertProducts(products);
            }

            final List<Product> finalProducts = products;
            runOnUiThread(() -> adapter.setProducts(finalProducts));
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
}
