# Pac-Man

## 1. Project Overview
Pac-Man is a two-dimensional desktop arcade game developed in Java. The player controls the hero within a labyrinth, aiming to collect all dots while avoiding moving ghosts. The software utilizes the Java Swing framework for its graphical interface, file handling for persistent game state storage, and automated JUnit unit tests to ensure code quality.

## 2. Use-Cases
* **Game Start and Control**: The user can start a new game from the menu and control Pac-Man inside the labyrinth using the WASD keyboard keys.
* **Dot Collection and Victory**: As Pac-Man passes through dots, they disappear and the score increases. When all dots are consumed, the player wins and advances to the next level.
* **Ghost Collision**: If the character collides with a ghost, it loses a life (starting from 3). If the number of lives reaches zero, the game ends, and starting a new game places the player back at the first level.
* **High-Score Saving**: At the end of the game, the system records and saves the achieved score to a CSV file.

## 3. Solution Architecture
* **Technologies Used**: Java, Swing (`JFrame`, `JPanel`, `Graphics` for low-level drawing, `Timer` for the game loop), and JUnit for testing.
* **File Format**: A simple CSV file used for storing the leaderboard.

## 4. Detailed Architecture and Class Structure
* **`GameEngine`**: The central controller of the game. It contains the game loop, updates the game state (positions, collisions), and manages the score and lives.
* **`Labyrinth`**: The class representing the map. It stores the locations of walls, empty paths, and dots using a two-dimensional array.
* **`GameObject` (Abstract Base Class)**: Encapsulates common properties for moving characters (X and Y coordinates, speed, direction).
* **`PacMan` (Child Class)**: Manages the player character's state and specific movement logic.
* **`Ghost` (Child Class)**: Manages the ghosts' state and movement logic.