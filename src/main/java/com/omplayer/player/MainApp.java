package com.omplayer.player;

import com.omplayer.controller.ListController;
import com.omplayer.controller.PlayerController;
import com.omplayer.controller.RootLayoutController;
import com.omplayer.model.Item;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;
import java.net.URISyntaxException;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(exclude = {"playerController", "listController"})
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

        showList();
        showPlayer();
        initializeAllAccelerators();

    }

    private void initializeMediaView() {
        try {
            MediaPlayer player = getPlayerInstance(getClass().getResource("/smth.mp3").toURI().toString());
            setPlayer(player);
            setMediaView(new MediaView(getPlayer()));
        } catch (URISyntaxException e) {
        e.printStackTrace();
    }
        getRootLayout().getChildren().add(getMediaView());
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
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE){
                    getRootLayout().requestFocus();
                }
            });
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
    private void initializeAllAccelerators(){
        getPlayerController().initializeAccelerators();
        getListController().initializeAccelerators();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public MediaPlayer getPlayerInstance(final String source) {
        return getPlayerController().getPlayerInstance(source);
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