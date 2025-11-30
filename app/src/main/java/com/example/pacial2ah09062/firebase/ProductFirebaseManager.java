package com.example.pacial2ah09062.firebase;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.pacial2ah09062.database.entity.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductFirebaseManager {

    private static final String TAG = "ProductFirebaseManager";
    private static final String PRODUCTS_COLLECTION = "products";
    private static final long FIREBASE_TIMEOUT_MS = 5000; // 5 segundos

    private final FirebaseFirestore db;
    private final Handler timeoutHandler;

    public ProductFirebaseManager() {
        db = FirebaseFirestore.getInstance();
        timeoutHandler = new Handler(Looper.getMainLooper());
    }

    public interface ProductsCallback {
        void onSuccess(List<Product> products);
        void onFailure(String error);
    }

    public void fetchAllProducts(ProductsCallback callback) {
        final boolean[] callbackExecuted = {false};

        Runnable timeoutRunnable = () -> {
            synchronized (callbackExecuted) {
                if (!callbackExecuted[0]) {
                    callbackExecuted[0] = true;
                    Log.w(TAG, "Timeout obteniendo productos de Firebase");
                    callback.onFailure("Sin conexión a internet");
                }
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable, FIREBASE_TIMEOUT_MS);

        db.collection(PRODUCTS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);

                            List<Product> products = new ArrayList<>();
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Product product = documentToProduct(doc);
                                if (product != null) {
                                    products.add(product);
                                }
                            }
                            Log.d(TAG, "Productos descargados de Firebase: " + products.size());
                            callback.onSuccess(products);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.e(TAG, "Error obteniendo productos de Firebase", e);
                            callback.onFailure(e.getMessage());
                        }
                    }
                });
    }

    private Product documentToProduct(DocumentSnapshot doc) {
        try {
            String id = doc.getId();
            String name = doc.getString("name");
            String description = doc.getString("description");
            Double price = doc.getDouble("price");
            String imagePath = doc.getString("imagePath");
            Boolean isAvailable = doc.getBoolean("isAvailable");

            if (id != null && name != null && price != null) {
                Product product = new Product(id, name, description != null ? description : "", price,
                        imagePath != null ? imagePath : "", isAvailable == null || isAvailable);
                return product;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo documento a Product", e);
        }
        return null;
    }
}
