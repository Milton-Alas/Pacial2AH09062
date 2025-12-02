package com.example.pacial2ah09062.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.entity.OrderItem;
import com.example.pacial2ah09062.database.entity.Product;
import com.example.pacial2ah09062.database.dao.ProductDAO;

import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderItemViewHolder> {

    public static class OrderItemDetail {
        private OrderItem orderItem;
        private Product product;

        public OrderItemDetail(OrderItem orderItem, Product product) {
            this.orderItem = orderItem;
            this.product = product;
        }

        public OrderItem getOrderItem() { return orderItem; }
        public Product getProduct() { return product; }
    }

    private List<OrderItemDetail> orderItems;

    public OrderDetailAdapter(List<OrderItemDetail> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItemDetail itemDetail = orderItems.get(position);
        holder.bind(itemDetail);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public void setOrderItems(List<OrderItemDetail> newOrderItems) {
        this.orderItems = newOrderItems;
        notifyDataSetChanged();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName, tvProductPrice, tvQuantity, tvSubtotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }

        public void bind(OrderItemDetail itemDetail) {
            OrderItem orderItem = itemDetail.getOrderItem();
            Product product = itemDetail.getProduct();

            // Nombre del producto
            String productName = product != null ? product.getName() : "Producto no encontrado";
            tvProductName.setText(productName);

            // Precio unitario
            tvProductPrice.setText(String.format(Locale.getDefault(), "$%.2f c/u", orderItem.getUnitPrice()));

            // Cantidad
            tvQuantity.setText(String.format(Locale.getDefault(), "x%d", orderItem.getQuantity()));

            // Subtotal
            tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", orderItem.getSubtotal()));
        }
    }
}