package com.example.pacial2ah09062.model;

import com.example.pacial2ah09062.database.entity.CartItem;
import com.example.pacial2ah09062.database.entity.Product;

public class CartItemWithProduct {

    private final CartItem cartItem;
    private final Product product;

    public CartItemWithProduct(CartItem cartItem, Product product) {
        this.cartItem = cartItem;
        this.product = product;
    }

    public CartItem getCartItem() {
        return cartItem;
    }

    public Product getProduct() {
        return product;
    }

    public String getProductName() {
        return product != null ? product.getName() : "";
    }

    public int getQuantity() {
        return cartItem.getQuantity();
    }

    public double getUnitPrice() {
        return product != null ? product.getPrice() : 0.0;
    }

    public double getSubtotal() {
        return getUnitPrice() * getQuantity();
    }
}
