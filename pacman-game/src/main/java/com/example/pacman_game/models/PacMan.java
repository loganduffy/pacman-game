package com.example.pacman_game.models;

import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class PacMan extends GameEntity
{
    // Private fields - proper encapsulation
    private double speed;
    private final double baseSpeed;
    private final double powerUpSpeed;
    private int direction;
    private final Circle visual;
    private final Maze maze;
    private int nextDirection;
    private final double radius;
    private boolean isMoving;
    private int score;
    private boolean isPoweredUp;
    private int powerUpTimer;
    private static final int POWER_UP_DURATION = 300;

    /**
     * Constructor - initializes PacMan at given position with maze reference
     */
    public PacMan(double x, double y, Maze maze)
    {
        super(x, y);
        this.maze = maze;
        this.radius = maze.getCellSize() * 0.45;
        this.visual = new Circle(x, y, this.radius, Color.YELLOW);
        this.speed = 0.75;
        this.baseSpeed = 0.75;
        this.powerUpSpeed = 1.0;
        this.direction = 0;
        this.nextDirection = -1;
        this.isMoving = true;
        this.score = 0;
        this.isPoweredUp = false;
        this.powerUpTimer = 0;
    }

    // Public getters - proper encapsulation
    public Node getVisual()
    {
        return visual;
    }

    public int getScore()
    {
        return score;
    }

    public void addScore(int points)
    {
        score += points;
    }

    public boolean isPoweredUp()
    {
        return isPoweredUp;
    }


    /**
     * Sets the desired direction for PacMan to move
     * Allows immediate reversal, otherwise buffers direction change
     */
    public void setDirection(int direction)
    {
        int oppositeDir = getOppositeDirection(this.direction);

        if (direction == oppositeDir && canMove(direction))
        {
            this.direction = direction;
            this.nextDirection = -1;
        }
        else
        {
            this.nextDirection = direction;
        }
    }



    // Returns opposite direction
    private int getOppositeDirection(int dir)
    {
        return switch (dir) {
            case 0 -> 2;
            case 1 -> 3;
            case 2 -> 0;
            case 3 -> 1;
            default -> dir;
        };
    }


    public void update()
    {
        updatePowerUp();
        checkPelletCollection();
        handleMovement();
    }


    // Power Up state strictly makes pacman quicker
    private void updatePowerUp()
    {
        if (isPoweredUp)
        {
            powerUpTimer--;
            if (powerUpTimer <= 0)
            {
                isPoweredUp = false;
                speed = baseSpeed;
            }
        }
    }


    // Checks for pellets at current location
    private void checkPelletCollection()
    {
        double cellSize = maze.getCellSize();
        int gridX = (int)(getX() / cellSize);
        int gridY = (int)(getY() / cellSize);

        if (maze.isPellet(gridX, gridY))
        {
            maze.clearCell(gridX, gridY);
            score += 10;
        }
        else if (maze.isPowerPellet(gridX, gridY))
        {
            maze.clearCell(gridX, gridY);
            score += 50;
            activatePowerUp();
        }
    }


    // Activates Power Up mode
    private void activatePowerUp()
    {
        isPoweredUp = true;
        powerUpTimer = POWER_UP_DURATION;
        speed = powerUpSpeed;
    }


    // Handles all of pacmans movements so his directions and wall collisions.
    private void handleMovement()
    {
        double cellSize = maze.getCellSize();

        int currentGridX = (int)(getX() / cellSize);
        int currentGridY = (int)(getY() / cellSize);

        double tileCenterX = currentGridX * cellSize + cellSize / 2;
        double tileCenterY = currentGridY * cellSize + cellSize / 2;

        double distanceToCenter = Math.sqrt(
                Math.pow(getX() - tileCenterX, 2) +
                        Math.pow(getY() - tileCenterY, 2)
        );

        boolean atCenter = distanceToCenter <= speed * 1.5;

        // Handle direction changes at tile center
        if (atCenter && nextDirection != -1)
        {
            if (canMove(nextDirection))
            {
                setX(tileCenterX);
                setY(tileCenterY);
                visual.setCenterX(tileCenterX);
                visual.setCenterY(tileCenterY);

                direction = nextDirection;
                nextDirection = -1;
                isMoving = true;
            }
            else
            {
                nextDirection = -1;
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
        if (tunnelWrapping(newX, newY))
        {
            return;
        }

        // Move if no collision
        if (canMoveTo(newX, newY))
        {
            setX(newX);
            setY(newY);
            visual.setCenterX(newX);
            visual.setCenterY(newY);
            isMoving = true;
        }
        else if (isMoving)
        {
            setX(tileCenterX);
            setY(tileCenterY);
            visual.setCenterX(tileCenterX);
            visual.setCenterY(tileCenterY);
            isMoving = false;
        }
    }


    // Handles tunnel wrapping at edges
    private boolean tunnelWrapping(double newX, double newY)
    {
        double cellSize = maze.getCellSize();
        int gridY = (int)(getY() / cellSize);

        if (gridY == 11) // Tunnel row
        {
            // Left side tunnel exit
            if (newX < cellSize)
            {
                newX = (maze.getWidth() - 2) * cellSize;
                setX(newX);
                setY(newY);
                visual.setCenterX(newX);
                visual.setCenterY(newY);
                isMoving = true;
                return true;
            }
            // Right side tunnel exit
            else if (newX > (maze.getWidth() - 2) * cellSize)
            {
                newX = cellSize;
                setX(newX);
                setY(newY);
                visual.setCenterX(newX);
                visual.setCenterY(newY);
                isMoving = true;
                return true;
            }
        }
        return false;
    }



    // Checks if pacman can move in that direction
    private boolean canMove(int dir)
    {
        double cellSize = maze.getCellSize();
        double testDistance = cellSize / 2 + 2;

        double testX = getX();
        double testY = getY();

        switch(dir)
        {
            case 0: testX += testDistance; break;
            case 1: testY += testDistance; break;
            case 2: testX -= testDistance; break;
            case 3: testY -= testDistance; break;
        }

        int gridX = (int)(testX / cellSize);
        int gridY = (int)(testY / cellSize);

        return !maze.isWall(gridX, gridY);
    }


    // Checks if pacman can move to the NEXT position
    private boolean canMoveTo(double centerX, double centerY)
    {
        double cellSize = maze.getCellSize();

        // Check all four corners of PacMan's bounding box (CHATGPT HELPED ME HERE)
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

            if (maze.isWall(gridX, gridY))
            {
                return false;
            }
        }

        return true;
    }
}