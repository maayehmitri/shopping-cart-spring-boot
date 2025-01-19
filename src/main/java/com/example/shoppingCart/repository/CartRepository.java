package com.example.shoppingCart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shoppingCart.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    public static Cart findByUserIdAndProductId(Integer userId, Integer productId) {
        return null;
    }

	static void save() {
	}

	public Integer countByUserId(Integer userId);

	public List<Cart> findByUserId(Integer userId);

}
