# Quick Start Guide - REST API

## Installation & Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- Spring Boot 3.2.5
- PostgreSQL (or configured database)

### Step 1: Build the Project
```bash
cd /path/to/oshxona
./mvnw clean package
```

### Step 2: Run the Application
```bash
./mvnw spring-boot:run
```

Or with production profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Step 3: Verify API is Running
```bash
curl http://localhost:8080/api/health
```

Expected response:
```json
{
  "status": "healthy",
  "service": "Telegram Food Voting Bot API",
  "version": "1.0.0"
}
```

## Quick API Testing

### Test 1: Check API Info
```bash
curl http://localhost:8080/api/info
```

### Test 2: Get Dashboard Data
```bash
curl http://localhost:8080/api/stats/dashboard
```

### Test 3: Get Top Dishes
```bash
curl "http://localhost:8080/api/stats/top-dishes?limit=5"
```

### Test 4: Get Category Statistics
```bash
curl http://localhost:8080/api/stats/category/breakfast
```

## Using the Example Dashboard

### Option 1: Local File
1. Open `dashboard-example.html` in your web browser
2. Dashboard will automatically fetch data from `http://localhost:8080/api`
3. Data refreshes every 10 seconds

### Option 2: Serve with HTTP Server
If you have Python installed:
```bash
cd /path/to/oshxona
python -m http.server 3000
```

Then visit: `http://localhost:3000/dashboard-example.html`

### Option 3: Modify the API URL
Edit `dashboard-example.html` and change:
```javascript
const API_URL = 'http://localhost:8080/api';
```

To your server address, e.g.:
```javascript
const API_URL = 'http://your-server.com:8080/api';
```

## API Endpoints Reference

### General
- `GET /api/health` - Health status
- `GET /api/info` - API info

### Statistics
- `GET /api/stats/dashboard` - Complete dashboard data
- `GET /api/stats/top-dishes?limit=5` - Top dishes
- `GET /api/stats/category/{category}` - Category stats
- `GET /api/stats/dish/{dishId}` - Dish stats

## Troubleshooting

### Error: "Cannot GET /api/health"
- Check if the server is running on port 8080
- Check firewall settings
- Verify Spring Boot started successfully

### Error: CORS errors in dashboard
- Make sure `CorsConfig.java` is present in `config` folder
- Restart the server after adding configuration

### Error: "No data available" in dashboard
- Check if there are any votes in the database
- Try adding some votes through the Telegram bot first
- Check database connection in application.properties

### Error: Connection refused
- Ensure Spring Boot application is running
- Check if port 8080 is not used by another application
- Try changing port in application.properties:
  ```properties
  server.port=8081
  ```

## Performance Tips

1. **Database Indexing**: Add indexes to improve query performance
   ```sql
   CREATE INDEX idx_vote_date ON votes(vote_date);
   CREATE INDEX idx_vote_category ON votes(category);
   CREATE INDEX idx_dish_category ON dishes(category);
   ```

2. **Connection Pooling**: Configured in Spring Boot by default

3. **Caching**: Consider adding Spring Cache annotations to services

## Production Deployment

### Environment Variables
Set these before running:

```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:postgresql://your-db:5432/oshxona
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password

# Bot
export BOT_TOKEN=your_telegram_bot_token
export BOT_USERNAME=your_bot_username

# Spring
export SPRING_PROFILES_ACTIVE=prod

# Server
export SERVER_PORT=8080
```

### Docker Deployment (Optional)

Create a `Dockerfile`:
```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/meal-vote-bot-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

Build and run:
```bash
docker build -t meal-vote-bot .
docker run -p 8080:8080 --env-file .env meal-vote-bot
```

## Files Modified/Created

**New Files:**
- `src/main/java/com/example/demo/dto/DishVoteDTO.java`
- `src/main/java/com/example/demo/dto/StatisticsDTO.java`
- `src/main/java/com/example/demo/dto/DashboardDataDTO.java`
- `src/main/java/com/example/demo/Service/ApiStatisticsService.java`
- `src/main/java/com/example/demo/controller/StatisticsController.java`
- `src/main/java/com/example/demo/controller/ApiController.java`
- `src/main/java/com/example/demo/config/CorsConfig.java`
- `dashboard-example.html`
- `API_DOCUMENTATION.md`
- `REST_API_IMPLEMENTATION.md`
- `QUICK_START.md` (this file)

**Modified Files:**
- `src/main/java/com/example/demo/repository/VoteRepository.java` (added new query methods)
- `src/main/resources/application.properties` (added API configuration)

## Next Steps

1. ✅ API endpoints are ready
2. 📊 Open `dashboard-example.html` to test
3. 📝 Check `API_DOCUMENTATION.md` for detailed endpoint information
4. 🚀 Deploy to production with your settings
5. 🔗 Integrate with your website

## Support

For issues or questions:
1. Check `API_DOCUMENTATION.md`
2. Check `REST_API_IMPLEMENTATION.md`
3. Review the example dashboard in `dashboard-example.html`
4. Check Spring Boot logs for errors

---

**Ready to go!** Your REST API is fully functional. 🎉
