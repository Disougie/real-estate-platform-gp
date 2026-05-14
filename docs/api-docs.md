# Real Estate Platform API Documentation

## Base URLs

- **Development:** `http://localhost:8080`
- **Production:** `https://real-estate-platform-gp.onrender.com`

## Authentication

Most endpoints require a Bearer Token (JWT) for authentication. Include the token in the `Authorization` header.

---

## 1. Authentication & Account Management

### POST `/api/v1/registration`
Register a new user account.

- **Auth Required:** No
- **Request Body:** `RegistrationRequest`


| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | User's full name |
| phone | string | No | Phone number |
| email | string (email) | Yes | Email address |
| password | string | Yes | Password |
| confirm_password | string | Yes | Confirm password |

- **Success Response:** `200 OK` → `RegistrationResponse` (id)

---

### POST `/api/v1/login`
Authenticate a user and receive a token.

- **Auth Required:** No
- **Request Body:** `LoginRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string (email) | Yes | Email address |
| password | string | Yes | Password |

- **Success Response:** `200 OK` → `LoginResponse` (name, role, id)

---

### GET `/api/v1/token/verify`
Verify email confirmation token.

- **Auth Required:** No
- **Query Parameters:** `token` (string, required)
- **Success Response:** `200 OK`

---

### POST `/api/v1/token/resend`
Resend email confirmation token.

- **Auth Required:** No
- **Request Body:** `ResendTokenRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string (email) | Yes | Email address |

- **Success Response:** `200 OK`

---

### POST `/api/v1/forgot-password`
Request a password reset token.

- **Auth Required:** No
- **Request Body:** `ForgotPasswordRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | Email address |

- **Success Response:** `200 OK`

---

### POST `/api/v1/reset-password`
Reset password using a token.

- **Auth Required:** No
- **Query Parameters:** `request` (`ResetPasswordRequest` object, required)

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| token | string | Yes | Reset token |
| newPassword | string (8-32 chars) | Yes | New password |
| confirmPassword | string (8-32 chars) | Yes | Confirm password |

- **Success Response:** `200 OK`

---

### POST `/api/v1/change/password`
Change password for authenticated user.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `ChangePasswordRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| oldPassword | string | Yes | Current password |
| newPassword | string | Yes | New password |

- **Success Response:** `200 OK`

---

### POST `/api/v1/change/email`
Request to change email address.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `ChangeEmailRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| newEmail | string | Yes | New email address |

- **Success Response:** `200 OK`

---

### GET `/api/v1/token/verify-change`
Verify email change token.

- **Auth Required:** No
- **Query Parameters:** `token` (string, required)
- **Success Response:** `200 OK`

---

### POST `/api/v1/change/phone`
Change phone number.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `ChangePhoneRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| phone | string | Yes | New phone number |
| password | string | Yes | Current password |

- **Success Response:** `200 OK`

---

### DELETE `/api/v1/users`
Disable (delete) user account.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `DisableRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| password | string | Yes | Password for confirmation |

- **Success Response:** `200 OK`

---

## 2. Property Management

### GET `/api/v1/properties`
Get paginated list of all properties.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `page` (int32, default: 0) - Page number
  - `size` (int32, default: 20) - Page size
- **Success Response:** `200 OK` → `PageResponsePropertyBriefResponse`

---

### POST `/api/v1/properties`
Add a new property advertisement.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `PropertyAdPostRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| title | string | Yes | Property title |
| price | double | Yes | Price |
| type | enum | Yes | RENT, PURCHASE, COMMERCIAL_RENT, COMMERCIAL_PURCHASE, STUDENT |
| lng | double | Yes | Longitude |
| lat | double | Yes | Latitude |
| description | string | No | Property description |
| rooms | int32 | No | Number of rooms |
| baths | int32 | No | Number of bathrooms |
| images | array (binary) | No | Property images |
| city | string | Yes | City name |
| area | string | Yes | Area name |
| size | int32 | Yes | Property size (sqm) |

- **Success Response:** `200 OK` → `PropertyAdCreationResponse` (property_id)

---

### GET `/api/v1/properties/{id}`
Get detailed info of a property by ID.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (string, required)
- **Success Response:** `200 OK` → `PropertyDetailedResponse`

---

### PATCH `/api/v1/properties/{id}`
Update a property ad.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (string, required)
- **Request Body:** `PropertyPatchRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| title | string | No | Property title |
| description | string | No | Property description |
| price | double | No | Price |
| images | array (binary) | No | Property images |

- **Success Response:** `200 OK`

---

### DELETE `/api/v1/properties/{id}`
Delete a property ad.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (string, required)
- **Success Response:** `200 OK`

---

### GET `/api/v1/properties/my-properties`
Get properties owned by the authenticated user.

- **Auth Required:** Yes (BearerAuth)
- **Success Response:** `200 OK` → Array of `Property` 

---

### GET `/api/v1/properties/my-property/{id}`
Get a specific property owned by the authenticated user.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (string, required)
- **Success Response:** `200 OK` → `PropertyDetailedResponse`

---

### POST `/api/v1/properties/rating/{id}`
Rate a property ad.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (string, required)
- **Request Body:** `ReviewRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| stars | double | Yes | Rating (0-5) |

- **Success Response:** `200 OK`

---

## 3. Property Search

### GET `/api/v1/properties/search`
Search properties by text query.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `text` (string, required) - Search text
  - `page` (int32, default: 0) - Page number
  - `size` (int32, default: 20) - Page size
- **Success Response:** `200 OK` → `PageResponsePropertyBriefResponse`

---

### GET `/api/v1/properties/search/filter`
Search properties by filters.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `type` (enum) - RENT, PURCHASE, COMMERCIAL_RENT, COMMERCIAL_PURCHASE, STUDENT
  - `city` (string) - City name
  - `area` (string) - Area name
  - `minRooms` (int32) - Minimum rooms
  - `maxRooms` (int32) - Maximum rooms
  - `minBaths` (int32) - Minimum bathrooms
  - `maxBaths` (int32) - Maximum bathrooms
  - `minPrice` (double) - Minimum price
  - `maxPrice` (int32) - Maximum price
  - `minSize` (int32) - Minimum size (sqm)
  - `maxSize` (int32) - Maximum size (sqm)
  - `page` (int32, default: 0) - Page number
  - `size` (int32, default: 20) - Page size
- **Success Response:** `200 OK` → `PageResponsePropertyBriefResponse`

---

### GET `/api/v1/properties/search/coord`
Search properties by GPS coordinates.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `lng` (double, required) - Longitude
  - `lat` (double, required) - Latitude
  - `maxDistance` (double) - Maximum distance (km)
- **Success Response:** `200 OK` → Array of `PropertyBriefResponse`

---

### GET `/api/v1/recommendation`
Get recommended properties for the user.

- **Auth Required:** Yes (BearerAuth)
- **Success Response:** `200 OK` → Array of `PropertyDetailedResponse`

---

## 4. Saved Properties

### GET `/api/v1/saved-properties`
Get all saved properties for the user.

- **Auth Required:** Yes (BearerAuth)
- **Success Response:** `200 OK` → Array of `PropertyDetailedResponse`

---

### POST `/api/v1/saved-properties`
Save a property to the user's list.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `SavePropertyRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| property_id | string | Yes | ID of property to save |

- **Success Response:** `200 OK`

---

### DELETE `/api/v1/saved-properties/{id}`
Remove a property from saved list.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (string, required)
- **Success Response:** `200 OK`

---

## 5. Reservations

### GET `/api/v1/initial-contracts`
Get contracts for the authenticated user.

- **Auth Required:** Yes (BearerAuth)
- **Success Response:** `200 OK` → Array of `initialContractResponse`

---

### POST `/api/v1/initial-contracts`
Create a new initial contract.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `InitialContractCreationRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| property_id | string | Yes | ID of the property |
| rentDuration | int32 | No | Rental duration (months) |

- **Success Response:** `200 OK` → `InitialContractCreationResponse` (id)

---

### GET `/api/v1/initial-contracts/{id}`
Get a specific contract by ID.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK` → `initialContractResponse`

---

### POST `/api/v1/initial-contracts/{id}/accept`
Accept a contract.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK`

---

### POST `/api/v1/initial-contracts/{id}/reject`
Reject a contract.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK`

---

## 6. Lawyer Endpoints

### GET `/api/v1/lawyer/initial-contracts`
Get pending contracts for lawyer.

- **Auth Required:** Yes (BearerAuth)
- **Success Response:** `200 OK` → Array of `initialContractResponse`

---

### GET `/api/v1/lawyer/initial-contracts/my-contracts`
Get contracts assigned to the lawyer.

- **Auth Required:** Yes (BearerAuth)
- **Success Response:** `200 OK` → Array of `initialContractResponse`

---

### GET `/api/v1/lawyer/initial-contracts/{id}`
Get a specific contract by ID for lawyer.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK` → `initialContractResponse`

---

### POST `/api/v1/lawyer/initial-contracts/{id}/working-on`
Mark contract as being worked on.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK`

---

### POST `/api/v1/lawyer/initial-contracts/{id}/complete`
Mark contract as complete.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK`

---

### POST `/api/v1/lawyer/initial-contracts/{id}/cancel`
Cancel a contract.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK`

---

### POST `/api/v1/lawyer/initial-contracts/{id}/ban`
Ban a contract.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK`

---

## 7. Admin Endpoints

### GET `/api/v1/admin/users`
Get paginated list of all users.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponseAppUserResponse`

---

### GET `/api/v1/admin/users/search`
Search users by text.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `text` (string, required)
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponseAppUserResponse`

---

### DELETE `/api/v1/admin/users/{id}`
Delete a user by ID.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK`

---

### POST `/api/v1/admin/users`
Register a new user as admin.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `RegistrationRequest`
- **Success Response:** `200 OK` → `RegistrationResponse`

---

### GET `/api/v1/admin/lawyers`
Get paginated list of lawyers.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponseAppUserResponse`

---

### GET `/api/v1/admin/lawyers/search`
Search lawyers by text.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `text` (string, required)
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponseAppUserResponse`

---

### POST `/api/v1/admin/lawyers`
Register a new lawyer as admin.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `RegistrationRequest`
- **Success Response:** `200 OK` → `RegistrationResponse`

---

### GET `/api/v1/admin/admins`
Get paginated list of admins.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponseAppUserResponse`

---

### GET `/api/v1/admin/admins/search`
Search admins by text.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `text` (string, required)
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponseAppUserResponse`

---

### POST `/api/v1/admin/admins`
Register a new admin.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `RegistrationRequest`
- **Success Response:** `200 OK` → `RegistrationResponse`

---

### GET `/api/v1/admin/properties`
Get paginated list of all properties (admin view).

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponsePropertyAdminResponse`

---

### GET `/api/v1/admin/properties/search`
Search properties by text (admin view).

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `text` (string, required)
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponsePropertyAdminResponse`

---

### DELETE `/api/v1/admin/properties/{id}`
Delete a property by ID as admin.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (string, required)
- **Success Response:** `200 OK`

---

## 8. Blog Management

### GET `/api/v1/blogs`
Get all blogs for users.

- **Auth Required:** Yes (BearerAuth)
- **Success Response:** `200 OK` → Array of `BlogUsersReponse`

---

### GET `/api/v1/admin/blogs`
Get paginated list of blogs (admin view).

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponseBlogAdminsResponse`

---

### GET `/api/v1/admin/blogs/search`
Search blogs by text (admin).

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `text` (string, required)
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponseBlogAdminsResponse`

---

### POST `/api/v1/admin/blogs`
Add a new blog as admin.

- **Auth Required:** Yes (BearerAuth)
- **Request Body:** `BlogRequest`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| title | string | Yes | Blog title |
| content | string | Yes | Blog content |

- **Success Response:** `200 OK` → `BlogCreationResponse` (id)

---

### PATCH `/api/v1/admin/blogs/{id}`
Update a blog as admin.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Request Body:** `BlogRequest`
- **Success Response:** `200 OK`

---

### DELETE `/api/v1/admin/blogs/{id}`
Delete a blog as admin.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK`

---

## 9. Other Endpoints

### GET `/api/v1/users/{id}`
Get user information by ID.

- **Auth Required:** Yes (BearerAuth)
- **Path Parameters:** `id` (int64, required)
- **Success Response:** `200 OK` → `AppUserResponse`

---

### GET `/api/v1/notifications`
Get notifications for the authenticated user.

- **Auth Required:** Yes (BearerAuth)
- **Query Parameters:**
  - `page` (int32, default: 0)
  - `size` (int32, default: 10)
- **Success Response:** `200 OK` → `PageResponseNotificationResponse`

---

### GET `/keep-alive`
Keep the server from shutting down (e.g., on Render).

- **Auth Required:** No
- **Success Response:** `200 OK`

---

## Authentication Scheme

| Name | Type | Scheme | Format |
|------|------|--------|--------|
| BearerAuth | HTTP | bearer | JWT |

Include the token in requests.

---

## Enums

### Property Types
Values for `type` field: `RENT`, `PURCHASE`, `COMMERCIAL_RENT`, `COMMERCIAL_PURCHASE`, `STUDENT`

### Property Status
Values for `status` field: `AVAILABLE`, `PENDING_PROCESSING`, `COMPLETED`, `BANNED`

### Contract Status
Values for `status` field: `PENDING_APPROVAL`, `PENDING_PROCESSING`, `UNDER_PROCESS`, `COMPLETED`, `REJECT`, `EXPIRED`, `BANNED`

---

## Response DTOs

Below are the data structures returned by the API endpoints.

### RegistrationResponse
| Field | Type | Description |
|-------|------|-------------|
| id | int64 | ID of the newly registered user |

### LoginResponse
| Field | Type | Description |
|-------|------|-------------|
| name | string | User's full name |
| role | string | User's role (e.g., USER, ADMIN, LAWYER) |

### AppUserResponse
| Field | Type | Description |
|-------|------|-------------|
| id | int64 | User ID |
| name | string | Full name |
| email | string | Email address |
| phone | string | Phone number |
| enabled | boolean | Whether the account is enabled |
| deletedAt | date-time | Deletion timestamp (if deleted) |

### PropertyDetailedResponse
| Field | Type | Description |
|-------|------|-------------|
| id | string | Property ID |
| title | string | Property title |
| ownerName | string | Name of the owner |
| decription | string | Property description (note: field name misspelled) |
| price | double | Price |
| type | enum | RENT, PURCHASE, COMMERCIAL_RENT, COMMERCIAL_PURCHASE, STUDENT |
| status | enum | AVAILABLE, PENDING_PROCESSING, COMPLETED, BANNED |
| features | Features | Rooms, baths, size |
| coordinates | array[double] | [lng, lat] |
| city | string | City name |
| area | string | Area name |
| imagesUrls | array[string] | URLs of property images |
| review | Review | Rating summary |

### PropertyBriefResponse
| Field | Type | Description |
|-------|------|-------------|
| id | string | Property ID |
| title | string | Property title |
| price | double | Price |
| area | string | Area name |
| imageUrl | string | URL of the first property image |

### PropertyAdminResponse
| Field | Type | Description |
|-------|------|-------------|
| id | string | Property ID |
| title | string | Property title |
| owner | string | Owner name |
| decription | string | Description (misspelled) |
| price | double | Price |
| type | enum | Property type |
| status | enum | Property status |
| features | Features | Rooms, baths, size |
| coordinates | array[double] | [lng, lat] |
| city | string | City name |
| area | string | Area name |
| review | Review | Rating summary |

### Property
| Field | Type | Description |
|-------|------|-------------|
| id | string | Property ID |
| title | string | Title |
| price | double | Price |
| type | enum | Property type |
| ownerId | int64 | Owner user ID |
| status | enum | Property status |
| features | Features | Rooms, baths, size |
| mapsLocation | GeoJsonPoint | GeoJSON point |
| location | Location | City and area |
| images | array[Image] | List of images |
| description | string | Description |
| review | Review | Rating summary |
| createdAt | date-time | Creation timestamp |
| deletedAt | date-time | Deletion timestamp |

### Features
| Field | Type | Description |
|-------|------|-------------|
| rooms | int32 | Number of rooms |
| baths | int32 | Number of bathrooms |
| size | int32 | Size in square meters |

### Review
| Field | Type | Description |
|-------|------|-------------|
| stars | double | Average rating (0-5) |
| noOfReview | int32 | Number of reviews |

### GeoJsonPoint
| Field | Type | Description |
|-------|------|-------------|
| x | double | Longitude |
| y | double | Latitude |
| type | string | Usually "Point" |
| coordinates | array[double] | [lng, lat] |

### Location
| Field | Type | Description |
|-------|------|-------------|
| city | string | City name |
| area | string | Area name |

### Image
| Field | Type | Description |
|-------|------|-------------|
| imageUrl | string | Public URL of the image |
| fileId | string | Internal file identifier |

### initialContractResponse
| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Contract ID |
| created_at | string | Creation date |
| expire_at | string | Expiration date |
| owner_data | UserData | Owner information |
| seeker_data | UserData | Seeker information |
| property_data | PropertyData | Property summary |
| financial_data | FinancialData | Rent and total amount |
| status | enum | PENDING_APPROVAL, PENDING_PROCESSING, UNDER_PROCESS, COMPLETED, REJECT, EXPIRED, BANNED |

### UserData
| Field | Type | Description |
|-------|------|-------------|
| name | string | User's name |
| phone | string | Phone number |
| email | string | Email address |

### PropertyData
| Field | Type | Description |
|-------|------|-------------|
| purpose | string | Rent or purchase |
| location | string | City/area |
| size | string | Size description |

### FinancialData
| Field | Type | Description |
|-------|------|-------------|
| rent_price | double | Monthly rent price |
| rent_duration | int32 | Duration in months |
| overall_amount | double | Total amount |

### BlogUsersReponse
| Field | Type | Description |
|-------|------|-------------|
| title | string | Blog title |
| content | string | Blog content |

### BlogAdminsResponse
| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Blog ID |
| writer | string | Admin who wrote the blog |
| title | string | Blog title |
| content | string | Blog content |
| createdAt | date-time | Creation timestamp |

### NotificationResponse
| Field | Type | Description |
|-------|------|-------------|
| message | string | Notification message |

### PageResponsePropertyBriefResponse
| Field | Type | Description |
|-------|------|-------------|
| content | array[PropertyBriefResponse] | List of properties |
| totalPages | int32 | Total number of pages |
| totalElements | int64 | Total number of elements |
| page | int32 | Current page number |
| size | int32 | Page size |
| last | boolean | Whether this is the last page |

### PageResponseAppUserResponse
Same pagination structure as above, with `content` of type `array[AppUserResponse]`.

### PageResponsePropertyAdminResponse
Same pagination structure, with `content` of type `array[PropertyAdminResponse]`.

### PageResponseBlogAdminsResponse
Same pagination structure, with `content` of type `array[BlogAdminsResponse]`.

### PageResponseNotificationResponse
Same pagination structure, with `content` of type `array[NotificationResponse]`.

### PropertyAdCreationResponse
| Field | Type | Description |
|-------|------|-------------|
| property_id | string | ID of the created property ad |

### InitialContractCreationResponse
| Field | Type | Description |
|-------|------|-------------|
| id | int64 | ID of the created contract |

### BlogCreationResponse
| Field | Type | Description |
|-------|------|-------------|
| id | int64 | ID of the created blog |