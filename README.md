# CouponManagementSystem
RESTful API to manage and apply different types of discount coupons (cart-wise, product-wise, and BxGy) for an e-commerce platform, with the ability to easily add new types of coupons in the future.

# Project Overview
The Coupon Management System solves the business problem of automatically validating and applying discounts on a user’s cart based on predefined rules. It supports multiple coupon types, including cart-level percentage discounts, product-level fixed discounts, and Buy X Get Y (BxGy) offers. The system exposes APIs to create coupons, validate coupons, apply coupons on cart items, and manage coupon templates and rules. Each coupon’s logic is dynamically executed using rule templates and metadata stored in the database.
The goal of the project is to build a scalable, rule-driven, and easily extendable coupon engine that can be integrated into any e-commerce platform.


-----------------------------------------------------------------------------------------------------------
# Tech Stack

# Backend
Java 17 – Core application language

Spring Boot 3 – For building REST APIs

Spring Data JPA – ORM and database access

Hibernate – Entity management and persistence

Drools Rule Engine – Dynamic coupon rule evaluation using DRL templates

# Database
MySQL 8 – Stores coupons, rule templates, and metadata

JSON Columns – Store flexible coupon configurations without schema changes


-----------------------------------------------------------------------------------------------------------


# Coupon Types Implemented

# 1. CART Coupon (Cart-Level Percentage Discount)

A CART coupon applies a percentage discount on the entire cart, but only if the cart value crosses a minimum amount.
Example:

Coupon: CART10

Metadata: minCartValue = 275, discountPercent = 10

If your cart total is ₹300 → You get 10% off (₹30 discount).
If your cart total is below 275 → Coupon is not applied.
➡️ Used for: Whole-cart percentage discounts.

# 2. PRODUCT Coupon (Fixed Discount on a Product)
3. 
A PRODUCT coupon gives a fixed amount off on a particular product or product category.
Example:

Coupon: PROD200

Metadata: productId = 101, discountAmount = 200

If the product price is ₹1200 → Final price becomes ₹1000.
Coupon applies only on that specific product or mapped category.
➡️ Used for: Item-specific fixed discounts.

# 3. BXGY Coupon (Buy X Get Y Free)

A BXGY coupon allows a customer to get Y quantity free when they buy X quantity of a product.
The system also supports repetitionLimit—how many times the offer can repeat.
Example:

Coupon: BUY2GET1

Metadata: buyQty = 2, getQty = 1, repetitionLimit = 3

If the user buys 6 items:
Eligible: (6 / 2) = 3 cycles → up to repetitionLimit (3)
Free items = 3 × 1 = 3 free items

If user buys 10 items:
Eligible cycles = 10 / 2 = 5
But repetitionLimit = 3 → Max 3 cycles

Free items = 3 × 1 = 3 free items only

➡️ Used for: Quantity-based offers like Buy 1 Get 1, Buy 2 Get 1...

# 4. BXGY (Category-Based Buy X Get Y Free) (New Coupon Type Implemented)

This variant of the BXGY coupon applies the Buy X Get Y offer not on a single product, but on any products that belong to a target category.
Instead of requiring a specific productId, the coupon checks the category of each item in the cart and then calculates how many BXGY cycles are applicable.

How it Works:

The user must buy X quantity of any product in the category.
They get Y quantity free, also from products of the same category.
The free items are chosen from the cheapest eligible items to maintain fairness.
repetitionLimit is still supported to control how many cycles can be applied.

-----------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------

# Extensible Architecture — How the System Supports Future Coupon Types

The Coupon Management System is intentionally designed to be modular, rule-driven, and data-configurable, so new discount types can be introduced with minimal code changes.

# 1. Rule Templates (DRL) Stored in the Database

Instead of hardcoding coupon logic inside Java classes, the system stores each coupon’s logic as Drools rule templates in the coupon_rule_template table.

This means:
Each new coupon gets its own DRL template
No changes are required in the Java business logic
No code redeploy needed if only DRL logic changes

Why this is powerful:
Adding a new coupon is as simple as adding a new row in the database.

# 2. Flexible Metadata Using JSON (No Schema Changes Needed)

Each coupon stores its configuration inside the metadata JSON field.
Every coupon type reads only the fields it needs.

Example:

CART coupons use { "minCartValue": 275, "discountPercent": 10 }

PRODUCT coupons use { "productId": 10, "discountAmount": 200 }

BXGY coupons use { "buyQty": 2, "getQty": 1, "repetitionLimit": 3 }

If a new coupon type needs new values, we simply add them inside metadata:

{
  "minSpend": 1000,
  "cashback": 50,
  "maxCap": 200
}


# 3. Dynamic Rule Execution Pipeline (Engine-Agnostic Design)

Every coupon—regardless of type—goes through the same engine flow:
Fetch coupon from DB
Read its metadata
Fetch its linked rule template
Generate a Drools rule dynamically
Execute rule on the cart context
Return discount decision

**New coupon = New rule template + New metadata
 The system automatically supports it**

# 4. Coupon Types Are Enum-Based — Add → Map → Done

To introduce a new coupon type:

Add a new enum value (e.g., TIERED_DISCOUNT)
Create a new DRL template for it
Insert template into DB
Create coupon entries referencing that template
No other system components require modification.

# 5. Rule Engine Decouples Business Logic from the Core Code

All discount logic is handled inside Drools, not in Java.

This means:

Java only orchestrates (does not contain logic)
Rule changes do not require Java code redeploy

In the future, we can add more complex logic without touching core modules

# 6. Supports Multiple Layers of Future Growth

Because the system is rule-driven and metadata-driven, it can easily support:

🟢 New Coupon Types

Cashbacks
Tiered discounts (spend more, get more)
Category-wide percentage discounts
Multi-product combo coupons
Seasonal coupons

🟢 Business-Specific Custom Rules

Customer-level coupons (first-time user, loyalty tier)
Payment-method based coupons
Store/branch-level discounts

# All of this is possible because rules are not tied to code.

-----------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------


