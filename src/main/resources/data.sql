-- PRODUCTS
INSERT IGNORE INTO product (product_id, category, name, price) VALUES
('P1', 'general', 'Product 1', 100),
('P2', 'general', 'Product 2', 200),
('P3', 'general', 'Product 3', 150),
('P4', 'clothing', 'T-Shirt', 500),
('P5', 'general', 'Product 5', 300),
('P6', 'general', 'Product 6', 250);

-- RULE TEMPLATES
INSERT IGNORE INTO coupon_rule_template (id, name, type, drl_template) VALUES
(1, 'Cart Percent Template', 'CART',
'package com.example.CMS.rules;

import com.example.CMS.model.cart.Cart;
import com.example.CMS.model.rule.DiscountDecision;

rule "CART"
    salience 10
when
    $cart : Cart( getTotalAmount() >= {{minCartValue}} )
then
    double percent = {{discountPercent}};
    double total = $cart.getTotalAmount();

    DiscountDecision d = new DiscountDecision();
    d.setCouponId(java.lang.Long.valueOf({{couponId}}));
    d.setApplicable(true);
    d.setDiscountAmount(total * (percent / 100.0));
    d.setMessage(percent + "% off on cart");
    insert(d);
end'),

(2, 'Product Fixed Discount', 'PRODUCT',
'package com.example.CMS.rules;

import com.example.CMS.model.cart.Cart;
import com.example.CMS.model.cart.CartItem;
import com.example.CMS.model.rule.DiscountDecision;
import com.example.CMS.entity.Coupon;

rule "PRODUCT"
    salience 50
when
    $cart : Cart()
    $coupon : Coupon( type == com.example.CMS.entity.enums.CouponType.PRODUCT )
then
    double discountAmount = {{discountAmount}};

    java.util.List<String> targetProducts = java.util.Arrays.asList({{productIds}});

    double totalDiscount = 0.0;

    for (CartItem item : $cart.getItems()) {
        if (targetProducts.contains(item.getProductId())) {
            totalDiscount += item.getQuantity() * discountAmount;
        }
    }

    if (totalDiscount > 0) {
        DiscountDecision d = new DiscountDecision();
        d.setCouponId(java.lang.Long.valueOf({{couponId}}));
        d.setApplicable(true);
        d.setDiscountAmount(totalDiscount);
        d.setMessage("Flat discount on selected products");
        insert(d);
    }
end'),

(3, 'BUY_X_GET_Y_PRODUCT', 'BXGY',
'package com.example.CMS.rules;

import com.example.CMS.model.cart.Cart;
import com.example.CMS.model.cart.CartItem;
import com.example.CMS.model.rule.DiscountDecision;

rule "Buy X Get Y Free (Product-based)"
salience 20
when
    $cart : Cart()
then
    int buyQty = {{buyQty}};
    int getQty = {{getQty}};
    int repetitionLimit = {{repetitionLimit}};

    java.util.List<String> buyProducts = java.util.Arrays.asList({{buyProducts}});
    java.util.List<String> getProducts = java.util.Arrays.asList({{getProducts}});

    int totalBuyCount = 0;

    for (CartItem item : $cart.getItems()) {
        if (buyProducts.contains(item.getProductId())) {
            totalBuyCount += item.getQuantity();
        }
    }

    // Not enough buy items → not applicable
    if (totalBuyCount < buyQty) {
        return;
    }

    // Calculate free units
    int eligibleFreeUnits = (totalBuyCount / buyQty) * getQty;

    // Apply repetition limit
    if (repetitionLimit > 0 && eligibleFreeUnits >= repetitionLimit) {
        eligibleFreeUnits = repetitionLimit;
    }

    double discount = 0.0;
    int appliedFreeUnits = 0;

    for (CartItem item : $cart.getItems()) {
        if (getProducts.contains(item.getProductId())) {
            int freeUnitsForItem = Math.min(eligibleFreeUnits, item.getQuantity());
            discount += freeUnitsForItem * item.getPrice();
            appliedFreeUnits += freeUnitsForItem;
            eligibleFreeUnits -= freeUnitsForItem;

            if (eligibleFreeUnits <= 0) break;
        }
    }


    if (appliedFreeUnits == 0) {
        return;
    }

    DiscountDecision d = new DiscountDecision();
    d.setCouponId(java.lang.Long.valueOf({{couponId}}));
    d.setApplicable(true);
    d.setDiscountAmount(discount);
    d.setMessage("Buy X Get Y Free (Product-based)");
    insert(d);
end
'),

(4, 'BUY_X_GET_Y_CATEGORY', 'BXGY',
'package com.example.CMS.rules;

import com.example.CMS.model.cart.Cart;
import com.example.CMS.model.cart.CartItem;
import com.example.CMS.model.rule.DiscountDecision;

rule "Buy X Get Y Free (Category-based)"
salience 20
when
    $cart : Cart()
then
    int buyQty = {{buyQty}};
    int getQty = {{getQty}};
    int repetitionLimit = {{repetitionLimit}};
    String category = "{{category}}";

    java.util.List<CartItem> categoryItems = new java.util.ArrayList<>();

    for (CartItem item : $cart.getItems()) {
        if (category.equalsIgnoreCase(item.getCategory())) {
            categoryItems.add(item);
        }
    }

    if (categoryItems.isEmpty()) {
        return;
    }

    int totalBuyCount = 0;
    for (CartItem item : categoryItems) {
        totalBuyCount += item.getQuantity();
    }

    if (totalBuyCount < buyQty) {
        return;
    }

    java.util.Collections.sort(categoryItems, (a, b) -> Double.compare(a.getPrice(), b.getPrice()));

    int eligibleFreeUnits = (totalBuyCount / buyQty) * getQty;

    if (repetitionLimit > 0 && eligibleFreeUnits > repetitionLimit) {
        eligibleFreeUnits = repetitionLimit;
    }

    double discount = 0.0;
    int appliedFreeUnits = 0;

    for (CartItem item : categoryItems) {
        int freeUnits = Math.min(eligibleFreeUnits, item.getQuantity());
        discount += freeUnits * item.getPrice();
        appliedFreeUnits += freeUnits;
        eligibleFreeUnits -= freeUnits;

        if (eligibleFreeUnits <= 0) break;
    }

    // Buy present but no free units applied → NOT applicable
    if (appliedFreeUnits == 0) {
        return;
    }

    DiscountDecision d = new DiscountDecision();
    d.setCouponId(java.lang.Long.valueOf({{couponId}}));
    d.setApplicable(true);
    d.setDiscountAmount(discount);
    d.setMessage("Buy X Get Y Free (Category-based)");
    insert(d);
end
');

-- COUPONS
INSERT IGNORE INTO coupon_master
(id, code, type, active, start_date, end_date, metadata, rule_template_id)
VALUES
(1, 'CART10', 'CART', true, '2024-01-01', '2030-01-01',
 '{
    "minCartValue": 275,
    "discountPercent": 10
 }',
 1),

(2, 'PROD200', 'PRODUCT', true, '2024-01-01', '2030-01-01',
 '{
    "discountAmount": 200,
    "productIds": ["P2", "P3"]
 }',
 2),

(3, 'B2G1_MIXED', 'BXGY', true, '2024-01-01', '2030-01-01',
 '{
    "buyQty": 2,
    "getQty": 1,
    "repetitionLimit": 2,
    "buyProducts": ["P1", "P2", "P3"],
    "getProducts": ["P5", "P6"]
 }',
 3),

(4, 'B2G1_CLOTH', 'BXGY', true, '2024-01-01', '2030-01-01',
 '{
    "buyQty": 2,
    "getQty": 1,
    "repetitionLimit": 2,
    "category": "clothing"
 }',
 4);

