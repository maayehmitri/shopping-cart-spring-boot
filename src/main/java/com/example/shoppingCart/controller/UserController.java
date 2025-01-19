package com.example.shoppingCart.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.shoppingCart.model.Cart;
import com.example.shoppingCart.model.Category;
import com.example.shoppingCart.model.OrderRequest;
import com.example.shoppingCart.model.ProductOrder;
import com.example.shoppingCart.model.UserDtls;
import com.example.shoppingCart.service.CartService;
import com.example.shoppingCart.service.CategoryService;
import com.example.shoppingCart.service.OrderService;
import com.example.shoppingCart.service.UserService;
import com.example.shoppingCart.util.CommonUtil;
import com.example.shoppingCart.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;


	@GetMapping("/")
	public String home() {
		return "user/home";
	}

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {
		Cart existingCart = cartService.getCartByUserAndProduct(uid, pid);

		if (existingCart != null) {
			// If the product already exists in the cart, increase the quantity
			existingCart.increaseQuantity(1);
			cartService.updateCart(existingCart); // Update cart in the database
			session.setAttribute("succMsg", "Product quantity updated in cart");
		} else {
			// If the product doesn't exist in the cart, create a new cart entry
			Cart newCart = cartService.saveCart(pid, uid);
			if (ObjectUtils.isEmpty(newCart)) {
				session.setAttribute("errorMsg", "Product add to cart failed");
			} else {
				session.setAttribute("succMsg", "Product added to cart");
			}
		}

		return "redirect:/product/" + pid;
	}


	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {

		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}

	private UserDtls getLoggedInUserDetails(Principal p) {
		if (p == null || p.getName() == null) {
			throw new IllegalStateException("Principal or user email is null.");
		}
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		if (userDtls == null) {
			throw new IllegalStateException("User details not found for email: " + email);
		}
		return userDtls;
	}


	@GetMapping("/orders")
	public String orderPage(Principal principal, Model model) {
		// Fetch the logged-in user
		UserDtls user = getLoggedInUserDetails(principal);
		model.addAttribute("user", user); // Add the user object to the model

		// Fetch user's cart
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		model.addAttribute("carts", carts);

		if (!carts.isEmpty()) {
			// Calculate order price (sum of all product prices in the cart)
			Double orderPrice = carts.stream()
					.mapToDouble(Cart::getTotalOrderPrice)
					.sum();

			// Tax percentage (e.g., 10%)
			double taxRate = 10.0;
			double taxAmount = (orderPrice * taxRate) / 100;

			// Fixed delivery fee
			double deliveryFee = 0.0;

			// Calculate total order price
			Double totalOrderPrice = orderPrice + taxAmount + deliveryFee;

			// Add to model
			model.addAttribute("orderPrice", orderPrice);
			model.addAttribute("taxAmount", taxAmount);
			model.addAttribute("deliveryFee", deliveryFee);
			model.addAttribute("totalOrderPrice", totalOrderPrice);
		}

		return "/user/order"; // Match your Thymeleaf template name
	}




	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p, HttpSession session) {
		if (p == null) {
			session.setAttribute("errorMsg", "You must be logged in to place an order.");
			return "redirect:/user/orders";
		}

		if (request == null || request.getOrderAddress() == null || request.getOrderAddress().isEmpty()) {
			session.setAttribute("errorMsg", "Order address is missing.");
			return "redirect:/user/orders";
		}

		try {
			UserDtls user = getLoggedInUserDetails(p);
			List<Cart> carts = cartService.getCartsByUser(user.getId());

			if (carts.isEmpty()) {
				session.setAttribute("errorMsg", "Your cart is empty.");
				return "redirect:/user/cart";
			}

			List<ProductOrder> productOrders = new ArrayList<>();
			for (Cart cart : carts) {
				ProductOrder productOrder = new ProductOrder();
				productOrder.setProduct(cart.getProduct());
				productOrder.setQuantity(cart.getQuantity());
				productOrder.setPrice(cart.getProduct().getPrice());
				productOrder.setTotalPrice(cart.getTotalOrderPrice());
				productOrders.add(productOrder);
			}

			orderService.saveOrder(user.getId(), request, productOrders);

			cartService.clearCart(user.getId());

			session.setAttribute("succMsg", "Order placed successfully!");
			return "redirect:/user/success";
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("errorMsg", "An error occurred while placing the order.");
			return "redirect:/user/success";
		}
	}




	@GetMapping("/success")
	public String loadSuccess() {
		return "/user/success";
	}

	@GetMapping("/user-orders")
	public String myOrder(Model m, Principal p) {
		UserDtls loginUser = getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
		m.addAttribute("orders", orders);
		return "/user/my_orders";
	}

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/user/user-orders";
	}

	@GetMapping("/profile")
	public String profile() {
		return "/user/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile Updated");
		}
		return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not updated !! Error in server");
			} else {
				session.setAttribute("succMsg", "Password Updated sucessfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password incorrect");
		}

		return "redirect:/user/profile";
	}

}
