// src/main/java/com/example/game/controller/GameController.java
package com.example.game.controller;

import com.example.game.model.Game;
import com.example.game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, Object>> createGame(
            @RequestParam(defaultValue = "1") int min,
            @RequestParam(defaultValue = "100") int max,
            @RequestParam(defaultValue = "10") int maxAttempts) {
        
        Game game = gameService.createGame(min, max, maxAttempts);
        
        Map<String, Object> response = new HashMap<>();
        response.put("gameId", game.getId());
        response.put("maxAttempts", game.getMaxAttempts());
        response.put("range", "The number is between " + min + " and " + max);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Map<String, Object>> getGameStatus(@PathVariable String gameId) {
        Game game = gameService.getGame(gameId);
        
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("gameId", game.getId());
        response.put("completed", game.isCompleted());
        response.put("attemptsRemaining", game.getRemainingAttempts());
        response.put("guessCount", game.getGuesses().size());
        
        if (game.isCompleted()) {
            response.put("targetNumber", game.getTargetNumber());
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{gameId}/guess")
    public ResponseEntity<Map<String, Object>> makeGuess(
            @PathVariable String gameId,
            @RequestBody Map<String, Integer> payload) {
        
        if (!payload.containsKey("guess")) {
            return ResponseEntity.badRequest().build();
        }
        
        int guess = payload.get("guess");
        
        try {
            Game.GuessResult result = gameService.makeGuess(gameId, guess);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", result.getMessage());
            response.put("correct", result.isCorrect());
            response.put("gameOver", result.isGameOver());
            response.put("remainingAttempts", result.getRemainingAttempts());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
