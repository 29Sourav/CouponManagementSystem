package com.example.CMS.model.cart;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    private String productId;
    private String productName;
    private String category;
    private double price;
    private int quantity;
}
