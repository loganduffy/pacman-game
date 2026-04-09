module com.example.pacman_game
{
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.pacman_game to javafx.fxml;
    exports com.example.pacman_game;
    exports com.example.pacman_game.controller;
    opens com.example.pacman_game.controller to javafx.fxml;
}