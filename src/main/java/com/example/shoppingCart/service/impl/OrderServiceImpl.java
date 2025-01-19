package com.example.shoppingCart.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.shoppingCart.model.Cart;
import com.example.shoppingCart.model.OrderAddress;
import com.example.shoppingCart.model.OrderRequest;
import com.example.shoppingCart.model.ProductOrder;
import com.example.shoppingCart.repository.CartRepository;
import com.example.shoppingCart.repository.ProductOrderRepository;
import com.example.shoppingCart.service.OrderService;
import com.example.shoppingCart.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private ProductOrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;




	@Override
	public void saveOrder(Integer userid, OrderRequest orderRequest, List<ProductOrder> productOrders) throws Exception {
		if (orderRequest == null || orderRequest.getAddress() == null || orderRequest.getAddress().isEmpty()) {
			throw new Exception("Invalid order request: Address is missing.");
		}

		List<Cart> carts = cartRepository.findByUserId(userid);

		if (carts.isEmpty()) {
			throw new Exception("Cart is empty, cannot place an order.");
		}

		try {
			for (Cart cart : carts) {
				if (cart.getProduct().getStock() < cart.getQuantity()) {
					throw new Exception("Insufficient stock for product: " + cart.getProduct().getName());
				}

				ProductOrder order = new ProductOrder();
				order.setOrderId(UUID.randomUUID().toString());
				order.setOrderDate(LocalDate.now());
				order.setProduct(cart.getProduct());
				order.setPrice(cart.getProduct().getDiscountPrice());
				order.setQuantity(cart.getQuantity());
				order.setUser(cart.getUser());
				order.setStatus(OrderStatus.IN_PROGRESS.getName());
				order.setPaymentType(orderRequest.getPaymentType());

				OrderAddress address = new OrderAddress();
				address.setFirstName(orderRequest.getFirstName());
				address.setLastName(orderRequest.getLastName());
				address.setEmail(orderRequest.getEmail());
				address.setMobileNo(orderRequest.getMobileNo());
				address.setAddress(orderRequest.getAddress());
				address.setCity(orderRequest.getCity());
				address.setState(orderRequest.getState());
				address.setPincode(orderRequest.getPincode());

				order.setOrderAddress(address);

				orderRepository.save(order);
				cart.getProduct().setStock(cart.getProduct().getStock() - cart.getQuantity());
				cartRepository.delete(cart);
			}

			//commonUtil.sendMailForProductOrder(null, "success");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("An error occurred while placing the order: " + e.getMessage());
		}
	}





	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
        return orderRepository.findByUserId(userId);
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
		Optional<ProductOrder> findById = orderRepository.findById(id);
		if (findById.isPresent()) {
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder = orderRepository.save(productOrder);
			return updateOrder;
		}
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);

		Page<ProductOrder> orders = orderRepository.findAll(pageable);

		// Ensure user and address details are loaded
		orders.forEach(order -> {
			if (order.getUser() != null) {
				order.getUser().getName(); // Trigger lazy loading
			}
			if (order.getOrderAddress() != null) {
				order.getOrderAddress().getFirstName(); // Trigger lazy loading
			}
		});

		return orders;
	}


	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		return orderRepository.findByOrderId(orderId);
	}

}
