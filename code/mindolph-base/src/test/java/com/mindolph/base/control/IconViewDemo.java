package com.mindolph.base.control;

import com.mindolph.base.control.snippet.ImageSnippet;
import com.mindolph.mfx.util.FxImageUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class IconViewDemo implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(IconViewDemo.class);

    @FXML
    private IconView iconView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            ObservableList<Image> images = FXCollections.observableArrayList();
            images.add(null); // for empty icon
            for (int i = 0; i < 100; i++) {
                Image img = FxImageUtils.readImageFromResource("/img_small.jpg");
                images.add(img);
            }
            List<ImageSnippet> snippets = images.stream().map(ImageSnippet::new).toList();
            snippets.forEach(snippet -> snippet.title("default title"));
            iconView.setItems(FXCollections.observableList(snippets));
//            iconView.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//                System.out.println(newValue);
//            });
//            iconView.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
//                log.info(BoundsUtils.boundsInString(newValue));
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
