package com.example.shoppingCart.model;

import com.example.shoppingCart.repository.CartRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private UserDtls user;

	@ManyToOne
	private Product product;

	private Integer quantity;
	
	@Transient
	private Double totalPrice;
	
	@Transient
	private Double totalOrderPrice;


	public void increaseQuantity(int quantityToAdd) {
		this.quantity += quantityToAdd;
	}

	public Cart getCartByUserAndProduct(Integer userId, Integer productId) {
		return CartRepository.findByUserIdAndProductId(userId, productId);
	}


}
