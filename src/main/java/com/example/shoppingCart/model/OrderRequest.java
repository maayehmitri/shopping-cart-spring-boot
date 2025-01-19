package com.example.shoppingCart.model;

import lombok.Data;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;

@ToString
@Data
public class OrderRequest {

	private String firstName;

	private String lastName;

	private String email;

	private String mobileNo;

	private String address;

	private String city;

	private String state;

	private String pincode;
	
	private String paymentType;

    public Collection<Object> getOrderAddress() {
		return Collections.singleton(address);
    }
}
