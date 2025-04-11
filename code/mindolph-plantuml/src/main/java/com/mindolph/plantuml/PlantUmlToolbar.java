package com.mindolph.plantuml;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 * @since 1.12.1
 */
public class PlantUmlToolbar extends ScrollPane implements EventHandler<ActionEvent> {

    private static final Logger log = LoggerFactory.getLogger(PlantUmlToolbar.class);

    @FXML
    private Button btnUml;
    @FXML
    private Button btnEntity;
    @FXML
    private Button btnOutline;


    private final PlantUmlCodeArea pumlCodeArea;

    public PlantUmlToolbar(PlantUmlCodeArea pumlCodeArea) {
        this.pumlCodeArea = pumlCodeArea;
        FxmlUtils.loadUri("/plantuml_toolbar.fxml", this);

        this.getStyleClass().add("no-scroll-bar");
        this.getStyleClass().add("no-border");


        FontIconManager fim = FontIconManager.getIns();
        btnUml.setGraphic(fim.getIcon(IconKey.AT));
        btnEntity.setGraphic(fim.getIcon(IconKey.ENTITY));
        btnOutline.setGraphic(fim.getIcon(IconKey.OUTLINE));

        btnUml.setOnAction(this);
        btnEntity.setOnAction(this);
        btnOutline.setOnAction(this);
    }


    @Override
    public void handle(ActionEvent event) {
        Object node = event.getSource();
        if (node == btnUml) {
            this.insertUmlTag();
        }
        else if (node == btnEntity) {
            this.insertEntityTag();
        }
        else if (node == btnOutline) {
            this.insertOutlineTag();
        }
        pumlCodeArea.requestFocus();
    }

    private void insertUmlTag() {
        pumlCodeArea.insertText("""
                @startuml
                title 'this is a PlantUML diagram'
                
                @enduml""");
    }

    private void insertEntityTag() {
        pumlCodeArea.insertText("""
                entity 'my_entity' as my_entity{
                
                }""");
    }

    private void insertOutlineTag() {
        pumlCodeArea.insertText("' * outline title *");
    }

}
