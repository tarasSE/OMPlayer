package com.omplayer.controller;

import com.omplayer.model.Item;
import com.omplayer.parser.MediaParser;
import com.omplayer.player.Player;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Data;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Data
public class PlayerController implements Initializable {
    //    private Scene scene;
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
    private Button addToFavorites;
    @FXML
    private Button removeFromFavoritesButton;
    @FXML
    private Button clearResultButton;
    @FXML
    private ProgressBar timeProgressBar;
    @FXML
    private Slider volumeSlider;
    @FXML
    private TextField searchField;
    @FXML
    private Label infoLabel;
    @FXML
    private Label currentTimeLabel;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private ListView<Item> itemsList;
    @FXML
    private ListView<Item> favoriteItemsListView;

    private ListView<Item> activeListView = itemsList;

    private MediaPlayer player;
    private MediaParser parser = new MediaParser();
    private int currentIndex;
    private Repeat repeatStatus = Repeat.ALL;
    private ChangeListener<Duration> progressChangeListener;

    enum Repeat {
        NOT, ONE, ALL
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert playPauseButton != null : "fx:id=\"playPauseButton\" was not injected: check your FXML file 'player.fxml'.";
        assert stopButton != null : "fx:id=\"stopButton\" was not injected: check your FXML file 'player.fxml'.";
        assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'player.fxml'.";
        assert forwardButton != null : "fx:id=\"forwardButton \" was not injected: check your FXML file 'player.fxml'.";
        assert timeProgressBar != null : "fx:id=\"timeSlider\" was not injected: check your FXML file 'player.fxml'.";

//        this.scene = Player.getPrimaryStage().getScene();
        this.player = Player.getPlayer();
        initializePlayPause();
        initializeStop();
        initializeBack();
        initializeForward();
        initializeVolumeMinus();
        initializeVolumePlus();
        initializeRepeat();
        initializeItemsList();
        initializeFavoriteItemsList();
        initializeAddToFavorites();
        initializeRemoveFromFavorites();
        initializeSearchField();

    }

    @FXML
    private void initializeSearchField() {
        searchField.setOnAction(event -> {
            try {
                searchFromMp3cc(
                        "http://mp3.cc/search/f/" +
                                URLEncoder.encode(searchField.getText().trim(), "UTF-8"));
                itemsList.scrollTo(0);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    private void searchFromMp3cc(String searchRequest) {  // FIXME: 18.05.16 Bottleneck
        ObservableList<Item> list = FXCollections.observableArrayList();

        try {
            list.addAll(parser.getMP3(searchRequest)
                    .parallelStream().collect(Collectors.toList()));
            itemsList.setItems(list);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 2;
        while (true) {
            try {
                list.addAll(
                        parser.getMP3(
                                searchRequest + "/page/" + i++ + "/")
                                .parallelStream().collect(Collectors.toList()
                        ));
            } catch (IOException e) {
                break;
            }
        }
    }


//    private void initializeInfoLabel() {
//        String text = itemsList.getSelectionModel().getSelectedResultItem().getShortName();
//        infoLabel.setText(text);
//    }

    private void initializePlayPause() {

        playPauseButton.setOnAction(actionEvent -> {
            MediaPlayer.Status status = player.statusProperty().getValue();

            if (itemsList.getItems() == null ||
                    itemsList.getItems().size() == 0) {
                setActiveListView(favoriteItemsListView);
            }

            if (status == null) {
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

            //play first item from list
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
        stopButton.setOnAction(actionEvent -> stop());
    }

    private void initializeBack() {
        backButton.setOnAction(actionEvent -> player.seek(player.getCurrentTime().subtract(Duration.seconds(5))));
    }

    private void initializeForward() {
        forwardButton.setOnAction(actionEvent -> player.seek(player.getCurrentTime().add(Duration.seconds(5))));
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

//    private void initializeVolumeSlider() {
//        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
//            player.setVolume((Double) newValue);
//        });
////        volumeSlider.setOnScroll(event -> );
//    }

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

    private void setRepeatStatus(Repeat status) {
        this.repeatStatus = status;
        this.repeatButton.setText(repeatStatus.name());
    }

    private void initializeItemsList() {
        itemsList.setOnMouseClicked(event -> twoClickPlay(event, itemsList));
//        itemsList.setCellFactory(lv -> {
//
//            ListCell<Item> cell = new ListCell<>();
//
//            ContextMenu contextMenu = new ContextMenu();
//
//
//            MenuItem editItem = new MenuItem();
//            editItem.textProperty().bind(Bindings.format("Edit \"%s\"", cell.itemProperty()));
//            editItem.setOnAction(event -> {
//                Item item = cell.getItem();
//                // code to edit item...
//            });
//            MenuItem deleteItem = new MenuItem();
//            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
//            deleteItem.setOnAction(event -> itemsList.getItems().remove(cell.getItem()));
//            contextMenu.getItems().addAll(editItem, deleteItem);
//
//            cell.textProperty().bind(cell.itemProperty());
//
//            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
//                if (isNowEmpty) {
//                    cell.setContextMenu(null);
//                } else {
//                    cell.setContextMenu(contextMenu);
//                }
//            });
//            return cell ;
//        });
    }

    private void initializeFavoriteItemsList() {
        //load favorites.list
        File file = new File("./favorites.list");
        File fileBk = new File("./favorites.list.bk");
        if (!file.exists()) {
            if (fileBk.exists()) {
                fileBk.renameTo(file);

            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try (
                InputStream is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            List<Item> list = reader.lines().map(Item::getInstance).collect(Collectors.toList());
            favoriteItemsListView.setItems(FXCollections.observableArrayList(list));

        } catch (IOException e) {
            e.printStackTrace();
        }

        favoriteItemsListView.setOnMouseClicked(event -> twoClickPlay(event, favoriteItemsListView));
    }

    private void initializeAddToFavorites() {

        addToFavorites.setOnAction(event -> {

            Item item = getFocusedItem(itemsList);
            getFavoritesList().add(item);

            refreshFavoritesList();
        });
    }

    private void initializeRemoveFromFavorites() {
        removeFromFavoritesButton.setOnAction(event -> {
            getFavoritesList().remove(getFocusedItem(favoriteItemsListView));

            refreshFavoritesList();
        });
    }

    private List<Item> getFavoritesList() {
        return favoriteItemsListView.getItems();
    }

    private void refreshFavoritesList() {
        List<Item> items = favoriteItemsListView.getItems();
        File file = new File("./favorites.list");
        File fileBk = new File("./favorites.list.bk");
        file.renameTo(fileBk);

        try (FileWriter printWriter = new FileWriter(new File("./favorites.list"))) {
            items.forEach(x -> {
                try {
                    printWriter.append(x.getUrl()).append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fileBk.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void twoClickPlay(MouseEvent event, ListView<Item> list) {
        if (event.getClickCount() == 2) {
            Item item = list.getSelectionModel().getSelectedItem();
            setCurrentIndex(list.getSelectionModel().getSelectedIndex());

            if (player != null) {
                player.stop();
            }

            player = getPlayerInstance(item.getUrl());
            setActiveListView(list);
            play();
        }
    }

    private void play() {
        MediaPlayer.Status status = player.getStatus();
        if (!status.equals(MediaPlayer.Status.PAUSED)) {
            setCurrentlyPlaying(player);
        }
        player.play();
        player.totalDurationProperty().addListener(observable -> {
            getTotalTimeLabel().setText(totalTimeToString());
        });
        player.setOnEndOfMedia(() -> {
            if (repeatStatus.equals(Repeat.ONE)) {
                player.seek(Duration.ZERO);
                return;
            }
            playNextItem();
        });
    }

    private void stop() {
        player.stop();
        player.seek(Duration.ZERO);
    }

    private void volumeDown() {
        player.setVolume(player.getVolume() - 0.05);
    }

    private void volumeUp() {
        player.setVolume(player.getVolume() + 0.05);
    }

    private void playItemOnStep(final int i) {
        if (currentIndex + i >= 0) {
            setCurrentIndex(currentIndex + i);
        }

        if (activeListView == null) return;

        int activeListSize = activeListView.getItems().size();

        if (currentIndex < 0 || currentIndex >= activeListSize) {
            switch (repeatStatus) {
                case ONE:
                case NOT: {
                    setCurrentIndex(0);
                    activeListView.getSelectionModel().select(0);
                    stop();
                    infoLabel.setText("-=-");
                    return;
                }
                case ALL: {
                    if (currentIndex < 0) {
                        setCurrentIndex(activeListSize);
                        break;
                    }
                    setCurrentIndex(0);
                    break;
                }

            }
        }

        activeListView.getSelectionModel().select(currentIndex);
        Item item = getSelectedItem(activeListView);
        if (item == null) return;
        player.stop();
        player = getPlayerInstance(item.getUrl());
        play();
    }

    @FXML
    private void clearResult() {
        itemsList.getItems().clear();
    }

    private Item getSelectedItem(final ListView<Item> listView) {
        return listView.getSelectionModel().getSelectedItem();
    }

    private Item getFocusedItem(final ListView<Item> listView) {
        return listView.getFocusModel().getFocusedItem();
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

    private String currentTimeToString() {
        int curentTimeSeconds = (int) player.getCurrentTime().toSeconds();
        int minutes = curentTimeSeconds / 60;
        int seconds = curentTimeSeconds - minutes * 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String totalTimeToString() {
        int totalTimeSeconds = (int) player.getTotalDuration().toSeconds();
        int minutes = totalTimeSeconds / 60;
        int seconds = totalTimeSeconds - minutes * 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private MediaPlayer getPlayerInstance(final String source) {
        Media media = new Media(source);
        MediaPlayer player = new MediaPlayer(media);
        player.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
        return player;
    }
}
