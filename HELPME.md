# How to Run the Project

This project is fully containerized using Docker, providing a consistent and easy-to-manage development environment. The only prerequisites are **Docker** and **Docker Compose**.

## ⚠️ Environment Notice

The configuration provided in the `docker-compose.yaml` file is intended for **local development and testing purposes only**. It is not configured for a production environment.

---

## 🚀 Running the Application

1.  **Clone the repository.**

2.  **Setup Environment Variables**: Copy the `env.example` file to a new file named `.env` and fill in the required values.

3.  **Start All Services**:
    ```bash
    docker compose up --build -d
    ```
    This command starts the API, PostgreSQL, and Redis containers. On the first run, it will automatically create the necessary database user for the API.

---

## ⚙️ First-Time Setup: Create the Owner User

After the application has started for the first time and the database tables have been created by Liquibase, you need to manually create the system's `OWNER` user. This step only needs to be performed once.

1.  **Wait for Migrations**: Ensure the API has started and Liquibase has created the database tables. You can check the logs:
    ```bash
    docker compose logs -f financial_reminder_api
    ```
    *(Press `Ctrl + C` to exit)*

2.  **Prepare the SQL**:
    * Open the file: `docker/scripts/db/create-owner-user.sql`.
    * Replace the placeholder values (e.g., `'APPLICATION_OWNER_EMAIL_VALUE'`) with the actual details.
    * **Important**: You must use a **Bcrypt hash** for the password, not plain text.

3.  **Execute the SQL**:
    * Connect to the database at `localhost:5432` using a client like DBeaver or DataGrip. Use the `API_DB_USER` credentials from your `.env` file.
    * Run the edited SQL script in your client.

---


## 🛠️ Managing Services

* **Accessing Services:**
  * **API:** `http://localhost:8080`
  * **Swagger UI:** `http://localhost:8080/swagger-ui.html`
  * **Database:** `localhost:5432`
* **Starting Services Individually:**
  * **Infrastructure only:** 
    ```bash
    docker compose up -d financial_management_db financial_management_redis
    ```
  * **Application only:**
    ```bash
    docker compose up -d --build financial_reminder_api
    ```
* **Stopping All Services:**
    ```bash
    docker compose down
    ```
* **Stopping and Deleting All Data (Hard Reset):**
    ```bash
    docker compose down -v
    ```

---

## 🔎 Troubleshooting Common Issues

Encountered a problem while trying to run the project? Here are some of the most common causes and their solutions.

### Problem: `FATAL: password authentication failed`  for a user

* **Cause:** You are trying to connect with a database client (like DBeaver), but the connection fails, even though you are sure the password in your `.env` file is correct. This is often caused by special characters (like `$` `&` `?` `@` `#`) in your password that are being misinterpreted by the database client or its JDBC driver.
* **Solution:**
    1.  **The simplest fix:** Change the problematic password in your `.env` file to a new, strong password that avoids these specific special characters.
    2.  **Crucial Step:** After changing the password, you must do a hard reset of your environment so the database user is recreated with the new, correct password. Run `docker compose down -v` and then `docker compose up --build -d`.

### Problem: "Port is already allocated" Error

* **Cause:** You already have another service (perhaps another database or API) running on your machine on one of the ports that `docker-compose` needs (8080, 5432, or 6379).
* **Solution:**
    1.  Stop the service that is using the conflicting port.
    2.  **OR**, change the port in the `docker-compose.yaml` file. For example, to run the API on port 8081, change the `ports` line for the `financial_reminder_api` service:
        ```yaml
        ports:
          - "8081:8080" # Now access it at localhost:8081
        ```

### Problem: The API Fails to Start or is in a "restarting" loop

* **Cause:** This usually indicates a configuration error. The most common causes are a missing `.env` file or incorrect credentials, preventing the API from connecting to the database or Redis.
* **Solution:**
    1.  Check that your `.env` file exists in the project root and that all variables are filled in correctly.
    2.  Check the API container's logs to find the specific error. Run the following command and look for error messages (e.g., `Connection refused`):
        ```bash
        docker-compose logs -f financial_reminder_api
        ```

### Problem: "Cannot connect to the Docker daemon"

* **Cause:** The Docker service is not running on your machine.
* **Solution:** Start the Docker Desktop application (or the Docker service on Linux).

### Problem: Everything seems broken or data is corrupted

* **Cause:** Sometimes, especially during development, Docker volumes can retain an old or corrupted state.
* **Solution (The "Hard Reset"):** This command will stop and remove all containers, networks, AND VOLUMES (data) associated with the project. **Warning: This will delete all data in your local database.**
    ```bash
    docker-compose down -v
    ```
  After running this command, you can start everything again with `docker-compose up --build -d`.
