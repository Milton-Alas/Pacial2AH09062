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
import com.example.pacial2ah09062.model.CartItemWithProduct;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.CartViewHolder> {

    public interface OnCartItemActionListener {
        void onRemoveItemClicked(CartItemWithProduct item);
    }

    private List<CartItemWithProduct> items;
    private final OnCartItemActionListener listener;
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public CartListAdapter(List<CartItemWithProduct> items, OnCartItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<CartItemWithProduct> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemWithProduct item = items.get(position);
        holder.bind(item, listener, firebaseStorage);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView tvProductName, tvQuantity, tvSubtotal;
        MaterialButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgCartProduct);
            tvProductName = itemView.findViewById(R.id.tvCartProductName);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvCartSubtotal);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }

        public void bind(final CartItemWithProduct item, final OnCartItemActionListener listener, final FirebaseStorage firebaseStorage) {
            tvProductName.setText(item.getProductName());
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvSubtotal.setText(String.format("$ %.2f", item.getSubtotal()));

            // Cargar imagen desde Firebase Storage
            String imagePath = item.getProduct().getImagePath();
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

            btnRemove.setOnClickListener(v -> listener.onRemoveItemClicked(item));
        }
    }
}
