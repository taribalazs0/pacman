package hu.bme.pacman.system;

import java.time.LocalDate;

/**
 * One row of highscores.csv: who scored how much, on what day.
 *
 * ARCHITECTURE DECISION: this lives in hu.bme.pacman.system, not in hu.bme.pacman.game.
 * It is not part of the strict MVC game model (GameEngine, PacMan, Ghost, ...); it only
 * exists to move data between LevelManager's file I/O and the UI's LeaderboardPanel.
 * A record is enough, because it has no behaviour, only three plain fields.
 */
public record HighscoreEntry(String playerName, int score, LocalDate date) {
}
