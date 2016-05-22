package com.omplayer.controller;

import com.omplayer.model.Item;
import com.omplayer.player.MainApp;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.Data;

import java.net.URL;
import java.util.ResourceBundle;


@Data
public class PlayerController implements Initializable {
    public Font x1;
    public Font x2;
    private MainApp mainApp;
    @FXML
    private AnchorPane playerAnchorPane;
    @FXML
    private Button playPauseButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button nextItemButton;
    @FXML
    private Button prevItemButton;
    @FXML
    private Button volumeMinusButton;
    @FXML
    private Button volumePlusButton;
    @FXML
    private Button repeatButton;
    @FXML
    private ProgressBar timeProgressBar;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label infoLabel;
    @FXML
    private Label currentTimeLabel;
    @FXML
    private Label totalTimeLabel;

    private Repeat repeatStatus = Repeat.ALL;
    private ChangeListener<Duration> progressChangeListener;

    enum Repeat {
        NOT, ONE, ALL
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeAnchors();
        initializePlayPause();
        initializeStop();
        initializeBack();
        initializeForward();
        initializeVolumeMinus();
        initializeVolumePlus();
        initializeRepeat();

    }

    private void initializeAnchors() {
        AnchorPane.setTopAnchor(getPlayerAnchorPane(), 0.0);
        AnchorPane.setLeftAnchor(getPlayerAnchorPane(), 0.0);
        AnchorPane.setRightAnchor(getPlayerAnchorPane(), 0.0);

    }

    private void initializePlayPause() {

        playPauseButton.setOnAction(actionEvent -> {
            MediaPlayer.Status status = getPlayer().statusProperty().getValue();

            if (getSearchItemsList().getItems() == null ||
                    getSearchItemsList().getItems().size() == 0) {
                setActiveListView(getFavoriteItemsListView());
            }

            if (status == null) {
                setCurrentIndex(-1);
                playNextItem();
                return;
            }
            if (status.equals(MediaPlayer.Status.PLAYING)) {
                getPlayer().pause();
                return;
            }

            if (status.equals(MediaPlayer.Status.PAUSED) || status.equals(MediaPlayer.Status.STOPPED)) {
                play();
                return;
            }

            //play first item from list
            setCurrentIndex(-1);
            playNextItem();
        });
    }

    private void initializeStop() {
        stopButton.setOnAction(actionEvent -> stop());
    }

    private void initializeBack() {
        backButton.setOnAction(actionEvent -> getPlayer().seek(getPlayer().getCurrentTime().subtract(Duration.seconds(5))));
    }

    private void initializeForward() {
        forwardButton.setOnAction(actionEvent -> getPlayer().seek(getPlayer().getCurrentTime().add(Duration.seconds(5))));
    }

    private void initializeVolumeMinus() {
        volumeMinusButton.setOnAction(event -> volumeDown());
    }

    private void initializeVolumePlus() {
        volumePlusButton.setOnAction(actionEvent -> volumeUp());
    }

    private void initializeRepeat() {
        toggleRepeat();
        setRepeatStatus(Repeat.ALL);

    }

    @FXML
    private void toggleRepeat() {
        repeatButton.setOnAction(event -> {
            int i = repeatStatus.ordinal() + 1;
            switch (i % 3 == 0 ? i = 0 : i) {
                case 1:
                    setRepeatStatus(Repeat.ONE);
                    break;
                case 2:
                    setRepeatStatus(Repeat.ALL);
                    break;
                default:
                    setRepeatStatus(Repeat.NOT);
            }

        });
    }

    public void play() {
        MediaPlayer.Status status = getPlayer().getStatus();
        if (!status.equals(MediaPlayer.Status.PAUSED)) {
            setCurrentlyPlaying(getPlayer());
        }
        getPlayer().play();
        getPlayer().totalDurationProperty().addListener(observable -> {
            getTotalTimeLabel().setText(totalTimeToString());
        });
        getPlayer().setOnEndOfMedia(() -> {
            if (repeatStatus.equals(Repeat.ONE)) {
                getPlayer().seek(Duration.ZERO);
                return;
            }
            playNextItem();
        });
    }

    public void stop() {
        getPlayer().stop();
        getPlayer().seek(Duration.ZERO);
    }

    private void volumeDown() {
        getPlayer().setVolume(getPlayer().getVolume() - 0.05);
    }

    private void volumeUp() {
        getPlayer().setVolume(getPlayer().getVolume() + 0.05);
    }

    @FXML
    private void playPreviousItem() {
        playItemOnStep(-1);
    }

    @FXML
    private void playNextItem() {
        playItemOnStep(1);
    }

    private void playItemOnStep(final int i) {
        if (getCurrentIndex() + i >= 0) {
            setCurrentIndex(getCurrentIndex() + i);
        }

        if (getActiveListView() == null) return;

        int activeListSize = getActiveListView().getItems().size();

        if (getCurrentIndex() < 0 || getCurrentIndex() >= activeListSize) {
            switch (repeatStatus) {
                case ONE:
                case NOT: {
                    setCurrentIndex(0);
                    getActiveListView().getSelectionModel().select(0);
                    stop();
                    infoLabel.setText("-=-");
                    return;
                }
                case ALL: {
                    if (getCurrentIndex() < 0) {
                        setCurrentIndex(activeListSize);
                        break;
                    }
                    setCurrentIndex(0);
                    break;
                }

            }
        }

        getActiveListView().getSelectionModel().select(getCurrentIndex());
        Item item = getSelectedItem(getActiveListView());
        if (item == null) return;
        getPlayer().stop();
        setPlayer(getPlayerInstance(item.getUrl()));
        play();
    }

    private String currentTimeToString() {
        int curentTimeSeconds = (int) getPlayer().getCurrentTime().toSeconds();
        int minutes = curentTimeSeconds / 60;
        int seconds = curentTimeSeconds - minutes * 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String totalTimeToString() {
        int totalTimeSeconds = (int) getPlayer().getTotalDuration().toSeconds();
        int minutes = totalTimeSeconds / 60;
        int seconds = totalTimeSeconds - minutes * 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private MediaPlayer getPlayerInstance(String url) {
        return mainApp.getPlayerInstance(url);
    }

    private Item getSelectedItem(final ListView<Item> listView) {
        return listView.getSelectionModel().getSelectedItem();
    }

    private Item getFocusedItem(final ListView<Item> listView) {
        return listView.getFocusModel().getFocusedItem();
    }

    private ListView<Item> getActiveListView() {
        return mainApp.getActiveListView();
    }

    private ListView<Item> getSearchItemsList() {
        return mainApp.getSearchItemsListView();
    }

    private ListView<Item> getFavoriteItemsListView() {
        return mainApp.getFavoriteItemsListView();
    }

    private int getCurrentIndex() {
        return mainApp.getCurrentIndex();
    }

    private MediaPlayer getPlayer() {
        return mainApp.getPlayer();
    }

    private void setCurrentlyPlaying(final MediaPlayer player) {
        player.seek(Duration.ZERO);

        timeProgressBar.setProgress(0);
        progressChangeListener = (observableValue, oldValue, newValue) -> {
            timeProgressBar.setProgress(1.0 * player.getCurrentTime().toMillis() / player.getTotalDuration().toMillis());

            getCurrentTimeLabel().setText(currentTimeToString());
        };

        player.currentTimeProperty().addListener(progressChangeListener);

        String source = player.getMedia().getSource();
        source = source.substring(0, source.length() - 4);
        source = source.substring(source.lastIndexOf("/") + 1).replaceAll("%20", " ");
        infoLabel.setText(source);
    }

    private void setRepeatStatus(Repeat status) {
        this.repeatStatus = status;
        this.repeatButton.setText(repeatStatus.name());
    }

    private void setActiveListView(ListView<Item> activeListView) {
        mainApp.setActiveListView(activeListView);
    }

    private void setCurrentIndex(int currentIndex) {
        mainApp.setCurrentIndex(currentIndex);
    }

    public void setPlayer(MediaPlayer player) {
        mainApp.setPlayer(player);
    }

}
