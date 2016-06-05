package com.omplayer.controller;

import com.omplayer.model.Item;
import com.omplayer.parser.MediaParser;
import com.omplayer.player.MainApp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import lombok.Data;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


@Data
public class ListController implements Initializable {
    private MainApp mainApp;
    @FXML
    private AnchorPane listAnchorPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button addToFavorites;
    @FXML
    private Button removeFromFavoritesButton;
    @FXML
    private Button clearResultButton;
    @FXML
    private ListView<Item> searchItemsListView;
    @FXML
    private ListView<Item> favoriteItemsListView;
    @FXML
    private TextField searchField;

    private MediaParser parser = new MediaParser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeAnchors();
        initializeSearchField();
        initializeSearchItemsList();
        initializeFavoriteItemsList();
        initializeAddToFavorites();
        initializeRemoveFromFavorites();
    }

    public void initializeAccelerators() {

//        addAccessorTo(
//                getPrevItemButton(),
//                KeyCodeCombination.valueOf("A"));
//        addAccessorTo( getAddToFavorites(), KeyCodeCombination.valueOf("Ctrl+A"));
        getSearchField().getScene().getAccelerators().put(KeyCombination.valueOf("Ctrl+F"), () -> getSearchField().requestFocus());
    }

    private void addAccessorTo(Button button, KeyCombination combination) {
        if(button.getScene() == null) System.out.println("NULL");
        button.getScene().getAccelerators().put(combination, button::fire);
    }

    private void initializeAnchors() {
        AnchorPane.setTopAnchor(getListAnchorPane(), 100.0);
        AnchorPane.setBottomAnchor(getListAnchorPane(), 0.0);
        AnchorPane.setLeftAnchor(getListAnchorPane(), 0.0);
        AnchorPane.setRightAnchor(getListAnchorPane(), 0.0);

    }

    @FXML
    private void initializeSearchField() {
        getSearchField().setOnAction(event -> {
            try {
                searchFromMp3cc(
                        "http://mp3.cc/search/f/" +
                                URLEncoder.encode(getSearchField().getText().trim(), "UTF-8"));
                getSearchItemsListView().scrollTo(0);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            setActiveListView(getSearchItemsListView());
        });
    }

    private void initializeSearchItemsList() {
        getSearchItemsListView().setItems(FXCollections.emptyObservableList());
        getSearchItemsListView().setOnMouseClicked(event -> twoClickPlay(event, getSearchItemsListView()));
//        searchItemsListView.setCellFactory(lv -> {
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
//            deleteItem.setOnAction(event -> searchItemsListView.getItems().remove(cell.getItem()));
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
            getFavoriteItemsListView().setItems(FXCollections.observableArrayList(list));

        } catch (IOException e) {
            e.printStackTrace();
        }

        getFavoriteItemsListView().setOnMouseClicked(event -> twoClickPlay(event, getFavoriteItemsListView()));
    }

    private void initializeAddToFavorites() {

        getAddToFavorites().setOnAction(event -> {

            Item item = getFocusedItem(getSearchItemsListView());
            getFavoritesList().add(item);

            refreshFavoritesList();
        });
    }

    private void initializeRemoveFromFavorites() {
        getRemoveFromFavoritesButton().setOnAction(event -> {
            getFavoritesList().remove(getFocusedItem(getFavoriteItemsListView()));

            refreshFavoritesList();
        });
    }

    private void searchFromMp3cc(final String searchRequest) {  // FIXME: 18.05.16 Bottleneck
        ObservableList<Item> list = FXCollections.observableArrayList();

        try {
            list.addAll(parser.getMP3(searchRequest)
                    .parallelStream().collect(Collectors.toList()));
            searchItemsListView.setItems(list);
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

    @FXML
    private void clearResult() {
        getSearchItemsListView().getItems().clear();
    }

    private void refreshFavoritesList() {
        List<Item> items = getFavoriteItemsListView().getItems();
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

    private void twoClickPlay(final MouseEvent event, final ListView<Item> list) {
        if (event.getClickCount() == 2) {
            Item item = list.getSelectionModel().getSelectedItem();
            setCurrentIndex(list.getSelectionModel().getSelectedIndex());

            if (getPlayer() != null) {
                getPlayer().stop();
            }

            setPlayer(getPlayerInstance(item.getUrl()));
            setActiveListView(list);
            play();
        }
    }

    private void play() {
        getMainApp().play();
    }

    private void stop() {
        getMainApp().stop();
    }

    private List<Item> getFavoritesList() {
        return getFavoriteItemsListView().getItems();
    }

    private MediaPlayer getPlayerInstance(final String url) {
        return getMainApp().getPlayerInstance(url);
    }

    private Item getSelectedItem(final ListView<Item> listView) {
        return listView.getSelectionModel().getSelectedItem();
    }

    private Item getFocusedItem(final ListView<Item> listView) {
        return listView.getFocusModel().getFocusedItem();
    }

    private int getCurrentIndex() {
        return getMainApp().getCurrentIndex();
    }

    public MediaPlayer getPlayer() {
        return getMainApp().getPlayer();
    }

    private void setCurrentIndex(int currentIndex) {
        getMainApp().setCurrentIndex(currentIndex);
    }

    public void setActiveListView(final ListView<Item> activeListView) {
        getMainApp().setActiveListView(activeListView);
    }

    public void setPlayer(final MediaPlayer player) {
        getMainApp().setPlayer(player);
    }
}
