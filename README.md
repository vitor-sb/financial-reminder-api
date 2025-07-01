# Financial Management API

A RESTful API for managing payment reminders and transactions. Built with Spring Boot and following the principles of Hexagonal Architecture.

## Tech Stack

-   **Java 17**
-   **Spring Boot**
    -   **Spring Web**
    -   **Spring Data JPA** & **Hibernate**
    -   **Spring Security**
-   **PostgreSQL**
-   **Redis**
-   **Docker**
-   **JWT (Java Web Token)**

## Prerequisites

-   **JDK 17**
-   **Maven 3.x**
-   **Docker** & **Docker Compose**

## How to Run

1.  Clone the repository.
2.  Create and configure the `.env` file from `.env.example` in the project root.
3.  Start the infrastructure with Docker:
    ```bash
    docker-compose up -d
    ```
4. Run the application:
   1. **Option 1: Via Terminal (Linux & macOS)**
      1.  In the terminal, navigate to the project root.
      2.  Load the environment variables from the `.env` file:
          ```bash
          export $(grep -v '^#' .env | xargs)
          ```
      3.  Run the application:
          ```bash
          ./mvnw spring-boot:run
          ```

   2. **Option 2: Via IntelliJ IDEA (Any OS)**
      1.  Open the project in the IntelliJ IDE.
      2.  In the upper right corner, click on **"Edit Configurations..."**.
      3.  Find the **"Environment variables"** field and click the icon at the end of the line to open the file selection window.
      4.  Select the previously configured `.env` file.
      5.  Click **OK**, then **Apply** and **OK** to save the configuration.
      6.  You can now run or debug the application using the Play/Debug buttons at the top of the IDE.