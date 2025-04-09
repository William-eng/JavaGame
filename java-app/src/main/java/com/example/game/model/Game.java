// src/main/java/com/example/game/model/Game.java
package com.example.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Game {
    private final String id;
    private final int targetNumber;
    private final List<Guess> guesses;
    private boolean completed;
    private int maxAttempts;
    private int remainingAttempts;

    public Game(int min, int max, int maxAttempts) {
        this.id = UUID.randomUUID().toString();
        Random random = new Random();
        this.targetNumber = random.nextInt(max - min + 1) + min;
        this.guesses = new ArrayList<>();
        this.completed = false;
        this.maxAttempts = maxAttempts;
        this.remainingAttempts = maxAttempts;
    }

    public String getId() {
        return id;
    }

    public int getTargetNumber() {
        return targetNumber;
    }

    public List<Guess> getGuesses() {
        return guesses;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public GuessResult makeGuess(int number) {
        if (completed) {
            return new GuessResult("Game already completed", false, true, remainingAttempts);
        }

        remainingAttempts--;
        Guess guess = new Guess(number);
        guesses.add(guess);

        if (number == targetNumber) {
            completed = true;
            return new GuessResult("Congratulations! You guessed the number!", true, true, remainingAttempts);
        } else if (remainingAttempts <= 0) {
            completed = true;
            return new GuessResult("Game over! The number was " + targetNumber, false, true, remainingAttempts);
        } else if (number < targetNumber) {
            return new GuessResult("Too low! Try a higher number", false, false, remainingAttempts);
        } else {
            return new GuessResult("Too high! Try a lower number", false, false, remainingAttempts);
        }
    }

    public static class Guess {
        private final int value;
        private final long timestamp;

        public Guess(int value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public int getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public static class GuessResult {
        private final String message;
        private final boolean correct;
        private final boolean gameOver;
        private final int remainingAttempts;

        public GuessResult(String message, boolean correct, boolean gameOver, int remainingAttempts) {
            this.message = message;
            this.correct = correct;
            this.gameOver = gameOver;
            this.remainingAttempts = remainingAttempts;
        }

        public String getMessage() {
            return message;
        }

        public boolean isCorrect() {
            return correct;
        }

        public boolean isGameOver() {
            return gameOver;
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }
    }
}
