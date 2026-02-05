# Troubleshooting Guide

## Common Issues and Solutions

### 1. Database Connection Issues

#### Problem: "Connection refused" or "Connection timeout"

**Symptoms:**
```
java.sql.SQLException: Connection refused
```

**Solutions:**

1. **Verify PostgreSQL is running:**
   ```bash
   # Linux/Mac
   sudo systemctl status postgresql
   # or
   pg_ctl status
   
   # Windows
   net start postgresql-x64-15
   ```

2. **Check connection parameters:**
   - Verify database name, username, and password in `application.yml`
   - Ensure PostgreSQL is listening on the correct port (default: 5432)
   
3. **Test connection manually:**
   ```bash
   psql -h localhost -U postgres -d ecommerce_db
   ```

4. **Check pg_hba.conf:**
   - Ensure local connections are allowed
   - Add line: `host all all 127.0.0.1/32 md5`

#### Problem: "Database does not exist"

**Solution:**
```bash
psql -U postgres
CREATE DATABASE ecommerce_db;
\q
```

### 2. Migration Issues

#### Problem: Flyway migration fails

**Symptoms:**
```
FlywayException: Validate failed: Migration checksum mismatch
```

**Solutions:**

1. **Clean and re-run migrations (DEVELOPMENT ONLY):**
   ```bash
   mvn flyway:clean flyway:migrate
   ```

2. **Repair migration history:**
   ```bash
   mvn flyway:repair
   ```

3. **Manual fix:**
   ```sql
   DELETE FROM flyway_schema_history WHERE success = false;
   ```

#### Problem: "Table already exists"

**Solution:**
Either drop existing tables or use baseline:
```bash
mvn flyway:baseline
```

### 3. Authentication Issues

#### Problem: "Invalid JWT token" or "Token expired"

**Solutions:**

1. **Generate new token:**
   - Login again to get a fresh token
   - Check token expiration time in configuration

2. **Verify JWT secret:**
   - Ensure `app.jwt.secret` is properly configured
   - Secret must be at least 256 bits (32 characters)

3. **Check token format:**
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

#### Problem: "Bad credentials" on login

**Solutions:**

1. **Verify password:**
   - Ensure password meets requirements (8+ chars, uppercase, lowercase, digit, special char)
   - Check for typos

2. **Reset test user password:**
   ```sql
   UPDATE users 
   SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
   WHERE username = 'testuser';
   -- Password: Test@123
   ```

### 4. Cart Issues

#### Problem: "Insufficient stock" error

**Solutions:**

1. **Check product stock:**
   ```sql
   SELECT product_id, product_name, stock_quantity 
   FROM products 
   WHERE product_id = ?;
   ```

2. **Update stock if needed:**
   ```sql
   UPDATE products 
   SET stock_quantity = 100 
   WHERE product_id = ?;
   ```

#### Problem: Cart not created or items not added

**Solutions:**

1. **Verify authentication:**
   - Ensure valid JWT token is provided
   - Check token hasn't expired

2. **Check product exists and is active:**
   ```sql
   SELECT * FROM products WHERE product_id = ? AND is_active = true;
   ```

3. **Review application logs:**
   ```bash
   tail -f logs/spring-boot-application.log
   ```

### 5. Build Issues

#### Problem: Maven build fails

**Solutions:**

1. **Clean and rebuild:**
   ```bash
   mvn clean install -U
   ```

2. **Delete .m2 cache:**
   ```bash
   rm -rf ~/.m2/repository
   mvn clean install
   ```

3. **Check Java version:**
   ```bash
   java -version  # Should be 17+
   mvn -version
   ```

#### Problem: Lombok not working

**Solutions:**

1. **Enable annotation processing in IDE:**
   - IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
   - Eclipse: Install Lombok plugin

2. **Rebuild project:**
   ```bash
   mvn clean compile
   ```

### 6. Performance Issues

#### Problem: Slow query performance

**Solutions:**

1. **Check missing indexes:**
   ```sql
   SELECT schemaname, tablename, indexname 
   FROM pg_indexes 
   WHERE schemaname = 'public';
   ```

2. **Analyze query plans:**
   ```sql
   EXPLAIN ANALYZE SELECT * FROM products WHERE product_name ILIKE '%laptop%';
   ```

3. **Optimize connection pool:**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 10
   ```

#### Problem: High memory usage

**Solutions:**

1. **Adjust JVM settings:**
   ```bash
   java -Xms512m -Xmx2048m -jar application.jar
   ```

2. **Enable pagination:**
   - Always use pagination for large result sets
   - Default page size: 10-50 items

### 7. CORS Issues

#### Problem: "CORS policy blocked" error in browser

**Solutions:**

1. **Update allowed origins in WebConfig:**
   ```java
   .allowedOrigins("http://localhost:3000", "http://your-frontend-url")
   ```

2. **Check request headers:**
   - Ensure proper Content-Type header
   - Include Authorization header for protected endpoints

### 8. Validation Errors

#### Problem: "Validation failed" with unclear errors

**Solutions:**

1. **Check request payload:**
   - Ensure all required fields are present
   - Verify data types match DTO definitions
   - Check field length constraints

2. **Review validation annotations:**
   ```java
   @NotBlank(message = "Username is required")
   @Size(min = 3, max = 50)
   private String username;
   ```

### 9. Transaction Issues

#### Problem: "Transaction rolled back" or "Deadlock detected"

**Solutions:**

1. **Check transaction boundaries:**
   - Ensure @Transactional is properly placed
   - Avoid long-running transactions

2. **Review locking strategy:**
   ```java
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   ```

3. **Increase transaction timeout:**
   ```yaml
   spring:
     transaction:
       default-timeout: 30
   ```

## Preventive Measures

### 1. Regular Maintenance

```sql
-- Vacuum database
VACUUM ANALYZE;

-- Reindex tables
REINDEX DATABASE ecommerce_db;

-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### 2. Monitoring

1. **Enable actuator endpoints:**
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info,metrics
   ```

2. **Check application health:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **Monitor database connections:**
   ```sql
   SELECT * FROM pg_stat_activity WHERE datname = 'ecommerce_db';
   ```

### 3. Logging

1. **Enable debug logging:**
   ```yaml
   logging:
     level:
       com.ecommerce: DEBUG
       org.springframework.security: DEBUG
   ```

2. **Review logs regularly:**
   ```bash
   tail -f logs/spring-boot-application.log | grep ERROR
   ```

### 4. Backup Strategy

```bash
# Daily backup
pg_dump -U postgres ecommerce_db > backup_$(date +%Y%m%d).sql

# Automated backup script
#!/bin/bash
BACKUP_DIR="/path/to/backups"
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -U postgres ecommerce_db | gzip > $BACKUP_DIR/backup_$DATE.sql.gz
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete
```

## Getting Help

### 1. Check Logs

```bash
# Application logs
tail -f logs/spring-boot-application.log

# PostgreSQL logs
tail -f /var/log/postgresql/postgresql-15-main.log
```

### 2. Enable Verbose Logging

```yaml
logging:
  level:
    root: INFO
    com.ecommerce: DEBUG
    org.springframework: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 3. Contact Support

- Create an issue on GitHub
- Email: support@example.com
- Include:
  - Error message
  - Stack trace
  - Steps to reproduce
  - Environment details (OS, Java version, PostgreSQL version)

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT.io](https://jwt.io/) - JWT debugger
- [Flyway Documentation](https://flywaydb.org/documentation/)

## Emergency Procedures

### Database Corruption

1. Stop application
2. Restore from latest backup
3. Verify data integrity
4. Restart application

### Security Breach

1. Immediately change JWT secret
2. Force logout all users (invalidate all tokens)
3. Review access logs
4. Update passwords
5. Audit code for vulnerabilities

### Data Loss

1. Stop all write operations
2. Restore from backup
3. Apply transaction logs if available
4. Verify data consistency
5. Resume operations