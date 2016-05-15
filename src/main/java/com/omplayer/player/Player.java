package com.omplayer.player;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Player extends Application {
    private Stage primaryStage;
    private AnchorPane rootLayout;
    private static MediaPlayer player;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        //Add a scene
        Group root = new Group();
        Scene scene = new Scene(root, 500, 200);

        FXMLLoader fxmlLoader = new FXMLLoader(Player.class.getResource("player.fxml"));

        Media pick = new Media(new File("/home/taras/IdeaProjects/NEH/02-КтоТы.mp3").toURI().toString());
        player = new MediaPlayer(pick);
//        player.play();

        //Add a mediaView, to display the media. Its necessary !
        //This mediaView is added to a Pane
        MediaView mediaView = new MediaView(player);
        ((Group) scene.getRoot()).getChildren().add(mediaView);

        //show the stage

        initRootLayout(primaryStage);
    }

    public void initRootLayout(Stage primaryStage) {
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Player.class.getResource("/player.fxml"));
            rootLayout = loader.load();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MediaPlayer getPlayer(){
        return player;
    }


    public static void main(String[] args) {
        launch(args);
    }
}