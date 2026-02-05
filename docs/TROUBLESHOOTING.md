# Troubleshooting Guide

## Table of Contents

1. [Database Issues](#database-issues)
2. [Authentication Issues](#authentication-issues)
3. [API Issues](#api-issues)
4. [Performance Issues](#performance-issues)
5. [Deployment Issues](#deployment-issues)
6. [Common Error Messages](#common-error-messages)

## Database Issues

### Issue: Database Connection Failed

**Symptoms:**
- Application fails to start
- Error: "Unable to connect to database"
- Connection timeout errors

**Diagnosis:**
```bash
# Check MySQL status
sudo systemctl status mysql

# Test connection
mysql -u root -p -h localhost

# Check port
netstat -an | grep 3306
```

**Solutions:**

1. **Verify MySQL is running:**
```bash
sudo systemctl start mysql
sudo systemctl enable mysql
```

2. **Check credentials:**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=correct_username
spring.datasource.password=correct_password
```

3. **Create database if missing:**
```sql
CREATE DATABASE IF NOT EXISTS ecommerce_db;
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'your_user'@'localhost';
FLUSH PRIVILEGES;
```

4. **Check firewall:**
```bash
sudo ufw allow 3306/tcp
```

### Issue: Flyway Migration Failed

**Symptoms:**
- Application fails to start
- Error: "Migration failed"
- Schema version mismatch

**Solutions:**

1. **Check migration history:**
```sql
SELECT * FROM flyway_schema_history;
```

2. **Repair failed migration:**
```bash
mvn flyway:repair
```

3. **Clean and rebuild (CAUTION: Deletes all data):**
```bash
mvn flyway:clean flyway:migrate
```

4. **Manual fix:**
```sql
-- Remove failed migration entry
DELETE FROM flyway_schema_history WHERE success = 0;
```

### Issue: Slow Database Queries

**Symptoms:**
- API responses are slow
- High database CPU usage
- Timeout errors

**Diagnosis:**
```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- Check slow queries
SHOW FULL PROCESSLIST;

-- Analyze query performance
EXPLAIN SELECT * FROM products WHERE category = 'Electronics';
```

**Solutions:**

1. **Add missing indexes:**
```sql
CREATE INDEX idx_product_category ON products(category);
CREATE INDEX idx_cart_user_status ON carts(user_id, status);
```

2. **Optimize queries:**
```java
// Use pagination
Pageable pageable = PageRequest.of(page, size);

// Use fetch joins for related entities
@Query("SELECT c FROM Cart c JOIN FETCH c.items WHERE c.id = :id")
```

3. **Configure connection pool:**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

## Authentication Issues

### Issue: JWT Token Invalid

**Symptoms:**
- 401 Unauthorized error
- "Invalid token" message
- Authentication fails

**Diagnosis:**
```bash
# Decode JWT token (without verification)
echo "YOUR_TOKEN" | cut -d'.' -f2 | base64 -d | jq
```

**Solutions:**

1. **Verify token format:**
```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

2. **Check token expiration:**
```properties
# Increase expiration time
jwt.expiration=86400000
```

3. **Verify JWT secret:**
```properties
# Ensure secret is consistent
jwt.secret=YourSecretKeyMustBeTheSameEverywhereAndLongEnough
```

4. **Clear and regenerate token:**
```bash
# Login again to get new token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'
```

### Issue: Password Validation Fails

**Symptoms:**
- Registration fails
- "Password does not meet requirements" error

**Solutions:**

1. **Check password requirements:**
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@$!%*?&)

2. **Valid password examples:**
```
SecurePass123!
MyP@ssw0rd
Test1234!
```

### Issue: User Already Exists

**Symptoms:**
- 409 Conflict error
- "Email already registered" message

**Solutions:**

1. **Use different email:**
```json
{
  "email": "newuser@example.com",
  "password": "SecurePass123!"
}
```

2. **Reset existing user (if needed):**
```sql
DELETE FROM users WHERE email = 'user@example.com';
```

## API Issues

### Issue: 404 Not Found

**Symptoms:**
- Endpoint not found
- Wrong URL

**Solutions:**

1. **Verify endpoint URL:**
```
Correct: http://localhost:8080/api/products/search
Wrong:   http://localhost:8080/products/search
```

2. **Check application is running:**
```bash
curl http://localhost:8080/actuator/health
```

3. **Review controller mappings:**
```bash
# Check application logs for registered endpoints
grep "Mapped" application.log
```

### Issue: 400 Bad Request - Validation Error

**Symptoms:**
- Request rejected
- Validation error messages

**Solutions:**

1. **Check request body format:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

2. **Verify required fields:**
- All @NotNull fields must be present
- All @NotBlank fields must have values
- Email must be valid format
- Numbers must be in valid range

3. **Review validation constraints:**
```java
@Min(value = 1, message = "Quantity must be at least 1")
private Integer quantity;
```

### Issue: 500 Internal Server Error

**Symptoms:**
- Unexpected server error
- Application crash

**Diagnosis:**
```bash
# Check application logs
tail -f logs/application.log

# Check stack trace
grep -A 20 "Exception" logs/application.log
```

**Solutions:**

1. **Review error logs:**
```bash
# Enable debug logging
logging.level.com.ecommerce=DEBUG
```

2. **Check for null pointer exceptions:**
```java
// Add null checks
if (product != null && product.getActive()) {
    // process
}
```

3. **Verify database constraints:**
```sql
-- Check for constraint violations
SHOW ENGINE INNODB STATUS;
```

## Performance Issues

### Issue: Slow API Response

**Symptoms:**
- High latency
- Timeout errors
- Poor user experience

**Diagnosis:**
```bash
# Measure response time
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/api/products

# Check application metrics
curl http://localhost:8080/actuator/metrics
```

**Solutions:**

1. **Enable caching:**
```java
@Cacheable("products")
public ProductResponse getProductById(Long id) {
    // ...
}
```

2. **Optimize queries:**
```java
// Use projections
@Query("SELECT new com.ecommerce.dto.ProductResponse(p.id, p.name, p.price) FROM Product p")
```

3. **Add pagination:**
```java
Pageable pageable = PageRequest.of(page, size);
Page<Product> products = productRepository.findAll(pageable);
```

4. **Configure thread pool:**
```properties
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
```

### Issue: High Memory Usage

**Symptoms:**
- OutOfMemoryError
- Application crashes
- Slow performance

**Diagnosis:**
```bash
# Check memory usage
jps -l
jmap -heap <PID>

# Generate heap dump
jmap -dump:live,format=b,file=heap.bin <PID>
```

**Solutions:**

1. **Increase heap size:**
```bash
java -Xms512m -Xmx2048m -jar ecommerce-api.jar
```

2. **Fix memory leaks:**
```java
// Close resources properly
try (Connection conn = dataSource.getConnection()) {
    // use connection
}
```

3. **Use pagination:**
```java
// Don't load all records at once
Page<Product> products = productRepository.findAll(pageable);
```

## Deployment Issues

### Issue: Application Won't Start

**Symptoms:**
- Application fails to start
- Port already in use
- Configuration errors

**Solutions:**

1. **Check port availability:**
```bash
# Check if port 8080 is in use
lsof -i :8080

# Kill process using port
kill -9 <PID>

# Or change port
server.port=8081
```

2. **Verify Java version:**
```bash
java -version
# Should be Java 17 or higher
```

3. **Check dependencies:**
```bash
mvn dependency:tree
mvn clean install
```

### Issue: Environment Variables Not Set

**Symptoms:**
- Configuration not loaded
- Default values used
- Connection failures

**Solutions:**

1. **Set environment variables:**
```bash
export DATABASE_URL=jdbc:mysql://localhost:3306/ecommerce_db
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=password
export JWT_SECRET=your_secret_key
```

2. **Use .env file:**
```bash
# .env
DATABASE_URL=jdbc:mysql://localhost:3306/ecommerce_db
DATABASE_USERNAME=root
DATABASE_PASSWORD=password
```

3. **Verify variables are loaded:**
```bash
echo $DATABASE_URL
```

## Common Error Messages

### "Resource not found"

**Cause:** Requested entity doesn't exist in database

**Solution:**
- Verify ID is correct
- Check if resource was deleted
- Ensure user has access to resource

### "Insufficient stock"

**Cause:** Product stock is less than requested quantity

**Solution:**
- Check product stock: `SELECT stock_quantity FROM products WHERE id = ?`
- Reduce quantity in request
- Wait for stock replenishment

### "Cart is empty"

**Cause:** Attempting to checkout with no items in cart

**Solution:**
- Add items to cart before checkout
- Verify cart status is ACTIVE
- Check cart items: `SELECT * FROM cart_items WHERE cart_id = ?`

### "Invalid credentials"

**Cause:** Wrong email or password

**Solution:**
- Verify email is correct
- Check password (case-sensitive)
- Reset password if forgotten
- Ensure user exists: `SELECT * FROM users WHERE email = ?`

### "Validation failed"

**Cause:** Request data doesn't meet validation requirements

**Solution:**
- Review validation error messages
- Check required fields
- Verify data formats (email, phone, etc.)
- Ensure values are in valid ranges

## Getting Help

If you're still experiencing issues:

1. **Check logs:**
```bash
tail -f logs/application.log
```

2. **Enable debug mode:**
```properties
logging.level.root=DEBUG
```

3. **Search GitHub issues:**
https://github.com/NavneetBN47/ecommerce/issues

4. **Create new issue:**
Include:
- Error message
- Stack trace
- Steps to reproduce
- Environment details
- Relevant logs

5. **Contact support:**
navneet.bhargavan@ascendion.com
