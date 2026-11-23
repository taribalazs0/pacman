package hu.bme.pacman.system;

import java.time.LocalDate;

/**
 * One row of highscores.csv: who scored how much, on what day.
 * Immutable: the fields are final and there are no setters.
 */
public final class HighscoreEntry {
    private final String playerName;
    private final int score;
    private final LocalDate date;

    public HighscoreEntry(String playerName, int score, LocalDate date) {
        this.playerName = playerName;
        this.score = score;
        this.date = date;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public LocalDate getDate() {
        return date;
    }

    /** Same line in the CSV format: PlayerName;Score;Date */
    @Override
    public String toString() {
        return playerName + ";" + score + ";" + date;
    }
}
