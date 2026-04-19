# API Documentation

This document provides an overview of the available RESTful APIs for the Real Estate Platform.

---

## 🔐 Authentication APIs

### Register
POST /api/v1/register

**Body:**
{
  "username": "string",
  "phone": "string",
  "email": "string",
  "password": "string",
  "confirmPassword": "string"
}

---

### Login
POST /api/v1/login

**Body:**
{
  "email": "string",
  "password": "string"
}

**Response:**

"Authorization": "JWT_TOKEN" , in response header


---

## 🏠 Property APIs

### Get All Properties
GET /api/v1/properties

**Query Parameters:**
- page (have default value = 0)
- size (have default value = 20)

---

### Get Property by ID
GET /api/v1/properties/{id}

---

### Create Property
POST /api/v1/properties

**Body:**
{
    "title": "string",
	"price": "number",
	"type": "string",
	"lng": "number",
	"lat": "number",
	"description": "string",
	"rooms": "number",
	"baths": "number"
}

---

### Update Property
PATCH /api/v1/properties/{id}

**body**
{
	"title": "string",
	"description": "string",
	"price": "number",
	"images": "array"
}

---

### Delete Property
DELETE /api/v1/properties/{id}

---

## 🔍 Search APIs

### Filter Search
GET /api/v1/properties/search/filter

**Query Parameters:**
- minPrice (not required)
- maxPrice (not required)
- minSize (not required)
- maxSize (not required)
- minRooms (not required)
- maxRooms (not required)
- minBaths (not required)
- maxBaths (not required)
- type (not required)
- page (have default value = 0)
- size (have default value = 20)

---

### Keyword Search
GET /api/v1/properties/search

**Query Parameters**
- text
- page (have default value = 0)
- size (have default value = 20)

---

### Map Search
GET /api/v1/properties/search/coord

**Query Parameters**
- lng
- lat
- maxDistance (not required)
- page (have default value = 0)
- size (have default value = 20)

---

## Save Properties

### Save Property
POST /api/v1/saved-properties/{propertyId}

---

### Get Saved Properties
GET /api/v1/saved-properties

---

## 📄 Initial Contracts

### Create Contract
POST /api/v1/initial-contracts

---

### Accept Contract (Owner)
POST /api/v1/initial-contracts/{id}/accept

---

### Reject Contract
POST /api/v1/initial-contracts/{id}/reject

---

### Get Contracts
GET /api/v1/initial-contracts

---

## ⚖️ Lawyer APIs

### Review Contract
GET /api/v1/lawyer/initial-contracts

---

## 🔔 Notifications (WebSocket)

### Connection Endpoint
/ws

### Subscription Example
/queue/notifications

---

## ⚙️ Admin APIs

### Get All Users
GET /api/v1/admin/users

---

### Delete User
DELETE /api/v1/admin/users/{id}

---

## 📌 Notes

- All secured endpoints require a valid JWT token.
- Use `Authorization: Bearer <token>` header.
- Rate limiting is applied to prevent abuse.
- Error responses follow a consistent structure using DTOs.

---