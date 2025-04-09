// src/main/java/com/example/game/service/GameService.java
package com.example.game.service;

import com.example.game.model.Game;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public Game createGame(int min, int max, int maxAttempts) {
        Game game = new Game(min, max, maxAttempts);
        games.put(game.getId(), game);
        return game;
    }

    public Game getGame(String id) {
        return games.get(id);
    }

    public Game.GuessResult makeGuess(String gameId, int number) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found with ID: " + gameId);
        }
        return game.makeGuess(number);
    }
}
