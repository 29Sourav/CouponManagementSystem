package com.example.CMS.model.cart;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {


    private List<CartItem> items= new ArrayList<>();

    private double totalAmount;
    private double finalAmount;
    private double totalDiscount = 0.0;

    public double getTotalAmount() {
        return items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
    }
    public double getFinalAmount() {
        return (getTotalAmount() - totalDiscount);
    }


}
