# ShopEase Backend API Documentation

This document describes the backend currently implemented in the ShopEase marketplace application.

## 1. Overview

ShopEase is a marketplace with three user roles:

- `CUSTOMER`: browses products, manages a cart, and places orders.
- `SELLER`: creates and manages owned products after admin approval.
- `ADMIN`: reviews, approves, and suspends sellers.

The local base URL is:

```text
http://localhost:8080
```

All API routes begin with `/api`.

## 2. Backend Request Flow

```text
Frontend or API client
        |
        v
Controller: receives HTTP requests
        |
        v
Service: applies marketplace rules
        |
        v
Repository: executes SQL
        |
        v
PostgreSQL database
```

## 3. Authentication Status

Registration and database-backed user loading are implemented. Passwords are hashed with BCrypt.

Protected routes use Spring Security and obtain the current user's email from `Principal`. However, the current backend does not yet expose a complete login endpoint or configure JWT, HTTP Basic, or form login explicitly. Therefore, teammates should not expect `POST /api/auth/login` to work yet.

This is the most important remaining integration task before a separate frontend can use protected routes.

## 4. Roles and Seller Statuses

User roles:

```text
CUSTOMER
SELLER
ADMIN
```

Seller statuses:

```text
NOT_SELLER
PENDING
APPROVED
SUSPENDED
```

Only an `APPROVED` seller can create, update, or delete products.

## 5. Public Endpoints

### Register a user

```http
POST /api/auth/register
Content-Type: application/json
```

Request:

```json
{
  "name": "Ada Buyer",
  "email": "ada@example.com",
  "password": "securepass123"
}
```

Successful response: `201 Created`

```json
{
  "id": 4,
  "name": "Ada Buyer",
  "email": "ada@example.com",
  "password": null,
  "role": "CUSTOMER",
  "sellerStatus": "NOT_SELLER"
}
```

The client cannot choose its role during registration. New accounts are always customers.

### List or search products

```http
GET /api/products
```

Optional query parameters:

| Parameter | Meaning | Default |
| --- | --- | --- |
| `q` | Search product name or description | empty |
| `categoryId` | Filter by category | all categories |
| `page` | Zero-based page number | `0` |
| `size` | Results per page, maximum 100 | `20` |

Examples:

```text
GET /api/products?q=mouse
GET /api/products?categoryId=1
GET /api/products?q=phone&categoryId=1&page=0&size=10
```

Response:

```json
[
  {
    "id": 1,
    "name": "Wireless Earbuds",
    "description": "Bluetooth earbuds with charging case",
    "price": 15000.00,
    "stockQuantity": 50,
    "categoryId": 1,
    "sellerId": 8,
    "imageUrl": "/images/wireless-earbuds.jpg"
  }
]
```

### Get one product

```http
GET /api/products/{id}
```

Returns `404 Not Found` if the product does not exist.

### List categories

```http
GET /api/categories
```

Response:

```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Phones, laptops, and accessories"
  }
]
```

## 6. Authenticated User Endpoints

These endpoints require a logged-in user after authentication is fully configured.

### Get current user identity

```http
GET /api/auth/me
```

Current response is the authenticated user's email as plain text.

### Request seller access

```http
POST /api/auth/seller-request
```

The account changes from:

```text
role=CUSTOMER, sellerStatus=NOT_SELLER
```

to:

```text
role=SELLER, sellerStatus=PENDING
```

An admin must approve the request before product management is allowed.

## 7. Seller Product Endpoints

These routes require the `SELLER` role. The service also verifies `sellerStatus=APPROVED`.

### Create a product

```http
POST /api/products
Content-Type: application/json
```

Request:

```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic rechargeable mouse",
  "price": 12000.00,
  "stockQuantity": 25,
  "categoryId": 1,
  "imageUrl": "/images/mouse.jpg"
}
```

Successful response: `201 Created`

The backend ignores a client-supplied product ID and assigns the authenticated seller automatically.

### Update an owned product

```http
PUT /api/products/{id}
Content-Type: application/json
```

The request body has the same fields as product creation. A seller cannot update another seller's product.

### Delete an owned product

```http
DELETE /api/products/{id}
```

Successful response: `204 No Content`.

## 8. Buyer Cart Endpoints

All cart operations use the authenticated user's account. One user cannot access another user's cart.

### View cart

```http
GET /api/cart
```

Response:

```json
[
  {
    "id": 10,
    "productId": 1,
    "productName": "Wireless Earbuds",
    "unitPrice": 15000.00,
    "quantity": 2,
    "availableStock": 50,
    "lineTotal": 30000.00
  }
]
```

### Get cart total

```http
GET /api/cart/total
```

Example response:

```json
30000.00
```

### Add product to cart

```http
POST /api/cart/{productId}?quantity=2
```

Adding the same product again increases its existing quantity.

### Replace cart item quantity

```http
PUT /api/cart/{productId}?quantity=3
```

### Remove product from cart

```http
DELETE /api/cart/{productId}
```

Quantities must be greater than zero and cannot exceed available stock.

## 9. Checkout and Buyer Orders

### Checkout

```http
POST /api/orders/checkout?shippingAddress=12 Example Street Lagos
```

Checkout performs one database transaction:

1. Loads the authenticated buyer's cart.
2. Rejects an empty cart.
3. Calculates the total.
4. Creates the order.
5. Saves order items with their purchase-time prices.
6. Reduces product stock.
7. Clears the cart.

If stock reduction fails, the entire transaction is rolled back.

New orders begin with:

```text
status=PENDING
paymentStatus=PENDING
```

### View buyer order history

```http
GET /api/orders
```

Response:

```json
[
  {
    "id": 14,
    "buyerId": 4,
    "totalAmount": 30000.00,
    "status": "PENDING",
    "paymentStatus": "PENDING",
    "shippingAddress": "12 Example Street Lagos",
    "createdAt": "2026-08-02T07:00:00"
  }
]
```

## 10. Seller Order Endpoints

### View orders containing seller products

```http
GET /api/orders/seller
```

### Update order status

```http
PUT /api/orders/{id}/status?value=SHIPPED
```

Accepted values currently are:

```text
SHIPPED
DELIVERED
CANCELLED
```

The order must contain a product owned by the authenticated seller.

Important limitation: an order may contain products from several sellers, but the database currently stores one status for the entire order. A later version should use seller-specific fulfillment records.

## 11. Admin Seller Endpoints

These routes require the `ADMIN` role.

### List pending sellers

```http
GET /api/admin/sellers/pending
```

### Approve seller

```http
POST /api/admin/sellers/{id}/approve
```

The seller status becomes `APPROVED`.

### Suspend seller

```http
POST /api/admin/sellers/{id}/suspend
```

The seller status becomes `SUSPENDED`, preventing product management.

## 12. Validation Rules

Products:

- Name is required and limited to 150 characters.
- Price is required and cannot be negative.
- Stock cannot be negative.
- Description is limited to 5000 characters.
- Image URL is limited to 500 characters.

Users:

- Name is required and limited to 100 characters.
- Email must be valid and unique.
- Password must contain 8 to 100 characters.

## 13. Error Response Format

Structured API errors use this format:

```json
{
  "timestamp": "2026-08-02T07:00:00Z",
  "status": 400,
  "message": "Product validation failed",
  "fieldErrors": {
    "name": "Product name is required"
  }
}
```

Common status codes:

| Status | Meaning |
| --- | --- |
| `200` | Request succeeded |
| `201` | Resource created |
| `204` | Resource deleted successfully |
| `400` | Request validation failed |
| `401` | Authentication is required |
| `403` | Authenticated user lacks permission |
| `404` | Product was not found |
| `409` | Business rule conflict, such as duplicate email or invalid state |

## 14. Frontend Integration Workflow

Buyer flow:

```text
Register/login
  -> GET /api/products
  -> POST /api/cart/{productId}
  -> GET /api/cart
  -> POST /api/orders/checkout
  -> GET /api/orders
```

Seller flow:

```text
Register/login
  -> POST /api/auth/seller-request
  -> Wait for admin approval
  -> POST /api/products
  -> GET /api/orders/seller
  -> PUT /api/orders/{id}/status
```

Admin flow:

```text
Login as admin
  -> GET /api/admin/sellers/pending
  -> POST /api/admin/sellers/{id}/approve
```

## 15. Remaining Backend Work

- Implement an explicit authentication API, preferably JWT for a separate frontend.
- Add logout/token refresh behavior if JWT is selected.
- Add a safe first-admin bootstrap mechanism.
- Integrate Paystack, Flutterwave, Stripe, or another payment provider.
- Return order item details instead of only order summaries.
- Split multi-seller orders into seller-specific fulfillment records.
- Add API/controller, security, repository, cart, and checkout tests.
- Add OpenAPI/Swagger-generated interactive documentation.

## 16. Running the Project

```powershell
.\mvnw.cmd spring-boot:run
```

Run tests:

```powershell
.\mvnw.cmd test
```

The PostgreSQL connection values come from the project-root `.env` file through `application.properties`.
