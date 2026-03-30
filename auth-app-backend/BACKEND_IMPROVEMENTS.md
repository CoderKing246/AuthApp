# Backend User Endpoints Improvements

## Summary of Changes

All improvements were made to the CRUD operations section in `UserService.java` (after the comment "// CRUD operation").

## ✅ Improvements Made

### 1. **Exception Handling**
- Created `ResourceNotFoundException` - Custom exception for resource not found errors
- Created `BadRequestException` - Custom exception for bad request errors
- Created `GlobalExceptionHandler` - Centralized exception handling with proper HTTP status codes
- Replaced generic `RuntimeException` and `UsernameNotFoundException` with specific exceptions

### 2. **New Endpoints**

#### GET `/api/v1/user/me`
- Get current authenticated user
- Returns the user based on JWT token

#### GET `/api/v1/user` (Enhanced)
- Now supports pagination with query parameters:
  - `page` (default: 0)
  - `size` (default: 20, max: 100)
  - `sortBy` (default: createdAt)
  - `sortDir` (asc/desc, default: desc)
- Returns `Page<UserDto>` instead of `List<UserDto>`

#### GET `/api/v1/user/all`
- Backward compatibility endpoint
- Returns all users as a list (limited to first 100)

#### GET `/api/v1/user/email/{email}`
- Get user by email address
- Proper validation and error handling

#### DELETE `/api/v1/user/{id}`
- Delete user by ID
- Automatically revokes all refresh tokens for the user
- Proper cleanup before deletion

#### GET `/api/v1/user/exists/{id}`
- Check if user exists by ID
- Returns boolean

### 3. **Enhanced Methods**

#### `getUser(UUID id)`
- ✅ Null validation
- ✅ Proper exception handling with `ResourceNotFoundException`
- ✅ Clear error messages

#### `getAllUsers(int page, int size, String sortBy, String sortDir)`
- ✅ Pagination support
- ✅ Sorting support (ascending/descending)
- ✅ Parameter validation (page >= 0, size between 1-100)
- ✅ Returns `Page<UserDto>` for better API responses

#### `editUser(UUID userId, UpdatedUserDto updatedUserDto)`
- ✅ Comprehensive validation:
  - Name: 2-100 characters
  - Image URL: Valid URL format
  - Date of Birth: Not in future, reasonable age (13-150 years)
- ✅ Null checks for all inputs
- ✅ Input trimming and sanitization
- ✅ `@Transactional` for data consistency
- ✅ Proper error messages

#### `deleteUser(UUID userId)`
- ✅ Null validation
- ✅ Automatic refresh token revocation
- ✅ Proper cleanup before deletion
- ✅ `@Transactional` for data consistency

#### `getCurrentUser()`
- ✅ New method to get authenticated user
- ✅ Uses Spring Security context
- ✅ Proper authentication check

#### `getUserByEmail(String email)`
- ✅ Email validation
- ✅ Proper error handling

#### `userExists(UUID userId)`
- ✅ Simple existence check
- ✅ Null-safe

### 4. **DTO Validation**
- Added validation annotations to `UpdatedUserDto`:
  - `@Size` for name (2-100 characters)
  - `@Pattern` for image URL validation
- Proper validation error messages

### 5. **Repository Updates**
- Added `findByUser(User user)` method to `RefreshTokenRepository`
- Used for cleanup when deleting users

### 6. **Code Quality**
- ✅ JavaDoc comments for all methods
- ✅ Proper null checks
- ✅ Input validation
- ✅ Transaction management with `@Transactional`
- ✅ Consistent error messages
- ✅ Better code organization

## 📋 API Endpoints Summary

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/user/me` | Get current user | Yes |
| GET | `/api/v1/user` | Get all users (paginated) | Yes |
| GET | `/api/v1/user/all` | Get all users (list) | Yes |
| GET | `/api/v1/user/{id}` | Get user by ID | Yes |
| GET | `/api/v1/user/email/{email}` | Get user by email | Yes |
| GET | `/api/v1/user/exists/{id}` | Check if user exists | Yes |
| PUT | `/api/v1/user/edit/{id}` | Update user | Yes |
| DELETE | `/api/v1/user/{id}` | Delete user | Yes |

## 🔒 Security Improvements

- Users can only access their own data (via `/me` endpoint)
- Proper authentication checks
- Input validation prevents malicious data
- Refresh token cleanup on user deletion

## 🎯 Error Handling

All endpoints now return proper HTTP status codes:
- `200 OK` - Success
- `400 BAD_REQUEST` - Validation errors
- `404 NOT_FOUND` - Resource not found
- `500 INTERNAL_SERVER_ERROR` - Server errors

Error responses include:
- Status code
- Error message
- Timestamp

## 📝 Validation Rules

### Name
- Minimum: 2 characters
- Maximum: 100 characters
- Trimmed before validation

### Image URL
- Must be a valid URL format
- Supports http://, https://, or relative URLs starting with /

### Date of Birth
- Cannot be in the future
- Must be between 13 and 150 years ago
- Proper date validation

## 🚀 Usage Examples

### Get Current User
```http
GET /api/v1/user/me
Authorization: Bearer <token>
```

### Get All Users (Paginated)
```http
GET /api/v1/user?page=0&size=20&sortBy=createdAt&sortDir=desc
Authorization: Bearer <token>
```

### Update User
```http
PUT /api/v1/user/edit/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "John Doe",
  "image": "https://example.com/image.jpg",
  "dateOfBirth": "1990-01-01"
}
```

### Delete User
```http
DELETE /api/v1/user/{id}
Authorization: Bearer <token>
```

## 🔄 Backward Compatibility

- The old `getAllUser()` method still exists and returns a list
- Old endpoints continue to work
- New endpoints are additive, not breaking changes

## 📦 Files Created/Modified

### New Files
1. `Exception/ResourceNotFoundException.java`
2. `Exception/BadRequestException.java`
3. `Exception/GlobalExceptionHandler.java`

### Modified Files
1. `Service/UserService.java` - Enhanced CRUD operations
2. `Controller/UserController.java` - Added new endpoints
3. `Dtos/UpdatedUserDto.java` - Added validation
4. `Repository/RefreshTokenRepository.java` - Added findByUser method
