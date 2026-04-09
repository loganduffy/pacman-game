package com.example.pacman_game.models;

import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Ghost extends GameEntity
{
    private double speed;
    private int direction;
    private final Circle visual;
    private final Maze maze;
    private final Color ghostColor;
    private final double radius;
    private final Random random;

    // Ghost behavior modes
    private GhostMode mode;
    private int frightenedTimer;
    private static final int FRIGHTENED_DURATION = 600;

    private PacMan targetPacman;

    private int moveCounter;
    private static final int DECISION_INTERVAL = 10;

    public enum GhostMode
    {
        CHASE,
        SCATTER,
        FRIGHTENED
    }

    public Ghost(double x, double y, Maze maze, Color color)
    {
        super(x, y);
        this.maze = maze;
        this.ghostColor = color;
        this.speed = 0.75;
        this.radius = maze.getCellSize() * 0.45;
        this.visual = new Circle(x, y, this.radius, ghostColor);
        this.random = new Random();
        this.direction = random.nextInt(4);
        this.mode = GhostMode.CHASE; // Start in chase mode
        this.frightenedTimer = 0;
        this.moveCounter = 0;
    }

    public Node getVisual()
    {
        return visual;
    }


    public void setMode(GhostMode mode)
    {
        this.mode = mode;

        if (mode == GhostMode.FRIGHTENED)
        {
            frightenedTimer = FRIGHTENED_DURATION;
            visual.setFill(Color.BLUE);
            speed = 0.5;
            // Reverse direction when frightened
            direction = getOppositeDirection(direction);
        }
        else
        {
            visual.setFill(ghostColor);
            speed = 0.75;
        }
    }

    public boolean isFrightened()
    {
        return mode == GhostMode.FRIGHTENED;
    }

    public void resetPosition(double x, double y)
    {
        setX(x);
        setY(y);
        visual.setCenterX(x);
        visual.setCenterY(y);
        direction = random.nextInt(4);

        // Reset to NORMAL state, not frightened
        mode = GhostMode.CHASE;
        visual.setFill(ghostColor);
        speed = 0.75;
        frightenedTimer = 0;
    }

    public void update()
    {
        if (mode == GhostMode.FRIGHTENED)
        {
            frightenedTimer--;

            if (frightenedTimer < 60 && frightenedTimer % 10 < 5)
            {
                visual.setFill(Color.WHITE);
            }
            else if (frightenedTimer < 60)
            {
                visual.setFill(Color.BLUE);
            }

            if (frightenedTimer <= 0)
            {
                setMode(GhostMode.CHASE);
            }
        }

        moveGhost();
    }

    public void setReferences(PacMan pacman)
    {
        this.targetPacman = pacman;
    }

    private void moveGhost()
    {
        double cellSize = maze.getCellSize();

        // Get current grid position
        int currentGridX = (int)(getX() / cellSize);
        int currentGridY = (int)(getY() / cellSize);

        // Calculate tile center
        double tileCenterX = currentGridX * cellSize + cellSize / 2;
        double tileCenterY = currentGridY * cellSize + cellSize / 2;

        // Check if at intersection
        double distanceToCenter = Math.sqrt(
                Math.pow(getX() - tileCenterX, 2) +
                        Math.pow(getY() - tileCenterY, 2)
        );

        boolean atIntersection = distanceToCenter <= speed * 1.5;

        // Make decision at intersections
        if (atIntersection)
        {
            moveCounter++;
            if (moveCounter >= DECISION_INTERVAL || !canMoveInDirection(direction))
            {
                // Snap to center
                setX(tileCenterX);
                setY(tileCenterY);
                visual.setCenterX(tileCenterX);
                visual.setCenterY(tileCenterY);

                chooseDirection(currentGridX, currentGridY);
                moveCounter = 0;
            }
        }

        // Calculate new position
        double newX = getX();
        double newY = getY();

        switch(direction)
        {
            case 0: newX += speed; break; // Right
            case 1: newY += speed; break; // Down
            case 2: newX -= speed; break; // Left
            case 3: newY -= speed; break; // Up
        }

        // Handle tunnel wrapping
        int gridY = (int)(getY() / cellSize);
        if (gridY == 11)
        {
            if (newX <= 0)
            {
                newX = (maze.getWidth() - 1) * cellSize - cellSize / 2;
                setX(newX);
                visual.setCenterX(newX);
                return;
            }
            else if (newX >= (maze.getWidth() - 1) * cellSize)
            {
                newX = cellSize / 2;
                setX(newX);
                visual.setCenterX(newX);
                return;
            }
        }

        // Move if possible
        if (canMoveTo(newX, newY))
        {
            setX(newX);
            setY(newY);
            visual.setCenterX(newX);
            visual.setCenterY(newY);
        }
    }

    private void chooseDirection(int currentGridX, int currentGridY)
    {
        if (mode == GhostMode.FRIGHTENED)
        {
            // Random movement when frightened
            chooseFrightenedDirection();
            return;
        }

        // Chase mode
        if (targetPacman == null)
        {
            chooseFrightenedDirection();
            return;
        }

        double cellSize = maze.getCellSize();
        int pacmanGridX = (int)(targetPacman.getX() / cellSize);
        int pacmanGridY = (int)(targetPacman.getY() / cellSize);

        // Count how many valid directions there are
        List<Integer> validDirections = new ArrayList<>();
        for (int dir = 0; dir < 4; dir++)
        {
            if (dir != getOppositeDirection(direction) && canMoveInDirection(dir))
            {
                validDirections.add(dir);
            }
        }

        // At intersections (3+ directions), 20% chance to go random direction
        if (validDirections.size() >= 2 && random.nextInt(100) < 20)
        {
            direction = validDirections.get(random.nextInt(validDirections.size()));
            return;
        }

        // Find best direction toward PacMan
        int bestDir = -1;
        double bestDistance = Double.MAX_VALUE;

        for (int dir = 0; dir < 4; dir++)
        {
            // Don't reverse direction
            if (dir == getOppositeDirection(direction))
            {
                continue;
            }

            if (canMoveInDirection(dir))
            {
                int testX = currentGridX;
                int testY = currentGridY;

                switch(dir)
                {
                    case 0: testX++; break;
                    case 1: testY++; break;
                    case 2: testX--; break;
                    case 3: testY--; break;
                }

                // Calculate distance to PacMan
                double distance = Math.sqrt(
                        Math.pow(testX - pacmanGridX, 2) +
                                Math.pow(testY - pacmanGridY, 2)
                );

                if (distance < bestDistance)
                {
                    bestDistance = distance;
                    bestDir = dir;
                }
            }
        }

        if (bestDir != -1)
        {
            direction = bestDir;
        }
        else
        {
            // Change ghosts direction incase of it being stuck.
            chooseFrightenedDirection();
        }
    }

    private void chooseFrightenedDirection()
    {
        List<Integer> validDirs = new ArrayList<>();

        // Try to avoid reversing
        for (int dir = 0; dir < 4; dir++)
        {
            if (dir != getOppositeDirection(direction) && canMoveInDirection(dir))
            {
                validDirs.add(dir);
            }
        }

        // If no options, allow reverse
        if (validDirs.isEmpty())
        {
            for (int dir = 0; dir < 4; dir++)
            {
                if (canMoveInDirection(dir))
                {
                    validDirs.add(dir);
                }
            }
        }

        if (!validDirs.isEmpty())
        {
            direction = validDirs.get(random.nextInt(validDirs.size()));
        }
    }

    private int getOppositeDirection(int dir)
    {
        return switch (dir)
        {
            case 0 -> 2;
            case 1 -> 3;
            case 2 -> 0;
            case 3 -> 1;
            default -> dir;
        };
    }

    private boolean canMoveInDirection(int dir)
    {
        double cellSize = maze.getCellSize();
        double testDistance = cellSize * 0.6;

        double testX = getX();
        double testY = getY();

        switch(dir)
        {
            case 0: testX += testDistance; break;
            case 1: testY += testDistance; break;
            case 2: testX -= testDistance; break;
            case 3: testY -= testDistance; break;
        }

        return canMoveTo(testX, testY);
    }

    private boolean canMoveTo(double centerX, double centerY)
    {
        double cellSize = maze.getCellSize();

        // Check all four corners of the ghost's bounding box (same as PacMan)
        double[][] checkPoints = {
                {centerX + radius, centerY + radius}, // Bottom-right
                {centerX - radius, centerY + radius}, // Bottom-left
                {centerX + radius, centerY - radius}, // Top-right
                {centerX - radius, centerY - radius}  // Top-left
        };

        for (double[] point : checkPoints)
        {
            int gridX = (int)(point[0] / cellSize);
            int gridY = (int)(point[1] / cellSize);

            if (gridX < 0 || gridX >= maze.getWidth() || gridY < 0 || gridY >= maze.getHeight())
            {
                return false;
            }

            if (maze.isWall(gridX, gridY))
            {
                return false;
            }
        }

        return true;
    }

    // Checks for collision (chatgpt helped with math)
    public boolean collidesWith(PacMan pacman)
    {
        double distance = Math.sqrt(Math.pow(getX() - pacman.getX(), 2) +
                          Math.pow(getY() - pacman.getY(), 2)
        );

        return distance < (radius * 1.5);
    }
}