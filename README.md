# Charity Management Application

A comprehensive web application for managing charity organizations, actions, donations, and participations.

## Features

- User management with email/password and OAuth2 authentication
- Organization registration and management
- Charity action creation and management
- Donation processing with payment integration
- Participation management for charity actions
- Admin dashboard for approvals and management
- Multilingual support (English, French, Arabic)
- Responsive design for all devices
- Email notifications
- File upload functionality

## Technology Stack

- Java 17
- Spring Boot 3.1.0
- Spring Security with OAuth2
- Spring Data JPA
- Thymeleaf templating engine
- Bootstrap 5.3.0
- MySQL / PostgreSQL database support
- Maven build system

## Setup Instructions

### Prerequisites

- Java 17 or higher
- MySQL 8.0 or PostgreSQL 13+
- Maven 3.8+

### Application Configuration

1. Clone the repository or extract the provided ZIP file
2. Configure database connection in `src/main/resources/application.properties` or use the existing configuration
3. For email notifications, configure SMTP settings in `application.properties`
4. For OAuth2, configure your Google client ID and secret in `application.properties`

### Running the Application

1. Navigate to the project root directory
2. Build the application: `mvn clean package`
3. Run the application: `java -jar target/charity-management-app-0.0.1-SNAPSHOT.jar`
4. Access the application at: `http://localhost:8080`


## Application Structure

- `src/main/java/com/charity/management/` - Java source files
  - `config/` - Configuration classes
  - `controller/` - Web controllers
  - `entity/` - JPA entities
  - `repository/` - Data repositories
  - `service/` - Business logic
  - `security/` - Security configuration
  - `util/` - Utility classes
- `src/main/resources/` - Application resources
  - `templates/` - Thymeleaf templates
  - `static/` - Static resources (CSS, JS, images)
  - `i18n/` - Internationalization files
