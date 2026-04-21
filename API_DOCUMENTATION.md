# REST API Documentation - Telegram Food Voting Bot

## Overview
The REST API provides real-time statistics and voting data for the website dashboard. All endpoints return JSON responses and support CORS for cross-origin requests.

## Base URL
```
http://localhost:8080/api
```

## General Endpoints

### 1. Health Check
Check if the API is running.

**Request:**
```
GET /api/health
```

**Response:**
```json
{
  "status": "healthy",
  "service": "Telegram Food Voting Bot API",
  "version": "1.0.0"
}
```

**Status:** 200 OK

---

### 2. API Information
Get detailed API information and available endpoints.

**Request:**
```
GET /api/info
```

**Response:**
```json
{
  "name": "Telegram Food Voting Bot",
  "version": "1.0.0",
  "description": "Real-time voting statistics API for school meal voting system",
  "endpoints": {
    "GET /api/health": "Check API health status",
    "GET /api/info": "Get API information",
    "GET /api/stats/dashboard": "Get complete dashboard data",
    "GET /api/stats/top-dishes": "Get today's top dishes",
    "GET /api/stats/category/{category}": "Get statistics for a category",
    "GET /api/stats/dish/{dishId}": "Get statistics for a specific dish"
  }
}
```

**Status:** 200 OK

---

## Statistics Endpoints

### 3. Dashboard Data
Get comprehensive dashboard data with all statistics for all categories.

**Request:**
```
GET /api/stats/dashboard
```

**Response:**
```json
{
  "date": "2026-04-21",
  "lastUpdated": "2026-04-21 14:30:45",
  "categories": [
    {
      "date": "2026-04-21",
      "category": "breakfast",
      "categoryName": "Breakfast 🥞",
      "totalVotes": 45,
      "totalVoters": 38,
      "topDish": {
        "id": 1,
        "name": "Pancakes",
        "description": "Fluffy pancakes with syrup",
        "category": "breakfast",
        "photoUrl": "https://example.com/pancakes.jpg",
        "votes": 15,
        "percentage": 33.33
      },
      "allDishes": [
        {
          "id": 1,
          "name": "Pancakes",
          "description": "Fluffy pancakes with syrup",
          "category": "breakfast",
          "photoUrl": "https://example.com/pancakes.jpg",
          "votes": 15,
          "percentage": 33.33
        },
        {
          "id": 2,
          "name": "Oatmeal",
          "description": "Warm oatmeal with fruits",
          "category": "breakfast",
          "photoUrl": "https://example.com/oatmeal.jpg",
          "votes": 12,
          "percentage": 26.67
        }
      ]
    }
  ],
  "globalTopDishes": [
    {
      "id": 1,
      "name": "Pancakes",
      "description": "Fluffy pancakes with syrup",
      "category": "breakfast",
      "photoUrl": "https://example.com/pancakes.jpg",
      "votes": 15,
      "percentage": 12.5
    }
  ],
  "totalVotesToday": 120,
  "totalVotersToday": 95
}
```

**Status:** 200 OK

---

### 4. Today's Top Dishes
Get the top voted dishes across all categories.

**Request:**
```
GET /api/stats/top-dishes?limit=5
```

**Query Parameters:**
- `limit` (optional, default: 5) - Maximum number of dishes to return

**Response:**
```json
[
  {
    "id": 1,
    "name": "Pancakes",
    "description": "Fluffy pancakes with syrup",
    "category": "breakfast",
    "photoUrl": "https://example.com/pancakes.jpg",
    "votes": 15,
    "percentage": 12.5
  },
  {
    "id": 5,
    "name": "Pizza",
    "description": "Cheese pizza",
    "category": "lunch",
    "photoUrl": "https://example.com/pizza.jpg",
    "votes": 12,
    "percentage": 10.0
  }
]
```

**Status:** 200 OK

---

### 5. Category Statistics
Get statistics for a specific category (breakfast, lunch, snack).

**Request:**
```
GET /api/stats/category/{category}
```

**Path Parameters:**
- `category` - Category name (breakfast, lunch, snack)

**Example:**
```
GET /api/stats/category/lunch
```

**Response:**
```json
{
  "date": "2026-04-21",
  "category": "lunch",
  "categoryName": "Lunch 🍽️",
  "totalVotes": 50,
  "totalVoters": 42,
  "topDish": {
    "id": 5,
    "name": "Pizza",
    "description": "Cheese pizza",
    "category": "lunch",
    "photoUrl": "https://example.com/pizza.jpg",
    "votes": 12,
    "percentage": 24.0
  },
  "allDishes": [
    {
      "id": 5,
      "name": "Pizza",
      "description": "Cheese pizza",
      "category": "lunch",
      "photoUrl": "https://example.com/pizza.jpg",
      "votes": 12,
      "percentage": 24.0
    }
  ]
}
```

**Status:** 200 OK

---

### 6. Dish Statistics
Get voting statistics for a specific dish.

**Request:**
```
GET /api/stats/dish/{dishId}
```

**Path Parameters:**
- `dishId` - ID of the dish

**Example:**
```
GET /api/stats/dish/1
```

**Response:**
```json
{
  "id": 1,
  "name": "Pancakes",
  "description": "Fluffy pancakes with syrup",
  "category": "breakfast",
  "photoUrl": "https://example.com/pancakes.jpg",
  "votes": 15,
  "percentage": 33.33
}
```

**Status:** 200 OK

**Error Response (Dish not found):**
```
Status: 404 Not Found
```

---

## Error Responses

### 500 Internal Server Error
```json
{
  "error": "Internal server error"
}
```

---

## Usage Examples

### JavaScript/Fetch API

```javascript
// Get dashboard data
fetch('http://localhost:8080/api/stats/dashboard')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));

// Get top dishes
fetch('http://localhost:8080/api/stats/top-dishes?limit=10')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));

// Get category statistics
fetch('http://localhost:8080/api/stats/category/lunch')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));
```

### cURL

```bash
# Health check
curl http://localhost:8080/api/health

# Dashboard data
curl http://localhost:8080/api/stats/dashboard

# Top dishes
curl "http://localhost:8080/api/stats/top-dishes?limit=5"

# Category statistics
curl http://localhost:8080/api/stats/category/lunch

# Dish statistics
curl http://localhost:8080/api/stats/dish/1
```

---

## Response Time Data Types

### DashboardDataDTO
- `date` (LocalDate) - Today's date
- `lastUpdated` (String) - Last update timestamp
- `categories` (List<StatisticsDTO>) - Statistics for each category
- `globalTopDishes` (List<DishVoteDTO>) - Top dishes across all categories
- `totalVotesToday` (Long) - Total number of votes today
- `totalVotersToday` (Long) - Total number of unique voters today

### StatisticsDTO
- `date` (LocalDate) - Date
- `category` (String) - Category name (lowercase)
- `categoryName` (String) - Formatted category name with emoji
- `totalVotes` (Long) - Total votes in category
- `totalVoters` (Long) - Total unique voters in category
- `topDish` (DishVoteDTO) - Top voted dish in category
- `allDishes` (List<DishVoteDTO>) - All dishes in category

### DishVoteDTO
- `id` (Long) - Dish ID
- `name` (String) - Dish name
- `description` (String) - Dish description
- `category` (String) - Category name
- `photoUrl` (String) - URL to dish photo
- `votes` (Long) - Number of votes
- `percentage` (Double) - Percentage of votes in category (rounded to 2 decimals)

---

## Rate Limiting
Currently, there is no rate limiting on the API. Future versions may implement rate limiting.

---

## CORS Configuration
All API endpoints are configured to accept CORS requests from any origin with the following methods:
- `GET` - For all endpoints
- `POST`, `PUT`, `DELETE` - For future expansion

---

## Version History

### v1.0.0 (Current)
- Initial REST API implementation
- Statistics endpoints
- Dashboard data endpoint
- Health check endpoints
