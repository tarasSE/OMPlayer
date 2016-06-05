package com.omplayer.controller;

import com.omplayer.model.Item;
import com.omplayer.player.MainApp;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Data;

import java.net.URL;
import java.util.ResourceBundle;


@Data
public class PlayerController implements Initializable {
    private MainApp mainApp;
//    @FXML
//    private MediaView mediaView;
//    @FXML
//    private AnchorPane mediaAnchorPane;
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
    private ProgressBar loadProgressBar;
//    @FXML
//    private Slider progressSlider;
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
    private ChangeListener<Duration> loadChangeListener;

    enum Repeat {
        NOT, ONE, ALL
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeAnchors();
//        initializeMediaView();
        initializePlayPause();
        initializeStop();
        initializeBack();
        initializeForward();
        initializeVolumeMinus();
        initializeVolumePlus();
        initializeRepeat();
//        initializeProgressBar();


}

//    private void initializeMediaView() {
////        try {
////            MediaPlayer player = getPlayerInstance(getClass().getResource("/smth.mp3").toURI().toString());
////            setPlayer(player);
//            setMediaView(new MediaView());
////        } catch (URISyntaxException e) {
////            e.printStackTrace();
////        }
//        getPlayerAnchorPane().getChildren().add(getMediaView());
//    }
    private void initializeAnchors() {
        AnchorPane.setTopAnchor(getPlayerAnchorPane(), 0.0);
        AnchorPane.setLeftAnchor(getPlayerAnchorPane(), 0.0);
        AnchorPane.setRightAnchor(getPlayerAnchorPane(), 0.0);

    }

    private void initializePlayPause() {

        getPlayPauseButton().setOnAction(actionEvent -> {
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

    public void initializeAccelerators() {

        addAccessorTo(
                getPrevItemButton(),
                KeyCodeCombination.valueOf("A"));

        addAccessorTo(
                getBackButton(),
                KeyCodeCombination.valueOf("Q"));

        addAccessorTo(
                getPlayPauseButton(),
                KeyCodeCombination.valueOf("W"));

        addAccessorTo(
                getStopButton(),
                KeyCodeCombination.valueOf("S"));

        addAccessorTo(
                getForwardButton(),
                KeyCodeCombination.valueOf("E"));

        addAccessorTo(
                getNextItemButton(),
                KeyCodeCombination.valueOf("D"));

        addAccessorTo(
                getVolumeMinusButton(),
                 KeyCodeCombination.valueOf("1"));

        addAccessorTo(
                getVolumePlusButton(),
                KeyCodeCombination.valueOf("3"));

        addAccessorTo(
                getRepeatButton(),
                KeyCodeCombination.valueOf("R"));

        addAccessorTo(
                getRepeatButton(),
                KeyCodeCombination.valueOf("R"));
    }

    private void addAccessorTo(Button button, KeyCombination combination) {
        button.getScene().getAccelerators().put(combination, button::fire);
    }

    private void initializeStop() {
        getStopButton().setOnAction(actionEvent -> stop());
    }

    private void initializeBack() {
        getBackButton().setOnAction(actionEvent -> getPlayer().seek(getPlayer().getCurrentTime().subtract(Duration.seconds(5))));
    }

    private void initializeForward() {
        getForwardButton().setOnAction(actionEvent -> getPlayer().seek(getPlayer().getCurrentTime().add(Duration.seconds(5))));
    }

    private void initializeVolumeMinus() {
        getVolumeMinusButton().setOnAction(event -> volumeDown());
    }

    private void initializeVolumePlus() {
        getVolumePlusButton().setOnAction(actionEvent -> volumeUp());
    }

    private void initializeRepeat() {
        toggleRepeat();
        setRepeatStatus(Repeat.ALL);

    }
    private void initializeProgressBar(){
//        getProgressSlider().valueProperty().addListener(new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> ov,
//                                Number old_val, Number new_val) {
//                getTimeProgressBar().setProgress(new_val.doubleValue() / 100);
//            }
//        });

    }

    @FXML
    private void toggleRepeat() {
        getRepeatButton().setOnAction(event -> {
            int i = getRepeatStatus().ordinal() + 1;
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
            if (getRepeatStatus().equals(Repeat.ONE)) {
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
            switch (getRepeatStatus()) {
                case ONE:
                case NOT: {
                    setCurrentIndex(0);
                    getActiveListView().getSelectionModel().select(0);
                    stop();
                    getInfoLabel().setText("-=-");
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

    public MediaPlayer getPlayerInstance(final String url) {
        Media media = new Media(url);
        MediaPlayer player = new MediaPlayer(media);
        player.volumeProperty().bindBidirectional(getVolumeSlider().valueProperty());
//        setMediaView(new MediaView(player));
//        player.setOnReady(() -> {
//            mainApp.getPrimaryStage().setMinHeight(player.getMedia().getHeight());
//            mainApp.getPrimaryStage().setMinWidth(player.getMedia().getWidth());
//        });
//
//        mediaAnchorPane.getChildren().add(getMediaView());
        return player;
    }

    private Item getSelectedItem(final ListView<Item> listView) {
        return listView.getSelectionModel().getSelectedItem();
    }

    private Item getFocusedItem(final ListView<Item> listView) {
        return listView.getFocusModel().getFocusedItem();
    }

    private ListView<Item> getActiveListView() {
        return getMainApp().getActiveListView();
    }

    private ListView<Item> getSearchItemsList() {
        return getMainApp().getSearchItemsListView();
    }

    private ListView<Item> getFavoriteItemsListView() {
        return getMainApp().getFavoriteItemsListView();
    }

    private int getCurrentIndex() {
        return getMainApp().getCurrentIndex();
    }

    private MediaPlayer getPlayer() {
        return getMainApp().getPlayer();
    }

    private void setCurrentlyPlaying(final MediaPlayer player) {
        player.seek(Duration.ZERO);

        getTimeProgressBar().setProgress(0);

        setProgressChangeListener(
                (observableValue, oldValue, newValue) -> {
            getTimeProgressBar().setProgress(1.0 * player.getCurrentTime().toMillis() / player.getTotalDuration().toMillis());

            getCurrentTimeLabel().setText(currentTimeToString());
        });

        player.currentTimeProperty().addListener(getProgressChangeListener());
        setCurrentlyLoading(player);

        String source = player.getMedia().getSource();
        source = source.substring(0, source.length() - 4);
        source = source.substring(source.lastIndexOf("/") + 1).replaceAll("%20", " ");
        getInfoLabel().setText(source);
    }

    private void setCurrentlyLoading(final MediaPlayer player){
        getLoadProgressBar().setProgress(0);
        setLoadChangeListener(
                (observableValue, oldValue, newValue) -> {
                    getLoadProgressBar().setProgress(1.0 * player.getBufferProgressTime().toMillis() / player.getTotalDuration().toMillis());
                });

        player.bufferProgressTimeProperty().addListener(getLoadChangeListener());
    }

    private void setRepeatStatus(final Repeat status) {
        this.repeatStatus = status;
        getRepeatButton().setText(getRepeatStatus().name());
    }

    private void setActiveListView(final ListView<Item> activeListView) {
        getMainApp().setActiveListView(activeListView);
    }

    private void setCurrentIndex(final int currentIndex) {
        getMainApp().setCurrentIndex(currentIndex);
    }

    public void setPlayer(final MediaPlayer player) {
        getMainApp().setPlayer(player);
    }

}
