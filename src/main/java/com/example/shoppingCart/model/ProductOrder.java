package com.example.shoppingCart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ProductOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String orderId;

	private LocalDate orderDate;

	@ManyToOne
	private Product product; // Product associated with this order

	private Double price; // Total price for the product (quantity * unit price)

	private Integer quantity; // Quantity ordered

	@ManyToOne
	private UserDtls user; // User who placed the order

	private String status; // Status of the order (e.g., Delivered, In Progress)

	private String paymentType; // Payment type (e.g., COD, Online Payment)

	@OneToOne(cascade = CascadeType.ALL)
	private OrderAddress orderAddress; // Delivery address for the order

	// Helper method to get the user's full name
	public String getUserName() {
		return user != null ? user.getName() : null;
	}

	// Helper method to get the delivery address details
	public String getDeliveryDetails() {
		if (orderAddress != null) {
			return String.format("%s %s, %s, %s, %s, %s",
					orderAddress.getFirstName(),
					orderAddress.getLastName(),
					orderAddress.getAddress(),
					orderAddress.getCity(),
					orderAddress.getState(),
					orderAddress.getPincode()
			);
		}
		return "Address not provided";
	}

	public void setTotalPrice(Double totalOrderPrice) {
	}
}
