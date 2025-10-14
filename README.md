# Fitness Microservice Application

A comprehensive fitness tracking microservice application built with Spring Boot and Spring Cloud, featuring user management, activity tracking, and AI-powered recommendations.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │   Config Server │    │  Eureka Server  │
│   Port: 8080    │    │   Port: 8888    │    │   Port: 8761    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
    ┌────────────────────────────┼────────────────────────────┐
    │                            │                            │
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│User Service │      │Activity Svc │      │ AI Service  │
│Port: 8081   │      │Port: 8082   │      │Port: 8083   │
└─────────────┘      └─────────────┘      └─────────────┘
    │                    │                    │
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│ PostgreSQL  │      │   Kafka     │      │   MongoDB   │
│Port: 5432   │      │Port: 9092   │      │Port: 27017  │
└─────────────┘      └─────────────┘      └─────────────┘
```

## 🚀 Services

### Core Services

1. **API Gateway** (Port: 8080)
   - Entry point for all client requests
   - Routes requests to appropriate microservices
   - Load balancing with Eureka service discovery
   - Security integration with Keycloak

2. **User Service** (Port: 8081)
   - User registration and authentication
   - User profile management
   - PostgreSQL database integration
   - RESTful APIs for user operations

3. **Activity Service** (Port: 8082)
   - Activity tracking and management
   - Integration with User Service for validation
   - Kafka messaging for activity events
   - RESTful APIs for activity operations

4. **AI Service** (Port: 8083)
   - AI-powered fitness recommendations
   - Google Gemini API integration
   - MongoDB for recommendation data
   - Kafka consumer for activity data

### Infrastructure Services

5. **Eureka Discovery Server** (Port: 8761)
   - Service registration and discovery
   - Health monitoring
   - Load balancing support

6. **Config Server** (Port: 8888)
   - Centralized configuration management
   - Environment-specific configurations
   - Git-based configuration storage

## 🛠️ Technology Stack

### Backend Framework
- **Java 21**
- **Spring Boot 3.5.x**
- **Spring Cloud 2023.x**
- **Maven** for dependency management

### Databases
- **PostgreSQL 15** - User Service data storage
- **MongoDB** - AI Service recommendations and analytics

### Messaging
- **Apache Kafka** - Event-driven communication between services

### Security
- **Keycloak** - Identity and Access Management (IAM)
- **OAuth 2.0 / JWT** - Authentication and authorization

### Service Discovery & Configuration
- **Netflix Eureka** - Service discovery
- **Spring Cloud Config** - Centralized configuration

### AI Integration
- **Google Gemini API** - AI-powered recommendations

### DevOps & Deployment
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

### Required Software
- **Java 21** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- **Git**

### Required Services
- **PostgreSQL 15**
- **MongoDB**
- **Apache Kafka**
- **Keycloak**

## ⚙️ Configuration Setup

### 1. Database Configuration

#### PostgreSQL Setup
```bash
# Create database
createdb fitness-micro-user

# Update connection details in config server:
# configserver/src/main/resources/config/user-service.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fitness-micro-user
    username: postgres
    password: your_password
```

#### MongoDB Setup
```bash
# Start MongoDB
mongod --port 27017

# Database will be auto-created: fitness-ai-db
```

### 2. Kafka Configuration

#### Start Kafka with Docker Compose
```yaml
# docker-compose-kafka.yml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
```

```bash
# Start Kafka
docker-compose -f docker-compose-kafka.yml up -d
```

### 3. Keycloak Configuration

#### Start Keycloak with Docker
```bash
# Start Keycloak
docker run -d \
  --name keycloak \
  -p 8181:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev
```

#### Configure Keycloak Realm
1. Access Keycloak Admin Console: http://localhost:8181
2. Login with admin/admin
3. Create a new realm: `fitness-app`
4. Create clients for each service
5. Configure JWT settings

### 4. Environment Variables

Create environment files for each service:

#### AI Service (.env)
```bash
# aiservice/.env
GEMINI_KEY=your_gemini_api_key
MONGODB_URI=mongodb://localhost:27017/fitness-ai-db
SERVER_PORT=8083
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## 🐳 Docker Configuration

### Complete Docker Compose Setup
```yaml
# docker-compose.yml
version: '3.8'
services:
  # Databases
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: fitness-micro-user
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

  # Message Broker
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"

  # Security
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    ports:
      - "8181:8080"
    command: start-dev

  # Infrastructure Services
  eureka-server:
    build: ./eureka
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  config-server:
    build: ./configserver
    ports:
      - "8888:8888"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  # Business Services
  api-gateway:
    build: ./gateway
    ports:
      - "8080:8080"
    depends_on:
      - eureka-server
      - config-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  user-service:
    build: ./userservice
    ports:
      - "8081:8081"
    depends_on:
      - postgres
      - eureka-server
      - config-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  activity-service:
    build: ./activityservice
    ports:
      - "8082:8082"
    depends_on:
      - kafka
      - eureka-server
      - config-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  ai-service:
    build: ./aiservice
    ports:
      - "8083:8083"
    depends_on:
      - mongodb
      - kafka
      - eureka-server
      - config-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - GEMINI_KEY=${GEMINI_API_KEY}

volumes:
  postgres_data:
  mongodb_data:
```

## 🚀 Running the Application

### Method 1: Local Development

#### 1. Start Infrastructure Services
```bash
# Start databases and Kafka
docker-compose up -d postgres mongodb zookeeper kafka keycloak

# Or start manually:
# PostgreSQL, MongoDB, Kafka, Keycloak as described above
```

#### 2. Start Spring Boot Services (in order)
```bash
# 1. Config Server
cd configserver
./mvnw spring-boot:run

# 2. Eureka Server
cd eureka
./mvnw spring-boot:run

# 3. API Gateway
cd gateway
./mvnw spring-boot:run

# 4. User Service
cd userservice
./mvnw spring-boot:run

# 5. Activity Service
cd activityservice
./mvnw spring-boot:run

# 6. AI Service
cd aiservice
./mvnw spring-boot:run
```

### Method 2: Full Docker Deployment
```bash
# Build and start all services
docker-compose up --build -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

## 🔗 API Endpoints

All requests go through the API Gateway at `http://localhost:8080`

### User Service
- `POST /api/users/register` - Register new user
- `GET /api/users/{userId}` - Get user profile
- `GET /api/users/{userId}/validate` - Validate user
- `GET /api/users/health` - Health check

### Activity Service
- `POST /api/activities` - Create activity
- `GET /api/activities/user/{userId}` - Get user activities
- `PUT /api/activities/{activityId}` - Update activity
- `DELETE /api/activities/{activityId}` - Delete activity
- `GET /api/activities/health` - Health check

### AI Service
- `GET /api/recommendations/user/{userId}` - Get user recommendations
- `GET /api/recommendations/activity/{activityId}` - Get activity recommendations

## 🔍 Monitoring & Health Checks

### Service Discovery
- Eureka Dashboard: http://localhost:8761

### Configuration Management
- Config Server: http://localhost:8888/{service-name}/default

### Health Endpoints
- User Service: http://localhost:8080/api/users/health
- Activity Service: http://localhost:8080/api/activities/health
- AI Service: http://localhost:8080/api/recommendations/health

## 🔧 Development

### Adding New Services
1. Create new Spring Boot module
2. Add Eureka client dependency
3. Configure in `application.yml`
4. Add routes in API Gateway configuration
5. Add service-specific configuration in Config Server

### Environment-Specific Configurations
- `application.yml` - Default configuration
- `application-dev.yml` - Development environment
- `application-docker.yml` - Docker environment
- `application-prod.yml` - Production environment

## 🛡️ Security

### Keycloak Integration
- Realm: `fitness-app`
- Client: Each microservice has its own client
- JWT Token validation at API Gateway
- Role-based access control

### Security Headers
- CORS configuration
- Content Security Policy
- Rate limiting (can be added)

## 📊 Data Flow

1. **User Registration**: User → API Gateway → User Service → PostgreSQL
2. **Activity Creation**: User → API Gateway → Activity Service → Kafka → AI Service
3. **AI Recommendations**: AI Service processes activity data → Gemini API → MongoDB
4. **Service Discovery**: All services register with Eureka for load balancing

## 🐛 Troubleshooting

### Common Issues

1. **Service not registering with Eureka**
   - Check Eureka server is running
   - Verify `eureka.client.service-url.defaultZone` configuration

2. **Configuration not loading**
   - Ensure Config Server is running before other services
   - Check `spring.config.import` settings

3. **Database connection issues**
   - Verify database is running and accessible
   - Check connection strings and credentials

4. **Kafka connection issues**
   - Ensure Kafka and Zookeeper are running
   - Check `spring.kafka.bootstrap-servers` configuration

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

For support and questions, please create an issue in the GitHub repository.