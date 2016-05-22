package com.omplayer.controller;

import com.omplayer.player.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import lombok.Data;

import java.net.URL;
import java.util.ResourceBundle;

@Data
public class RootLayoutController implements Initializable {
    private MainApp mainApp;

    @FXML
    private AnchorPane rootAnchorPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //here will be something. maybe
    }
}
