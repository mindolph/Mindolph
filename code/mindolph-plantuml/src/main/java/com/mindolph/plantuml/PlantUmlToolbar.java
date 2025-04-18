package com.mindolph.plantuml;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import org.apache.commons.lang3.StringUtils;
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
    private Button btnActor;
    @FXML
    private Button btnRectangle;
    @FXML
    private Button btnOutline1;
    @FXML
    private Button btnOutline2;
    @FXML
    private Button btnOutline3;

    private final PlantUmlCodeArea pumlCodeArea;

    public PlantUmlToolbar(PlantUmlCodeArea pumlCodeArea) {
        this.pumlCodeArea = pumlCodeArea;
        FxmlUtils.loadUri("/plantuml_toolbar.fxml", this);

        this.getStyleClass().add("no-scroll-bar");
        this.getStyleClass().add("no-border");


        FontIconManager fim = FontIconManager.getIns();
        btnUml.setGraphic(fim.getIcon(IconKey.AT));
        btnEntity.setGraphic(fim.getIcon(IconKey.ENTITY));
        btnActor.setGraphic(fim.getIcon(IconKey.HUMAN));
        btnRectangle.setGraphic(fim.getIcon(IconKey.SQUARE));
        btnOutline1.setGraphic(fim.getIcon(IconKey.H1));
        btnOutline2.setGraphic(fim.getIcon(IconKey.H2));
        btnOutline3.setGraphic(fim.getIcon(IconKey.H3));

        btnUml.setOnAction(this);
        btnEntity.setOnAction(this);
        btnActor.setOnAction(this);
        btnRectangle.setOnAction(this);
        btnOutline1.setOnAction(this);
        btnOutline2.setOnAction(this);
        btnOutline3.setOnAction(this);
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
        else if (node == btnActor) {
            this.insertActorTag();
        }
        else if (node == btnRectangle) {
            this.insertRectangleTag();
        }
        else if (node == btnOutline1) {
            this.insertOutlineTag(1);
        }
        else if (node == btnOutline2) {
            this.insertOutlineTag(2);
        }
        else if (node == btnOutline3) {
            this.insertOutlineTag(3);
        }
        pumlCodeArea.requestFocus();
    }

    private void insertUmlTag() {
        pumlCodeArea.insertText("""
                @startuml
                title 'This is a PlantUML diagram'
                
                @enduml""");
    }

    private void insertEntityTag() {
        pumlCodeArea.insertText("""
                entity "My Entity" as my_entity {
                
                }""");
    }

    private void insertActorTag() {
        pumlCodeArea.insertText("""
                actor "My Actor" as my_actor {
                
                }""");
    }

    private void insertRectangleTag() {
        pumlCodeArea.insertText("""
                rectangle "My Rectangle" as my_rect {
                
                }""");
    }

    private void insertOutlineTag(int level) {
        String levelChars = StringUtils.repeat('*', level);
        pumlCodeArea.insertText("' %s outline title %s\n".formatted(levelChars, levelChars));
    }

}
