package com.example.pacial2ah09062.repository;

import android.content.Context;
import android.util.Log;

import com.example.pacial2ah09062.database.AppDatabase;
import com.example.pacial2ah09062.database.dao.ProductDAO;
import com.example.pacial2ah09062.database.entity.Product;
import com.example.pacial2ah09062.firebase.ProductFirebaseManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {

    private static final String TAG = "ProductRepository";

    public interface ProductCallback {
        void onSuccess(List<Product> products);
        void onFailure(String error);
    }

    private static volatile ProductRepository INSTANCE;

    private final ProductDAO productDAO;
    private final ProductFirebaseManager productFirebaseManager;
    private final ExecutorService executorService;

    private ProductRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        this.productDAO = db.productDAO();
        this.productFirebaseManager = new ProductFirebaseManager();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public static ProductRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ProductRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ProductRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Obtiene productos locales y luego intenta refrescar desde Firestore.
     * El callback puede ser llamado más de una vez (primero con datos locales, luego con remotos).
     */
    public void getProducts(ProductCallback callback) {
        executorService.execute(() -> {
            List<Product> localProductsTemp;
            try {
                localProductsTemp = productDAO.getAllProducts();
            } catch (Exception e) {
                Log.e(TAG, "Error leyendo productos locales", e);
                callback.onFailure("Error leyendo productos locales: " + e.getMessage());
                localProductsTemp = Collections.emptyList();
            }

            final List<Product> localProducts = localProductsTemp;

            if (localProducts != null && !localProducts.isEmpty()) {
                callback.onSuccess(localProducts);
            }

            // Intentar refrescar desde Firestore
            productFirebaseManager.fetchAllProducts(new ProductFirebaseManager.ProductsCallback() {
                @Override
                public void onSuccess(List<Product> products) {
                    executorService.execute(() -> {
                        try {
                            productDAO.replaceAll(products);
                        } catch (Exception e) {
                            Log.e(TAG, "Error guardando productos remotos en Room", e);
                        }
                        callback.onSuccess(products);
                    });
                }

                @Override
                public void onFailure(String error) {
                    if (localProducts == null || localProducts.isEmpty()) {
                        callback.onFailure(error);
                    } else {
                        Log.w(TAG, "Fallo actualizando productos desde Firebase: " + error);
                    }
                }
            });
        });
    }
}
