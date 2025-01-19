package com.example.shoppingCart.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.shoppingCart.model.OrderRequest;
import com.example.shoppingCart.model.ProductOrder;

public interface OrderService {

	public void saveOrder(Integer userid, OrderRequest orderRequest, List<ProductOrder> productOrders) throws Exception;

	public List<ProductOrder> getOrdersByUser(Integer userId);

	public ProductOrder updateOrderStatus(Integer id, String status);

	public List<ProductOrder> getAllOrders();

	public ProductOrder getOrdersByOrderId(String orderId);
	
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo,Integer pageSize);
}
