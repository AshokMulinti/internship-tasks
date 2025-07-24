# internship-tasks
 Tasks during internship


# Auth API - Spring Boot Internship Project

This is a backend authentication system built using Spring Boot. It supports secure user login and signup, JWT-based token authentication, file uploads (CSV and Excel), and protects API endpoints using Spring Security.

#**Features**
- User Signup and Login
- JWT Token Generation and Validation
- Secured Endpoints with JWT Authentication
- File Upload (CSV and Excel)
- API Documentation using Swagger UI
- MySQL Database Integration

**#Tech Stack**
- Java 21
- Spring Boot
- Spring Security
- JWT (JSON Web Token)
- MySQL
- Gradle
- Swagger (springdoc-openapi)
- IntelliJ IDEA

**#Project Structure**
src/
├── controller/
├── dto/
├── model/
├── exceptions/
├── repository/
├── security/
├── service/
├── utils/
└── AuthApiApplication.java

### application.properties
properties
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# Swagger config 
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true

**Secured Endpoints**
These endpoints require a valid JWT token:
GET /api/dashboard
GET /api/users{id}
POST /api/upload-csv
POST /api/upload-excel
GET /api/dashboard
PUT /api/users{id}
DELETE /api/users{id}

**Public endpoints**
These do not require a JWT token:
POST /api/signup
POST /api/login
Swagger UI: /swagger-ui/**, /v3/api-docs/**


