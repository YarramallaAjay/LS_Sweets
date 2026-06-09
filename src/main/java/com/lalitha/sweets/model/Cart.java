package com.lalitha.sweets.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart {

	private List<CartItem> items = new ArrayList<>();
	
	public void addItem(CartItem item) {
		for(CartItem existing : items) {
			if(existing.getProductId().equals(item.getProductId()) &&
			   existing.getPriceLabel().equals(item.getPriceLabel())) {
				
				existing.setQuantity(existing.getQuantity() + item.getQuantity());
				return;
			}
		}
		items.add(item);
	}
	
	public BigDecimal getTotal() {
		return items.stream().map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
							 .reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public List<CartItem> getItems() {
		return items;
	}

	
}
