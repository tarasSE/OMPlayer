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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.io.IOException;
import java.net.URISyntaxException;

@EqualsAndHashCode(callSuper = true)
@Data
public class Player extends Application {
    private @Setter static Stage primaryStage;
    private AnchorPane rootLayout;
    private static MediaPlayer player;

    @Override
    public void start(Stage primaryStage) {
        setPrimaryStage(primaryStage);
        //Add a scene
        Group root = new Group();
        Scene scene = new Scene(root, 500, 200);

        Media pick = null;
        try {
            pick = new Media(Player.class.getResource("/smth.mp3").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        player = new MediaPlayer(pick);

        MediaView mediaView = new MediaView(player);
        ((Group) scene.getRoot()).getChildren().add(mediaView);

        initRootLayout(primaryStage);
    }

    public void initRootLayout(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Player.class.getResource("/view/player.fxml"));
            rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setTitle("OMPlayer");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MediaPlayer getPlayer(){
        return player;
    }

    public static void setPlayer(MediaPlayer player) {
        Player.player = player;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}