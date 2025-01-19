package com.example.shoppingCart.service;

import java.util.List;

import com.example.shoppingCart.model.Cart;
import com.example.shoppingCart.repository.CartRepository;

public interface CartService {

	public Cart saveCart(Integer productId, Integer userId);

	public List<Cart> getCartsByUser(Integer userId);
	
	public Integer getCountCart(Integer userId);

	public void updateQuantity(String sy, Integer cid);

	Cart getCartByUserAndProduct(Integer uid, Integer pid);

	public default void updateCart(Cart cart) {
		// Save the updated cart entry with the new quantity
		CartRepository.save();
	}

	void clearCart(Integer id);
}
