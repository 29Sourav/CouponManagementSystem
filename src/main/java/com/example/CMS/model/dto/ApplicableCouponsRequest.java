package com.example.CMS.model.dto;

import com.example.CMS.model.cart.Cart;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicableCouponsRequest {

    private Cart cart;
}
