package com.omplayer.player;

import com.omplayer.controller.ListController;
import com.omplayer.controller.PlayerController;
import com.omplayer.controller.RootLayoutController;
import com.omplayer.model.Item;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.net.URISyntaxException;

@EqualsAndHashCode(callSuper = true)
@Data
public class MainApp extends Application {
    private Stage primaryStage;
    private AnchorPane rootLayout;

    private RootLayoutController rootLayoutController;
    private PlayerController playerController;
    private ListController listController;

    private MediaView mediaView;
    private MediaPlayer player;

    private ListView<Item> activeListView;

    private int currentIndex;

    @Override
    public void start(Stage primaryStage) {
        setPrimaryStage(primaryStage);
        initRootLayout(primaryStage);
        initializeMediaView();
        showPlayer();
        showList();
    }

    private void initializeMediaView() {
        Media pick = null;
        try {
            pick = new Media(getClass().getResource("/smth.mp3").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        setPlayer(new MediaPlayer(pick));
        setMediaView(new MediaView(getPlayer()));
    }

    private void showPlayer() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/player.fxml"));
            AnchorPane anchorPane = loader.load();

            getRootLayout().getChildren().add(anchorPane);

            setPlayerController(loader.getController());
            getPlayerController().setMainApp(this);
            getPrimaryStage().show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showList(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/list.fxml"));
            AnchorPane anchorPane = loader.load();

            getRootLayout().getChildren().add(anchorPane);

            setListController(loader.getController());
            getListController().setMainApp(this);
            getPrimaryStage().show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initRootLayout(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/rootLayout.fxml"));
            setRootLayout(loader.load());

            Scene scene = new Scene(getRootLayout());
            primaryStage.setTitle("OMPlayer");
            primaryStage.setMinHeight(500);
            primaryStage.setMinWidth(450);
            primaryStage.setScene(scene);

            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public MediaPlayer getPlayerInstance(final String source) {
        Media media = new Media(source);
//        player.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
        return new MediaPlayer(media);
    }

    public ListView<Item> getSearchItemsListView(){
        return getListController().getSearchItemsListView();
    }

    public ListView<Item> getFavoriteItemsListView(){
        return getListController().getFavoriteItemsListView();
    }

    public void play(){
        getPlayerController().play();
    }

    public void stop(){
        getPlayerController().stop();
    }

}