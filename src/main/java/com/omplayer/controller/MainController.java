package com.omplayer.controller;

import com.omplayer.player.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private MediaPlayer player;
    @FXML
    private Button playPause;
    @FXML
    private Button stop;
    @FXML
    private Button back;
    @FXML
    private Button forward;
    @FXML
    private Button volumeMinus;
    @FXML
    private Button volumePlus;
    @FXML
    private Slider timeSlider;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ListView<String> itemsList;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert playPause != null : "fx:id=\"playPause\" was not injected: check your FXML file 'player.fxml'.";
        assert stop != null : "fx:id=\"stop\" was not injected: check your FXML file 'player.fxml'.";
        assert back != null : "fx:id=\"back\" was not injected: check your FXML file 'player.fxml'.";
        assert forward != null : "fx:id=\"forward \" was not injected: check your FXML file 'player.fxml'.";
        assert timeSlider != null : "fx:id=\"timeSlider\" was not injected: check your FXML file 'player.fxml'.";

        this.player = Player.getPlayer();
        initializePlayPauseButton();
        initializeStopButton();
        initializeBackButton();
        initializeForwardButton();
        initializeVolumeMinusButton();
        initializeVolumePlusButton();
        initializeItemsList();

    }

    private void initializeItemsList() {
        ObservableList<String> list = FXCollections.observableArrayList(
                "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten");
        itemsList.setItems(list);
    }

    private void initializePlayPauseButton() {
        playPause.setOnAction(actionEvent -> {
            if (player.statusProperty().getValue().equals(MediaPlayer.Status.PLAYING)) {
                player.pause();
            } else {
                player.play();
            }
        });


    }

    private void initializeStopButton() {
        stop.setOnAction(actionEvent -> player.stop());
    }

    private void initializeBackButton() {
        back.setOnAction(actionEvent -> player.seek(player.getCurrentTime().subtract(new Duration(5000))));
    }

    private void initializeForwardButton() {
        forward.setOnAction(actionEvent -> player.seek(player.getCurrentTime().add(new Duration(5000))));
    }

    private void initializeVolumeMinusButton() {
        volumeMinus.setOnAction(actionEvent -> player.setVolume(player.getVolume() - 0.1));
    }

    private void initializeVolumePlusButton() {
        volumePlus.setOnAction(actionEvent -> player.setVolume(player.getVolume() + 0.1));
    }

    private void initializeTimeSlider() {

    }
//   0 private void initializePlayPauseButton(){}
//    private void initializePlayPauseButton(){}
}
