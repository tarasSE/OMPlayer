package com.omplayer.controller;

import com.omplayer.parser.Item;
import com.omplayer.parser.MediaParser;
import com.omplayer.player.Player;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Data;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Data
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
    private Button nextItem;
    @FXML
    private Button volumeMinus;
    @FXML
    private Button volumePlus;
    @FXML
    private Slider timeSlider;
    @FXML
    private Slider volumeSlider;
    @FXML
    private TextField searchField;
    @FXML
    private Label info;
    @FXML
    private ListView<Item> itemsList;
    private int currentIndex;

    private ChangeListener<Duration> progressChangeListener;
    private MapChangeListener<String, Object> metadataChangeListener;



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
        initializeSearchField();
        initializeInfoLabel();

    }

    private void initializeInfoLabel() {
        info.setText("Some \n text");
    }

    @FXML
    private void playNextItem(){
        itemsList.getSelectionModel().select(++currentIndex);
        Item item = itemsList.getSelectionModel().getSelectedItem();
        player.stop();
        player = getPlayerInstance(item.getUrl());
        player.play();
    }

    @FXML
    private void initializeSearchField() {
        searchField.setOnAction(event -> {
            MediaParser parser = new MediaParser();
            try {

                List<Item> list = parser.getMP3(
                        "http://mp3.cc/search/f/" + URLEncoder.encode(searchField.getText().trim(), "UTF-8"))
                        .parallelStream().collect(Collectors.toList());
                itemsList.setItems(FXCollections.observableArrayList(list));
                System.out.println(searchField.getText());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    private void initializeItemsList() {
//        ObservableList<Item> list = FXCollections.observableArrayList(
//                Item.getInstance("One", "1"), Item.getInstance("Two", "2")
//        );
//        itemsList.setItems(list);
        itemsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Item item = itemsList.getSelectionModel().getSelectedItem();
                currentIndex = itemsList.getSelectionModel().getSelectedIndex();
                player.stop();
                player = getPlayerInstance(item.getUrl());
                player.play();
                player.setOnEndOfMedia(this::playNextItem);
            }
        });
    }

    private void initializePlayPauseButton() {
        playPause.setOnAction(actionEvent -> {
            MediaPlayer.Status status = player.statusProperty().getValue();
            if (status.equals(MediaPlayer.Status.PLAYING)) {
                player.pause();
            } else if (status.equals(MediaPlayer.Status.PAUSED) || status.equals(MediaPlayer.Status.STOPPED)){
                player.play();
//                player.setOnEndOfMedia(this::playNextItem);
                player.setOnEndOfMedia(this::playNextItem);
            }
            else {
                setCurrentIndex(-1);
                playNextItem();
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

    private MediaPlayer getPlayerInstance(String source) {
        Media media = new Media(source);
        return new MediaPlayer(media);
    }
//   0 private void initializePlayPauseButton(){}
//    private void initializePlayPauseButton(){}
}
