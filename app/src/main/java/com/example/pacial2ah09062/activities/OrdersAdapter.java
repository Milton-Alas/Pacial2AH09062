package com.example.pacial2ah09062.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.entity.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onViewDetailClicked(Order order);
    }

    private List<Order> orders;
    private OnOrderClickListener listener;
    private SimpleDateFormat dateFormat;

    public OrdersAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvDeliveryAddress, tvOrderTotal;
        private MaterialButton btnViewDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }

        public void bind(Order order, OnOrderClickListener listener) {
            // Mostrar ID corto del pedido
            String shortId = order.getId().length() > 8 ? 
                order.getId().substring(0, 8) + "..." : order.getId();
            tvOrderId.setText("Pedido #" + shortId);

            // Formatear fecha
            Date date = new Date(order.getCreatedAt());
            tvOrderDate.setText(dateFormat.format(date));

            // Estado del pedido con traducción
            String statusText = getStatusText(order.getStatus());
            tvOrderStatus.setText(statusText);

            // Color del estado
            int statusColor = getStatusColor(order.getStatus());
            tvOrderStatus.setTextColor(statusColor);

            // Dirección de entrega
            tvDeliveryAddress.setText(order.getDeliveryAddress());

            // Total
            tvOrderTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", order.getTotal()));

            // Listener del botón
            btnViewDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailClicked(order);
                }
            });
        }

        private String getStatusText(String status) {
            switch (status.toUpperCase()) {
                case "PENDING":
                    return "Pendiente";
                case "PREPARING":
                    return "En preparación";
                case "ON_WAY":
                    return "En camino";
                case "DELIVERED":
                    return "Entregado";
                default:
                    return status;
            }
        }

        private int getStatusColor(String status) {
            switch (status.toUpperCase()) {
                case "PENDING":
                    return 0xFFFF9800; // Orange
                case "PREPARING":
                    return 0xFF2196F3; // Blue
                case "ON_WAY":
                    return 0xFF9C27B0; // Purple
                case "DELIVERED":
                    return 0xFF4CAF50; // Green
                default:
                    return 0xFF757575; // Gray
            }
        }
    }
}