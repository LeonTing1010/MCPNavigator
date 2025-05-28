# NLWeb-Spring-AI Project

This project enables browser automation using natural language commands. It integrates a Spring Boot application with Spring AI, Microsoft's NLWeb for natural language processing, and Playwright-MCP for browser control. Users can issue commands like "navigate to example.com and click the login button" to interact with web pages programmatically.

## Architecture Overview

The system consists of three main services designed to run together using Docker Compose:

1.  **Spring AI Application (`spring-app`):**
    *   The central orchestrator built with Java and Spring Boot.
    *   Exposes a REST API (`/api/v1/process-nl`) to accept natural language queries from the user.
    *   Communicates with the NLWeb service to translate the natural language query into a structured MCP (Model Context Protocol) command.
    *   Sends the MCP command to the Playwright-MCP service, which executes the browser action.
    *   Streams responses from Playwright-MCP back to the user as Server-Sent Events (SSE).

2.  **NLWeb Service (`nlweb`):**
    *   A Python application based on the [microsoft/NLWeb](https://github.com/microsoft/NLWeb) project.
    *   It takes a natural language query and uses a Large Language Model (LLM), such as OpenAI's GPT models, to parse it into an MCP command.
    *   Requires an API key for the configured LLM provider (e.g., OpenAI).
    *   This service is containerized using `docker/nlweb/Dockerfile.nlweb`.

3.  **Playwright-MCP Service (`playwright-mcp`):**
    *   A Node.js application based on the [microsoft/playwright-mcp](https://github.com/microsoft/playwright-mcp) project.
    *   It listens for MCP commands.
    *   Uses Playwright to execute these commands in a web browser (e.g., navigate, click, type, snapshot).
    *   Streams back responses and browser state changes (like snapshots) as SSE.

## Prerequisites

*   **Git:** For cloning the repository.
*   **Docker Desktop:** Or Docker Engine with Docker Compose installed and running.

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository_url_placeholder>
cd nlweb-spring-ai
```
*(Replace `<repository_url_placeholder>` with the actual URL of this repository if you are cloning it from a remote source.)*

### 2. Configure API Key

The NLWeb service requires an API key for an LLM (e.g., OpenAI) to parse natural language.

*   Copy the `.env.example` file to a new file named `.env` in the project root:
    ```bash
    cp .env.example .env
    ```
*   Open the `.env` file with a text editor and replace `YOUR_OPENAI_API_KEY_HERE` with your actual OpenAI API key.
    ```env
    # Example content for .env file
    OPENAI_API_KEY="sk-yourActualOpenAiApiKeyGoesHere"
    ```

### 3. Build and Run with Docker Compose

From the project root directory (`nlweb-spring-ai`), run:

```bash
docker-compose up --build
```

*   This command will:
    *   Build the Docker image for the Spring AI application (`spring-app`).
    *   Build the Docker image for the NLWeb service (`nlweb`), which includes cloning the NLWeb repository and installing its dependencies.
    *   Pull the official Docker image for the Playwright-MCP service (`playwright-mcp`).
    *   Start all three services and connect them on a shared Docker network.
*   The initial build process, especially for the `nlweb` service (which clones a repo and installs Python dependencies), might take some time. Subsequent starts using `docker-compose up` (without `--build` if no changes were made) will be much faster.

### 4. Stopping the Services

*   To stop all running services, press `Ctrl+C` in the terminal where `docker-compose up` is running.
*   Then, to remove the containers and network, run:
    ```bash
    docker-compose down
    ```

## Using the Application

Once the services are running, you can interact with the Spring AI application.

*   **API Endpoint:** `POST /api/v1/process-nl`
*   **Request Body:** A JSON object with a single field `query` containing the natural language command.
    ```json
    {
      "query": "navigate to example.com and take a snapshot"
    }
    ```
*   **Response:** The endpoint returns a stream of Server-Sent Events (`text/event-stream`). Each event is a JSON object representing an `McpResponse` from the Playwright-MCP service. This stream will provide real-time updates as browser actions are performed.

### Example with `curl`

You can use `curl` to send a request to the application. The `localhost:8080` address maps to the `spring-app` service.

```bash
curl -N -X POST -H "Content-Type: application/json" \
     -d '{"query": "navigate to playwright.dev and take a snapshot"}' \
     http://localhost:8080/api/v1/process-nl
```

*   The `-N` flag for `curl` disables buffering, which is useful for observing the SSE stream.
*   You will see a series of JSON objects streamed back, representing acknowledgments, snapshots, or errors from the browser automation process.

## Project Structure

A brief overview of the key files and directories:

*   `nlweb-spring-ai/`
    *   `src/main/java/`: Contains the Java source code for the Spring Boot application (`spring-app`).
        *   `controller/`: REST controllers.
        *   `service/`: Business logic services (Orchestration, NLWeb, MCP).
        *   `client/`: Clients for external services (NLWeb, Playwright-MCP).
        *   `mcp/model/`: POJOs for Playwright-MCP communication.
        *   `model/`: POJOs for NLWeb communication and internal DTOs.
        *   `config/`: Spring configuration classes.
    *   `src/main/resources/`:
        *   `application.properties`: Spring Boot application configuration.
    *   `src/test/java/`: Unit and integration tests for the Spring Boot application.
    *   `docker/nlweb/`: Contains artifacts for building the `nlweb` Docker image.
        *   `Dockerfile.nlweb`: Dockerfile for the NLWeb Python service.
    *   `Dockerfile`: Dockerfile for the `spring-app` (this Spring AI application).
    *   `docker-compose.yml`: Docker Compose file to orchestrate all services.
    *   `.env.example`: Template for environment variables (primarily `OPENAI_API_KEY`).
    *   `pom.xml`: Maven project file for the Spring AI application.
    *   `README.md`: This file.
