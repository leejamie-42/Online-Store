# Postman Collections - Store Backend API

This directory contains Postman collections and environment files for testing the Store Backend APIs.

## 📦 Files Included

1. **`Authentication_API.postman_collection.json`** - Complete API collection with all authentication endpoints
2. **`Product_API.postman_collection.json`** - Complete API collection with all product endpoints
3. **`Store_Backend_Environment.postman_environment.json`** - Environment variables for local development
4. **`Order_API.postman_collection.json`** - Authenticated endpoints for creating and viewing orders

## 🚀 Quick Start

### Step 1: Import into Postman

1. Open **Postman**
2. Click **Import** button (top left)
3. Drag and drop all JSON files or click "Upload Files"
4. Import all files:
   - `Authentication_API.postman_collection.json`
   - `Product_API.postman_collection.json`
   - `Order_API.postman_collection.json`
   - `Store_Backend_Environment.postman_environment.json`

### Step 2: Select Environment

1. In Postman, look at the top-right corner
2. Click the environment dropdown
3. Select **"Store Backend - Local Development"**

### Step 3: Start the Backend Server

```bash
cd store-backend
../gradlew bootRun
```

The server should be running on `http://localhost:8080`

### Step 4: Test the APIs

#### Authentication API Requests

Run these requests in order:

1. **Register New User** - Creates a new user account
2. **Login** - Authenticates and gets JWT tokens
3. **Refresh Access Token** - Gets a new access token
4. **Logout** - Invalidates tokens and logs out user
5. **Test Protected Endpoint** - Tests authentication with JWT

#### Product API Requests

These are public endpoints (no authentication required):

1. **Get All Products** - Retrieves all products
2. **Get Product by ID** - Retrieves specific product
3. **Example Requests** - Pre-configured product examples (Wireless Mouse, Keyboard)
4. **Test 404** - Tests error handling for invalid product ID

#### Order API Requests

Run these after Authentication and Product collections:

1. **Create Order** (`POST /api/orders`)
   - Requires Bearer `{{accessToken}}`
   - Uses `{{userId}}` from Authentication and `{{productId}}` from Product requests
   - Auto-saves `orderId` to environment
2. **Get Order by ID** (`GET /api/orders/{{orderId}}`)
   - Requires Bearer `{{accessToken}}`
   - Uses `{{orderId}}` from Create Order
3. **Get My Orders** (`GET /api/orders`)
   - Requires Bearer `{{accessToken}}`
   - Returns list of the authenticated user's orders (newest first)

---

## 📋 Authentication API Collection Overview

### 1. Register New User

- **Endpoint:** `POST /api/auth/register`
- **Purpose:** Create a new user account
- **Auto-saves:** Access token, refresh token, user details to environment

**Request Body:**

```json
{
  "name": "Test User",
  "email": "test.user@example.com",
  "password": "SecurePassword123!"
}
```

**Success Response (201 Created):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "Test User",
    "email": "test.user@example.com",
    "role": "CUSTOMER"
  }
}
```

### 2. Login

- **Endpoint:** `POST /api/auth/login`
- **Purpose:** Authenticate existing user
- **Auto-saves:** Tokens to environment for subsequent requests

**Request Body:**

```json
{
  "username": "customer",
  "password": "COMP5348"
}
```

**Note:** `username` is the user's name (not email). For the default test account:

- Username: `customer`
- Password: `COMP5348`

### 3. Refresh Access Token

- **Endpoint:** `POST /api/auth/refresh`
- **Purpose:** Get new access token without re-authenticating
- **Uses:** Refresh token from login/register response

**Request Body:**

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

### 4. Logout

- **Endpoint:** `POST /api/auth/logout`
- **Purpose:** Invalidate user tokens and log out
- **Uses:** Access token from Authorization header + refresh token from request body

**Request Headers:**

- `Authorization: Bearer {{accessToken}}`

**Request Body:**

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

**Success Response (200 OK):**

```json
{
  "message": "Logged out successfully"
}
```

**Note:** After logout, both tokens are blacklisted and can no longer be used for authentication. The tokens are automatically cleared from the Postman environment.

### 5. Test Protected Endpoint

- **Endpoint:** `GET /api/products` (example)
- **Purpose:** Verify JWT authentication works
- **Uses:** Bearer token authentication with `{{accessToken}}`

---

## 📋 Product API Collection Overview

The Product API collection provides endpoints for browsing the product catalog. **No authentication is required** for these endpoints.

### 1. Get All Products

- **Endpoint:** `GET /api/products`
- **Purpose:** Retrieve all products from the catalog
- **Authentication:** None (public endpoint)
- **Auto-saves:** `productId` from first product in response

**Success Response (200 OK):**

```json
[
  {
    "id": 1,
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse with adjustable DPI",
    "price": 49.99,
    "imageUrl": "https://example.com/images/wireless-mouse.jpg",
    "stock": 150,
    "published": true
  },
  {
    "id": 2,
    "name": "Mechanical Keyboard",
    "description": "RGB mechanical keyboard with blue switches",
    "price": 129.99,
    "imageUrl": "https://example.com/images/mechanical-keyboard.jpg",
    "stock": 75,
    "published": true
  }
]
```

**Automated Tests:**

- ✅ Status code is 200
- ✅ Response is an array
- ✅ Each product has required fields (id, name, price, stock, published)
- ✅ Validates data types (id is number, price is number, published is boolean)
- ✅ Auto-saves first product's ID to `{{productId}}` environment variable

### 2. Get Product by ID

- **Endpoint:** `GET /api/products/:id`
- **Purpose:** Retrieve a specific product by its ID
- **Authentication:** None (public endpoint)
- **Uses:** `{{productId}}` from environment (populated by "Get All Products")

**Success Response (200 OK):**

```json
{
  "id": 1,
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with adjustable DPI",
  "price": 49.99,
  "imageUrl": "https://example.com/images/wireless-mouse.jpg",
  "stock": 150,
  "published": true
}
```

**Error Response (404 Not Found):**

```json
{
  "error": "Not Found",
  "message": "Product not found with id: 999"
}
```

**Automated Tests:**

- ✅ Status code is 200
- ✅ Response is an object (not array)
- ✅ Product ID matches requested ID
- ✅ All required fields are present
- ✅ Validates data types

### 3. Example Requests

Pre-configured requests for testing specific products from seed data:

#### Example 1 - Wireless Mouse

- **Product ID:** 1
- **Expected Name:** "Wireless Mouse"
- **Expected Price:** $49.99

#### Example 2 - Mechanical Keyboard

- **Product ID:** 2
- **Expected Name:** "Mechanical Keyboard"
- **Expected Price:** $129.99

### 4. Test 404 - Invalid Product ID

- **Product ID:** 999 (non-existent)
- **Expected Status:** 404 Not Found
- **Purpose:** Verify error handling works correctly

**Expected Response:**

```json
{
  "error": "Not Found",
  "message": "Product not found with id: 999"
}
```

### Seed Data Products

The backend includes 8 products in seed data:

| ID  | Product Name                | Price   | Stock |
| --- | --------------------------- | ------- | ----- |
| 1   | Wireless Mouse              | $49.99  | 150   |
| 2   | Mechanical Keyboard         | $129.99 | 75    |
| 3   | 27-inch Monitor             | $399.99 | 50    |
| 4   | USB-C Hub                   | $59.99  | 200   |
| 5   | Laptop Stand                | $39.99  | 100   |
| 6   | Noise-Cancelling Headphones | $249.99 | 60    |
| 7   | Webcam 1080p                | $79.99  | 120   |
| 8   | External SSD 1TB            | $149.99 | 80    |

---

## 🔧 Environment Variables

The environment includes the following variables:

| Variable          | Description                                 | Default Value           | Auto-populated               |
| ----------------- | ------------------------------------------- | ----------------------- | ---------------------------- |
| `baseUrl`         | API base URL                                | `http://localhost:8081` | No                           |
| `testEmail`       | Test user email                             | `test.user@example.com` | No                           |
| `testUserName`    | Test user name (used as username for login) | `testuser`              | No                           |
| `testPassword`    | Test user password                          | `SecurePassword123!`    | No                           |
| `defaultUsername` | Default test account username               | `customer`              | No                           |
| `defaultPassword` | Default test account password               | `COMP5348`              | No                           |
| `accessToken`     | JWT access token                            | (empty)                 | Yes (after login/register)   |
| `refreshToken`    | JWT refresh token                           | (empty)                 | Yes (after login/register)   |
| `userId`          | Logged-in user ID                           | (empty)                 | Yes (after login/register)   |
| `userEmail`       | Logged-in user email                        | (empty)                 | Yes (after login/register)   |
| `productId`       | Product ID for testing                      | (empty)                 | Yes (after Get All Products) |
| `orderId`         | Last created order ID                       | (empty)                 | Yes (after Create Order)     |

**Important:** The `username` field in login requests uses the user's **name** (not email). The `testUserName` variable should contain the username (e.g., "customer"), not an email address.

### Default Test Account

Use the pre-configured default account for quick testing:

- **Username:** `{{defaultUsername}}` (resolves to `customer`)
- **Password:** `{{defaultPassword}}` (resolves to `COMP5348`)

You can use these in the Login request by changing the body to:

```json
{
  "username": "{{defaultUsername}}",
  "password": "{{defaultPassword}}"
}
```

## ✅ Automated Tests

Each request includes automated tests that:

- ✅ Verify HTTP status codes
- ✅ Validate response structure
- ✅ Save tokens automatically to environment
- ✅ Check for required fields

**Test Results** are displayed in the Postman "Test Results" tab after each request.

## 🔄 Typical Workflow

### Product Browsing Flow (No Authentication Required)

```
1. Get All Products (GET /api/products)
   → Returns list of all products
   → Auto-saves first product ID to environment

2. Get Product by ID (GET /api/products/{id})
   → Uses {{productId}} from previous request
   → Returns detailed product information

3. Test with Examples
   → Use pre-configured requests (Wireless Mouse, Keyboard)
   → Verify specific product data

4. Test Error Handling
   → Run "Test 404 - Invalid Product ID"
   → Verify 404 response for non-existent products
```

### First-Time User Registration Flow

```
1. Register New User (POST /api/auth/register)
   → Saves accessToken and refreshToken

2. Test Protected Endpoint (GET /api/products)
   → Uses accessToken automatically

3. (After 15 minutes) Refresh Access Token (POST /api/auth/refresh)
   → Gets new accessToken using refreshToken
```

### Existing User Login Flow

```
1. Login (POST /api/auth/login)
   → Saves accessToken and refreshToken

2. Test Protected Endpoint
   → Uses accessToken automatically

3. (When token expires) Refresh Access Token
   → Gets new accessToken

4. Logout (POST /api/auth/logout)
   → Invalidates both tokens
   → Clears tokens from environment

### End-to-End Ordering Flow

```

1. Login (or Register) to obtain tokens
   → Populates {{accessToken}}, {{userId}}

2. Get All Products
   → Populates {{productId}}

3. Create Order
   → Uses {{accessToken}}, {{userId}}, {{productId}}
   → Populates {{orderId}}

4. Get Order by ID
   → Uses {{orderId}} to fetch details

5. Get My Orders
   → Verifies the new order appears in history

```

```

## 📖 Example Responses

### Success Responses

**Register/Login Success:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjk1MTI0ODAwLCJleHAiOjE2OTUxMjU3MDB9.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjk1MTI0ODAwLCJleHAiOjE2OTUyMTEyMDB9.signature",
  "user": {
    "id": 1,
    "name": "Test User",
    "email": "test@example.com",
    "role": "CUSTOMER"
  }
}
```

**Refresh Token Success:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.NEW_TOKEN.signature"
}
```

### Error Responses

**409 Conflict - Username Already Exists:**

```json
{
  "error": "Conflict",
  "message": "User already exists with username: testuser"
}
```

**409 Conflict - Email Already Exists:**

```json
{
  "error": "Conflict",
  "message": "User already exists with email: test@example.com"
}
```

**401 Unauthorized - Invalid Credentials:**

```json
{
  "error": "Unauthorized",
  "message": "Invalid credentials"
}
```

**Note:** Login requires the user's **username** (name field), not email address.

**400 Bad Request - Validation Error:**

```json
{
  "error": "Bad Request",
  "fieldErrors": {
    "name": "Name is required",
    "email": "Email must be valid",
    "password": "Password must be at least 8 characters"
  }
}
```

## 🛠️ Customization

### Change Base URL

1. Select environment in top-right dropdown
2. Click the eye icon (👁️) to view variables
3. Edit `baseUrl` to point to different server (e.g., production)

### Use Different Test Data

1. Edit environment variables:
   - `testEmail`
   - `testUserName`
   - `testPassword`
2. Save changes

### Add More Requests

1. Right-click on the collection
2. Select "Add Request"
3. Configure endpoint, headers, body
4. Use `{{accessToken}}` for authentication

## 🔐 Security Notes

- **Tokens are sensitive:** The `accessToken` and `refreshToken` are marked as "secret" in the environment
- **Don't commit tokens:** The tokens are auto-populated and should not be committed to version control
- **Token Expiration:**
  - Access Token: 15 minutes
  - Refresh Token: 24 hours

## 🐛 Troubleshooting

### "Connection Refused" Error

- ✅ Ensure backend server is running: `./gradlew bootRun`
- ✅ Verify server is on port 8080: Check console output
- ✅ Check `baseUrl` in environment matches server port

### Product API Returns Empty Array

- ✅ Verify seed data is loaded: Check backend logs for "Loaded X products into database"
- ✅ Ensure `app.data.seed: true` in `application-local.yml`
- ✅ Database might be empty on first run - restart server to trigger seed data load
- ✅ Check if database was manually cleared

### "404 Not Found" on Valid Product ID

- ✅ Run "Get All Products" first to verify products exist
- ✅ Check console logs for actual product IDs in database
- ✅ Verify you're using correct product ID (1-8 for seed data)
- ✅ Database might have different IDs if manually modified

### Product API Shows Incorrect Data

- ✅ Verify you're testing against correct environment (local vs production)
- ✅ Check if database was manually modified
- ✅ Clear database and restart server to reload seed data
- ✅ Verify `products.json` hasn't been modified

### "401 Unauthorized" on Protected Endpoints

- ✅ Run "Login" or "Register" first to get tokens
- ✅ Check that `accessToken` is populated in environment
- ✅ Verify token hasn't expired (15 minute lifetime)
- ✅ Use "Refresh Access Token" to get a new token

### Validation Errors

- ✅ Check request body matches expected format
- ✅ Password must be at least 8 characters
- ✅ Email must be valid format
- ✅ Name must be 2-100 characters

### "409 Conflict" on Register

- ✅ User with this username or email already exists
- ✅ Check if username (`name` field) is already taken
- ✅ Use "Login" instead, or change `testUserName` and `testEmail` variables

## 📚 Additional Resources

### Authentication API

- **API Documentation:** See `CLAUDE.md` in the backend directory
- **Backend Tests:** `store-backend/src/test/java/com/comp5348/store/controller/AuthenticationControllerTest.java`
- **Error Handling:** `store-backend/src/main/java/com/comp5348/store/exception/GlobalExceptionHandler.java`

### Product API

- **API Documentation:** `store-backend/PRODUCT_API.md`
- **Seed Data:** `store-backend/src/main/resources/data/products.json`
- **Backend Tests:**
  - Repository: `store-backend/src/test/java/com/comp5348/store/repository/ProductRepositoryTest.java`
  - Service: `store-backend/src/test/java/com/comp5348/store/service/ProductServiceTest.java`
  - Controller: `store-backend/src/test/java/com/comp5348/store/controller/ProductControllerTest.java`
  - Data Loader: `store-backend/src/test/java/com/comp5348/store/config/DataLoaderTest.java`
- **Implementation Plan:** `tasks/store-backend/phase-2-product-catalog.md`

## 🤝 Contributing

To add new endpoints to the collection:

1. Create the request in Postman
2. Add test scripts if needed
3. Export the collection (Collection → Export → Collection v2.1)
4. Replace the existing JSON file

---

**Happy Testing! 🚀**
