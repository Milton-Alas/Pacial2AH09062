package com.example.pacial2ah09062.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.pacial2ah09062.R;

public class DeliveryAddressDialog {

    public interface OnAddressConfirmedListener {
        void onAddressConfirmed(String address);
    }

    private AlertDialog dialog;
    private TextInputLayout tilDeliveryAddress;
    private TextInputEditText etDeliveryAddress;
    private MaterialButton btnCancel, btnConfirm;
    private OnAddressConfirmedListener listener;

    public DeliveryAddressDialog(Context context, OnAddressConfirmedListener listener) {
        this.listener = listener;
        initDialog(context);
    }

    private void initDialog(Context context) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delivery_address, null);

        tilDeliveryAddress = dialogView.findViewById(R.id.tilDeliveryAddress);
        etDeliveryAddress = dialogView.findViewById(R.id.etDeliveryAddress);
        btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        setupListeners(context);
    }

    private void setupListeners(Context context) {
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String address = etDeliveryAddress.getText().toString().trim();

            // Validar que la dirección no esté vacía
            if (address.isEmpty()) {
                tilDeliveryAddress.setError("Por favor ingrese una dirección de entrega");
                return;
            }

            // Validar longitud mínima
            if (address.length() < 10) {
                tilDeliveryAddress.setError("La dirección debe tener al menos 10 caracteres");
                return;
            }

            // Limpiar error y confirmar
            tilDeliveryAddress.setError(null);
            
            if (listener != null) {
                listener.onAddressConfirmed(address);
            }
            
            dialog.dismiss();
        });

        // Limpiar error al escribir
        etDeliveryAddress.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilDeliveryAddress.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
            // Limpiar campos al mostrar
            etDeliveryAddress.setText("");
            tilDeliveryAddress.setError(null);
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}