package com.example.pacial2ah09062.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.entity.Product;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onAddToCartClicked(Product product);
    }

    private List<Product> products;
    private final OnProductClickListener listener;
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public ProductListAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener, firebaseStorage);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView tvName, tvDescription, tvPrice;
        MaterialButton btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvDescription = itemView.findViewById(R.id.tvProductDescription);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }

        public void bind(final Product product, final OnProductClickListener listener, final FirebaseStorage firebaseStorage) {
            tvName.setText(product.getName());
            tvDescription.setText(product.getDescription());
            tvPrice.setText(String.format("$ %.2f", product.getPrice()));

            String imagePath = product.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                StorageReference ref = firebaseStorage.getReference().child(imagePath);
                ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> Glide.with(imgProduct.getContext())
                                .load(uri)
                                .placeholder(R.drawable.chivoeats)
                                .into(imgProduct))
                        .addOnFailureListener(e -> imgProduct.setImageResource(R.drawable.chivoeats));
            } else {
                imgProduct.setImageResource(R.drawable.chivoeats);
            }

            btnAddToCart.setOnClickListener(v -> listener.onAddToCartClicked(product));
        }
    }
}
