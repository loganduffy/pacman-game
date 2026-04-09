package com.example.pacman_game;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PacmanApplication extends Application
{
    @Override
    public void start(Stage stage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(PacmanApplication.class.getResource("/com/example/pacman_game/maze-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 570, 760);
        stage.setTitle("Pac-Man Game");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
