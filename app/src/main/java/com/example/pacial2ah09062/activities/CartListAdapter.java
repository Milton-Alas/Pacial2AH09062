package com.example.pacial2ah09062.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.model.CartItemWithProduct;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.CartViewHolder> {

    public interface OnCartItemActionListener {
        void onRemoveItemClicked(CartItemWithProduct item);
    }

    private List<CartItemWithProduct> items;
    private final OnCartItemActionListener listener;

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
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView tvProductName, tvQuantity, tvSubtotal;
        MaterialButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvCartProductName);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvCartSubtotal);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }

        public void bind(final CartItemWithProduct item, final OnCartItemActionListener listener) {
            tvProductName.setText(item.getProductName());
            tvQuantity.setText("Cantidad: " + item.getQuantity());
            tvSubtotal.setText(String.format("Subtotal: $ %.2f", item.getSubtotal()));

            btnRemove.setOnClickListener(v -> listener.onRemoveItemClicked(item));
        }
    }
}
