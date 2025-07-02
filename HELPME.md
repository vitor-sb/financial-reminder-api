# How to Run the Project

This project is fully containerized using Docker. The only prerequisites are **Docker** and **Docker Compose**.

## Running the Application

1.  **Clone the repository.**
2.  **Create the `.env` file**: Copy the content from `.env.example` to a new file named `.env` and fill in the required values. Ensure all variables, including `ROOT_DB_USER` and `APPLICATION_OWNER_EMAIL`, are set.
3.  **Start all services**: Navigate to the project's root directory in your terminal and run the following command:

    ```bash
    docker-compose up --build -d
    ```
    * `--build`: This flag ensures that Docker rebuilds your application's image if you have made any code changes.
    * `-d`: This flag runs the containers in "detached" mode (in the background).

That's it! The command will start all services. On the very first run, it will automatically initialize the database by executing the scripts located in `docker/scripts/db`.

---

## Accessing the Services

* **API**: The API will be available at `http://localhost:8080`.
* **Database (PostgreSQL)**: Can be accessed on `localhost:5432`.
* **Redis**: Can be accessed on `localhost:6379`.

---

## Accessing the API Documentation (Swagger UI)

Once the application is running, you can access the interactive API documentation (Swagger UI) in your browser at the following URL:

[**http://localhost:8080/swagger-ui.html**](http://localhost:8080/swagger-ui.html)

On this page, you can view all available endpoints, see their request/response models, and execute requests directly from the browser, which is excellent for testing.

---

## Troubleshooting Common Issues

Encountered a problem while trying to run the project? Here are some of the most common causes and their solutions.

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

---

## Using Profiles

This `docker-compose` setup uses profiles to allow you to start services independently.

* **To start ONLY the infrastructure (Database and Redis):**
    ```bash
    docker-compose up -d --profile infra
    ```

* **To start ONLY the application (assuming the infrastructure is already running):**
    ```bash
    docker-compose up -d --profile app
    ```
