# Number Guessing Game

A simple web-based number guessing game built with Spring Boot. This application runs on port 3000 and provides both a web interface and a REST API for playing the game.

## Features

- Customizable number range and maximum attempts
- Interactive web interface
- RESTful API endpoints
- Containerized with Docker for easy deployment

## Prerequisites

- Java 17 or later
- Maven 3.6+
- Docker (for containerized deployment)

## Project Structure

```
game-app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── game/
│       │               ├── GameApplication.java
│       │               ├── controller/
│       │               │   ├── GameController.java
│       │               │   └── WebController.java
│       │               ├── model/
│       │               │   └── Game.java
│       │               └── service/
│       │                   └── GameService.java
│       └── resources/
│           ├── templates/
│           │   └── index.html
│           └── application.properties
├── pom.xml
├── Dockerfile
└── README.md
```

## Building and Running Locally

### Without Docker

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/number-guessing-game.git
   cd number-guessing-game
   ```

2. Build the application
   ```bash
   mvn clean package
   ```

3. Run the application
   ```bash
   java -jar target/game-0.0.1-SNAPSHOT.jar
   ```

4. Access the application at http://localhost:3000

### With Docker

1. Build the Docker image
   ```bash
   docker build -t number-guessing-game .
   ```

2. Run the Docker container
   ```bash
   docker run -p 3000:3000 --name game-container number-guessing-game
   ```

3. Access the application at http://localhost:3000

## API Endpoints

### Create a new game
```
POST /api/game/new
```

Query Parameters:
- `min` (optional, default: 1): Minimum number in the range
- `max` (optional, default: 100): Maximum number in the range
- `maxAttempts` (optional, default: 10): Maximum number of attempts allowed

Response:
```json
{
  "gameId": "b7e89f4a-8c9d-4e5f-9a9b-0c1d2e3f4a5b",
  "maxAttempts": 10,
  "range": "The number is between 1 and 100"
}
```

### Get game status
```
GET /api/game/{gameId}
```

Response:
```json
{
  "gameId": "b7e89f4a-8c9d-4e5f-9a9b-0c1d2e3f4a5b",
  "completed": false,
  "attemptsRemaining": 8,
  "guessCount": 2
}
```

### Make a guess
```
POST /api/game/{gameId}/guess
```

Request Body:
```json
{
  "guess": 50
}
```

Response:
```json
{
  "message": "Too low! Try a higher number",
  "correct": false,
  "gameOver": false,
  "remainingAttempts": 7
}
```

## Docker Commands

### Build the image
```bash
docker build -t number-guessing-game .
```

### Run the container
```bash
docker run -p 3000:3000 --name game-container number-guessing-game
```

### Stop the container
```bash
docker stop game-container
```

### Remove the container
```bash
docker rm game-container
```

### View container logs
```bash
docker logs game-container
```

## Deploying to Nexus Repository

To deploy the application to your Nexus repository:

```bash
mvn deploy
```

Note: Ensure you have configured your Maven settings with appropriate credentials.

## License

[MIT](LICENSE)
