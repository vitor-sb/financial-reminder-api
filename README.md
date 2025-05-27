# Finance Management API

API for financial and account management.

## Overview

This project is a RESTful API built with Spring Boot to assist in personal finance management, including user control, authentication, and payment reminders.

## Prerequisites (What you need to install)

* **Git**
* **JDK 17 (or higher)**
* **Apache Maven 3.6.x (or higher)**
* **Docker and Docker Compose**
* **Optional:**
    * An SQL client (e.g., DBeaver, pgAdmin).
    * An API client (e.g., Postman, Insomnia).

## Technologies Used

* **Java:** 
* **Spring Boot**
* **Maven**
* **Spring Security**
* **JWT (Java JWT)**
* **JPA (Hibernate)**
* **PostgreSQL**
* **H2 Database** (For unit test)
* **Liquibase** 
* **Redis**
* **Lombok**
* **Docker & Docker Compose** 

## Environment Setup

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd finance-management-api
    ```

2.  **Environment Variables File (`.env`):**
    Copy the content from the `.env.example` file (detailed in the previous response) to a new file named `.env` in the project root (`finance-management-api/.env`). **It is crucial to adjust the values in this file** according to your local or production settings.

3.  **Configuration and Execution with Docker Compose:**
    The project uses Docker Compose to orchestrate infrastructure services (PostgreSQL and Redis). The main configuration file for this is `docker/docker-compose.yaml`.

    * **Defined Services:**
        * `financial_management_db`: PostgreSQL container.
            * Uses the `postgres:17-alpine` image.
            * Exposes port `5432`.
            * The environment variables `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` are used to create the initial database and the PostgreSQL superuser. These values should be defined in your `.env` file as `POSTGRES_DB_NAME`, `ROOT_DB_USER`, and `ROOT_DB_PASSWORD`, respectively.
        * `financial_management_redis`: Redis container.
            * Uses the `redis:7.4.3-alpine` image.
            * Exposes port `6379`.
            * The Redis password is configured via the `REDIS_PASSWORD` variable in your `.env` file.

    * **To start the services:**
      Ensure Docker is running. In the project root, execute:
        ```bash
        docker-compose up -d
        ```
      This will start the PostgreSQL and Redis containers in the background.

4.  **Creating the Application User in the Database:**
    The `docker-compose.yaml` sets up a superuser for PostgreSQL (defined by `ROOT_DB_USER` and `ROOT_DB_PASSWORD`). For security reasons, the Spring Boot application should use a user with more restricted permissions (`APP_DB_USER` and `APP_DB_PASSWORD`).

    The script `docker/scripts/db/create-app-user.sql` is responsible for creating this specific application user and granting the necessary permissions. You will need to execute it manually after the Docker containers are active:

    * Find the name or ID of your PostgreSQL container (usually `financial_management_postgres_container`): `docker ps`
    * Execute the SQL script. Ensure that the variables `ROOT_DB_USER`, `APP_DB_USER`, and `APP_DB_PASSWORD` in the command below (or in your environment) match those defined in your `.env` file:
        ```bash
        # Load variables from .env into the current shell if needed, or substitute directly
        # Example: export $(grep -v '^#' .env | xargs)
        
        docker exec -i financial_management_postgres_container psql -U ${ROOT_DB_USER} -d ${POSTGRES_DB_NAME} \
          -v APP_DB_USER="${APP_DB_USER}" \
          -v APP_DB_PASSWORD="${APP_DB_PASSWORD}" \
          < docker/scripts/db/create-app-user.sql
        ```
      You will be prompted for the `ROOT_DB_USER` password.
      *Note: The SQL script uses the variables `APP_DB_USER` and `APP_DB_PASSWORD` internally. The command above demonstrates how to pass them to `psql` if the script does not read them directly from the container's environment.*

    *Alternatively, use an SQL client to connect to the PostgreSQL database (port 5432) with the `ROOT_DB_USER` credentials, select the `finance-management` database, and execute the content of the `docker/scripts/db/create-app-user.sql` file (replacing the placeholders `${APP_DB_USER}` and `${APP_DB_PASSWORD}` with your `.env` values before execution).*

## Running the Application

After setting up the environment and infrastructure services, you can run the Spring Boot application:

```bash
mvn spring-boot:run