package com.example.pacman_game.controller;

import com.example.pacman_game.models.PacMan;
import com.example.pacman_game.models.Ghost;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import com.example.pacman_game.models.Maze;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class GameController
{
    @FXML
    public Button startBTN;

    @FXML
    public Button exitBTN;

    @FXML
    private Label titleLabel;

    @FXML
    private HBox buttonContainer;

    @FXML
    private VBox mainContainer;

    @FXML
    private Pane mazePane;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label livesLabel;

    private Maze maze;
    private PacMan pacman;
    private List<Ghost> ghosts;
    private AnimationTimer gameLoop;
    private boolean gameRunning = false;
    private List<Circle> pelletVisuals;
    private List<Circle> powerPelletVisuals;
    private boolean wasPoweredUp = false;
    private int lives = 3;
    private boolean isPaused = false;
    private int pauseTimer = 0;
    private static final int RESPAWN_DELAY = 120;

    @FXML
    public void initialize()
    {
        this.maze = new Maze();
        this.pelletVisuals = new ArrayList<>();
        this.powerPelletVisuals = new ArrayList<>();

        double startX = 9 * maze.getCellSize() + (double) maze.getCellSize() / 2;
        double startY = 14 * maze.getCellSize() + (double) maze.getCellSize() / 2;
        this.pacman = new PacMan(startX, startY, maze);

        this.ghosts = new ArrayList<>();
        initializeGhosts();

        drawMazeStatic();
        mazePane.getChildren().add(pacman.getVisual());

        for (Ghost ghost : ghosts)
        {
            mazePane.getChildren().add(ghost.getVisual());
        }
    }

    private void initializeGhosts()
    {
        double cellSize = maze.getCellSize();

        // Spawn coordinates for the ghosts
        int[][] ghostStarts = {
                {9, 9},
                {10, 9},
                {8, 9},
                {9, 8}
        };

        // Colors for each ghosts
        Color[] ghostColors = {Color.RED, Color.PINK, Color.CYAN, Color.ORANGE};

        for (int i = 0; i < ghostColors.length; i++)
        {
            int gridX = ghostStarts[i][0];
            int gridY = ghostStarts[i][1];

            Ghost ghost = new Ghost(
                    gridX * cellSize + cellSize / 2,
                    gridY * cellSize + cellSize / 2,
                    maze,
                    ghostColors[i]
            );
            ghosts.add(ghost);
        }

        for (Ghost ghost : ghosts)
        {
            ghost.setReferences(pacman);
        }
    }

    @FXML
    private void startGame()
    {
        if (!gameRunning)
        {
            gameRunning = true;

            titleLabel.setVisible(false);
            buttonContainer.setVisible(false);

            // Make score and lives visible
            scoreLabel.setVisible(true);
            livesLabel.setVisible(true);
            updateUI();

            mainContainer.setPadding(new javafx.geometry.Insets(0));
            mainContainer.setSpacing(0);
            mainContainer.setStyle("-fx-background-color: black");

            Stage stage = (Stage) startBTN.getScene().getWindow();

            stage.setWidth(585);
            stage.setHeight(780);
            stage.centerOnScreen();

            drawMazeStatic();
            mazePane.getChildren().add(pacman.getVisual());

            for (Ghost ghost : ghosts)
            {
                mazePane.getChildren().add(ghost.getVisual());
            }

            mazePane.setFocusTraversable(true);
            mazePane.requestFocus();
            mazePane.setOnKeyPressed(this::handleKeyPress);

            gameLoop = new AnimationTimer()
            {
                @Override
                public void handle(long now)
                {
                    updateGame();
                }
            };
            gameLoop.start();
        }
    }

    private void handleKeyPress(KeyEvent event)
    {
        KeyCode code = event.getCode();
        switch (code)
        {
            case RIGHT:
            case D:
                pacman.setDirection(0);
                break;
            case DOWN:
            case S:
                pacman.setDirection(1);
                break;
            case LEFT:
            case A:
                pacman.setDirection(2);
                break;
            case UP:
            case W:
                pacman.setDirection(3);
                break;
        }
    }

    @FXML
    private void exitGame()
    {
        if (gameLoop != null)
        {
            gameLoop.stop();
        }
        System.exit(0);
    }

    private void updateGame()
    {
        // Handle respawn pause
        if (isPaused)
        {
            pauseTimer--;
            if (pauseTimer <= 0)
            {
                respawnAfterDeath();
            }
            return; // Don't update anything while paused
        }

        pacman.update();

        if (pacman.isPoweredUp() && !wasPoweredUp)
        {
            for (Ghost ghost : ghosts)
            {
                ghost.setMode(Ghost.GhostMode.FRIGHTENED);
            }
            wasPoweredUp = true;
        }
        else if (!pacman.isPoweredUp())
        {
            wasPoweredUp = false;
        }

        for (Ghost ghost : ghosts)
        {
            ghost.update();

            if (ghost.collidesWith(pacman))
            {
                handleGhostCollision(ghost);
            }
        }

        updatePellets();
        updateUI();

        // Check win condition
        checkWinCondition();
    }

    private void checkWinCondition()
    {
        // Check if all pellets are collected
        if (pelletVisuals.isEmpty() && powerPelletVisuals.isEmpty())
        {
            // Player wins!
            if (gameLoop != null)
            {
                gameLoop.stop();
            }
            gameRunning = false;
            System.out.println("YOU WIN! Final Score: " + pacman.getScore());

            // You can add a win screen here later
            scoreLabel.setText("YOU WIN! Score: " + pacman.getScore());
        }
    }

    private void updateUI()
    {
        scoreLabel.setText("Score: " + pacman.getScore());
        livesLabel.setText("Lives: " + lives);
    }

    private void updatePellets()
    {
        pelletVisuals.removeIf(pellet -> {
            double cellSize = maze.getCellSize();
            int gridX = (int)((pellet.getCenterX() - cellSize / 2) / cellSize);
            int gridY = (int)((pellet.getCenterY() - cellSize / 2) / cellSize);

            if (maze.isEmpty(gridX, gridY))
            {
                mazePane.getChildren().remove(pellet);
                return true;
            }
            return false;
        });

        powerPelletVisuals.removeIf(pellet -> {
            double cellSize = maze.getCellSize();
            int gridX = (int)((pellet.getCenterX() - cellSize / 2) / cellSize);
            int gridY = (int)((pellet.getCenterY() - cellSize / 2) / cellSize);

            if (maze.isEmpty(gridX, gridY))
            {
                mazePane.getChildren().remove(pellet);
                return true;
            }
            return false;
        });
    }

    private void handleGhostCollision(Ghost ghost)
    {
        if (ghost.isFrightened())
        {
            double cellSize = maze.getCellSize();

            ghost.resetPosition(
                    9 * cellSize + cellSize / 2,
                    9 * cellSize + cellSize / 2
            );

            // Add points for eating ghost
            pacman.addScore(200);
        }
        else
        {
            handlePacManDeath();
        }
    }

    private void handlePacManDeath()
    {
        lives--;

        // Immediately reset positions
        double cellSize = maze.getCellSize();
        double startX = 9 * cellSize + cellSize / 2;
        double startY = 14 * cellSize + cellSize / 2;
        pacman.setX(startX);
        pacman.setY(startY);

        // Update visual immediately
        pacman.getVisual().setTranslateX(0);
        pacman.getVisual().setTranslateY(0);
        ((Circle)pacman.getVisual()).setCenterX(startX);
        ((Circle)pacman.getVisual()).setCenterY(startY);

        int[][] ghostStarts = {
                {9, 9},
                {10, 9},
                {8, 9},
                {9, 8}
        };

        for (int i = 0; i < ghosts.size(); i++)
        {
            Ghost ghost = ghosts.get(i);
            ghost.resetPosition(
                    ghostStarts[i][0] * cellSize + cellSize / 2,
                    ghostStarts[i][1] * cellSize + cellSize / 2
            );
        }

        updateUI();

        if (lives <= 0)
        {
            // Game over
            if (gameLoop != null)
            {
                gameLoop.stop();
            }
            gameRunning = false;
            System.out.println("GAME OVER! Final Score: " + pacman.getScore());
            return;
        }

        // Pause AFTER resetting positions
        isPaused = true;
        pauseTimer = RESPAWN_DELAY;
    }

    private void respawnAfterDeath()
    {
        // Just unpause - positions already reset
        isPaused = false;
    }

    private void drawMazeStatic()
    {
        mazePane.getChildren().clear();
        pelletVisuals.clear();
        powerPelletVisuals.clear();

        for (int y = 0; y < maze.getHeight(); y++)
        {
            for (int x = 0; x < maze.getWidth(); x++)
            {
                drawCell(x, y);
            }
        }
    }

    private void drawCell(int x, int y)
    {
        double screenX = x * maze.getCellSize();
        double screenY = y * maze.getCellSize();

        if (maze.isWall(x, y))
        {
            drawWall(screenX, screenY);
        }
        else if (maze.getCell(x, y) == 2)
        {
            Circle pellet = drawPellet(screenX, screenY);
            pelletVisuals.add(pellet);
        }
        else if (maze.getCell(x, y) == 3)
        {
            Circle powerPellet = drawPowerPellet(screenX, screenY);
            powerPelletVisuals.add(powerPellet);
        }
    }

    private void drawWall(double x, double y)
    {
        Rectangle wall = new Rectangle(x, y, maze.getCellSize(), maze.getCellSize());
        wall.setFill(Color.DARKBLUE);
        wall.setStroke(Color.BLUE);
        mazePane.getChildren().add(wall);
    }

    private Circle drawPellet(double x, double y)
    {
        double centerX = x + (double) maze.getCellSize() / 2;
        double centerY = y + (double) maze.getCellSize() / 2;
        Circle pellet = new Circle(centerX, centerY, 3, Color.WHITE);
        mazePane.getChildren().add(pellet);
        return pellet;
    }

    private Circle drawPowerPellet(double x, double y)
    {
        double centerX = x + (double) maze.getCellSize() / 2;
        double centerY = y + (double) maze.getCellSize() / 2;
        Circle powerPellet = new Circle(centerX, centerY, 6, Color.WHITE);
        mazePane.getChildren().add(powerPellet);
        return powerPellet;
    }
}