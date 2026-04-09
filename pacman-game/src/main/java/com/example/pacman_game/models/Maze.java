package com.example.pacman_game.models;

public class Maze
{
    private static final int CELL_SIZE = 30;
    private static final int WALL = 1;
    private static final int PELLET = 2;
    private static final int POWER_PELLET = 3;
    private static final int EMPTY = 0;

    private final int[][] MAZE_LAYOUT =
            {
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 3, 1},
                    {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 2, 1, 1, 2, 1, 1, 1, 2, 1},
                    {1, 2, 1, 1, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 1, 1, 2, 1},
                    {1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1},
                    {1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 2, 1, 2, 1},
                    {1, 2, 1, 2, 1, 2, 1, 1, 0, 0, 0, 1, 1, 2, 1, 2, 1, 2, 1},
                    {1, 2, 3, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 1},
                    {1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1},
                    {0, 0, 0, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 0, 0, 0},
                    {1, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 1, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 3, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 1, 1, 1, 2, 1, 1, 2, 2, 2, 1, 1, 2, 1, 1, 1, 2, 1},
                    {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 2, 1, 1, 2, 1, 1, 1, 2, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1},
                    {1, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 3, 1},
                    {1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
            };

    public int getCell(int x, int y)
    {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight())
        {
            return MAZE_LAYOUT[y][x];
        }
        return WALL;
    }

    public void clearCell(int x, int y)
    {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight())
        {
            MAZE_LAYOUT[y][x] = EMPTY;
        }
    }

    public boolean isWall(int x, int y)
    {
        return getCell(x, y) == WALL;
    }

    public boolean isPellet(int x, int y)
    {
        return getCell(x, y) == PELLET;
    }

    public boolean isPowerPellet(int x, int y)
    {
        return getCell(x, y) == POWER_PELLET;
    }

    public boolean isEmpty(int x, int y)
    {
        return getCell(x, y) == EMPTY;
    }

    public int getWidth() { return MAZE_LAYOUT[0].length; }
    public int getHeight() { return MAZE_LAYOUT.length; }
    public int getCellSize() { return CELL_SIZE; }
}
