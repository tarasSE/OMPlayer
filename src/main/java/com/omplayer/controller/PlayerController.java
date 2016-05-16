package com.omplayer.controller;

import com.omplayer.parser.Item;
import com.omplayer.parser.MediaParser;
import com.omplayer.player.Player;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
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
public class PlayerController implements Initializable {
    private Scene scene;
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
    private Button prevItem;
    @FXML
    private Button volumeMinus;
    @FXML
    private Button volumePlus;
    @FXML
    private Button repeat;
    @FXML
    private ProgressBar timeProgress;
    @FXML
    private Slider volumeSlider;
    @FXML
    private TextField searchField;
    @FXML
    private Label info;
    @FXML
    private ListView<Item> itemsList;

    private MediaPlayer player;
    private int currentIndex;
    private Repeat repeatStatus = Repeat.NOT;
    private ChangeListener<Duration> progressChangeListener;

    enum Repeat {
        NOT, ONE, ALL
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert playPause != null : "fx:id=\"playPause\" was not injected: check your FXML file 'player.fxml'.";
        assert stop != null : "fx:id=\"stop\" was not injected: check your FXML file 'player.fxml'.";
        assert back != null : "fx:id=\"back\" was not injected: check your FXML file 'player.fxml'.";
        assert forward != null : "fx:id=\"forward \" was not injected: check your FXML file 'player.fxml'.";
        assert timeProgress != null : "fx:id=\"timeSlider\" was not injected: check your FXML file 'player.fxml'.";

//        this.scene = Player.getPrimaryStage().getScene();
        this.player = Player.getPlayer();
        initializePlayPause();
        initializeStop();
        initializeBack();
        initializeForward();
        initializeVolumeMinus();
        initializeVolumePlus();
        initializeItemsList();
        initializeSearchField();

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

    private void initializeInfoLabel() {
        String text = itemsList.getSelectionModel().getSelectedItem().getShortName();
        info.setText(text);
    }

    private void initializePlayPause() {

        playPause.setOnAction(actionEvent -> {
            MediaPlayer.Status status = player.statusProperty().getValue();

            if(status == null) {
                setCurrentIndex(-1);
                playNextItem();
                return;
            }
            if (status.equals(MediaPlayer.Status.PLAYING)) {
                player.pause();
                return;
            }

            if (status.equals(MediaPlayer.Status.PAUSED) || status.equals(MediaPlayer.Status.STOPPED)) {
                play();
                return;
            }

            setCurrentIndex(-1);
            playNextItem();


        });
    }

    @FXML
    private void playPreviousItem() {
        playItemOnStep(-1);
    }

    @FXML
    private void playNextItem() {
        playItemOnStep(1);
    }

    private void initializeStop() {
        stop.setOnAction(actionEvent -> stop());
    }

    private void initializeBack() {
        back.setOnAction(actionEvent -> player.seek(player.getCurrentTime().subtract(new Duration(5000))));
    }

    private void initializeForward() {
        forward.setOnAction(actionEvent -> player.seek(player.getCurrentTime().add(new Duration(5000))));
    }

    private void initializeVolumeMinus() {
        volumeMinus.setOnAction(event -> volumeDown());
    }

    private void initializeVolumePlus() {
        volumePlus.setOnAction(actionEvent -> volumeUp());
    }

    private void initializeVolumeSlider() {
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            player.setVolume((Double) newValue);
        });
//        volumeSlider.setOnScroll(event -> );
    }

    @FXML
    private void toggleRepeat() {
        repeat.setOnAction(event -> {
            int i = repeatStatus.ordinal() + 1;
            switch (i % 3 == 0 ? i = 0 : i) {
                case 1:
                    repeat.setText("one");
                    setRepeatStatus(Repeat.ONE);
                    break;
                case 2:
                    repeat.setText("all");
                    setRepeatStatus(Repeat.ALL);
                    break;
                default:
                    repeat.setText("not");
                    setRepeatStatus(Repeat.NOT);
            }

        });
    }

    private void initializeItemsList() {

        itemsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Item item = itemsList.getSelectionModel().getSelectedItem();
                setCurrentIndex(itemsList.getSelectionModel().getSelectedIndex());
                player.stop();
                player = getPlayerInstance(item.getUrl());
                play();
            }
        });
    }

    private void play() {
        MediaPlayer.Status status = player.getStatus();
        if (!status.equals(MediaPlayer.Status.PAUSED)){
            setCurrentlyPlaying(player);
        }
        player.play();
        player.setOnEndOfMedia(this::playNextItem);
//        initializeInfoLabel();
    }

    private void stop() {
        player.stop();
        player.seek(new Duration(0));
    }

    private void volumeDown(){
        player.setVolume(player.getVolume() - 0.05);
    }

    private void volumeUp(){
        player.setVolume(player.getVolume() + 0.05);
    }

    private void playItemOnStep(final int i) {
        if (currentIndex + i >= 0) {
            setCurrentIndex(currentIndex + i);
        }

        itemsList.getSelectionModel().select((currentIndex));
        Item item = itemsList.getSelectionModel().getSelectedItem();
        if (item == null) return;
        player.stop();
        player = getPlayerInstance(item.getUrl());
        play();
    }

    private void setCurrentlyPlaying(final MediaPlayer player) {
        player.seek(Duration.ZERO);

        timeProgress.setProgress(0);
        progressChangeListener = (observableValue, oldValue, newValue) -> timeProgress.setProgress(1.0 * player.getCurrentTime().toMillis() / player.getTotalDuration().toMillis());
        player.currentTimeProperty().addListener(progressChangeListener);

        String source = player.getMedia().getSource();
        source = source.substring(0, source.length() - 4);
        source = source.substring(source.lastIndexOf("/") + 1).replaceAll("%20", " ");
        info.setText("Now Playing: " + source);
    }

    private MediaPlayer getPlayerInstance(final String source) {
        Media media = new Media(source);
        MediaPlayer player = new MediaPlayer(media);
        player.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
        return player;
    }
}
