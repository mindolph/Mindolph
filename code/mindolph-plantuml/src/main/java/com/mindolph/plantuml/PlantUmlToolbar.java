package com.mindolph.plantuml;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.ExtCodeArea;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.I18nHelper;

/**
 * @author mindolph.com@gmail.com
 * @since 1.12.1
 */
public class PlantUmlToolbar extends ScrollPane implements EventHandler<ActionEvent> {

    private static final Logger log = LoggerFactory.getLogger(PlantUmlToolbar.class);

    private final I18nHelper i18n = I18nHelper.getInstance();

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
    @FXML
    private Button btnLineComment;
    @FXML
    private Button btnBlockComment;

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
        btnLineComment.setGraphic(fim.getIcon(IconKey.COMMENT));
        btnBlockComment.setGraphic(fim.getIcon(IconKey.BLOCK_COMMENT));

        btnUml.setOnAction(this);
        btnEntity.setOnAction(this);
        btnActor.setOnAction(this);
        btnRectangle.setOnAction(this);
        btnOutline1.setOnAction(this);
        btnOutline2.setOnAction(this);
        btnOutline3.setOnAction(this);
        btnLineComment.setOnAction(this);
        btnBlockComment.setOnAction(this);
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
        else if (node == btnLineComment) {
            this.insertLineComment();
        }
        else if (node == btnBlockComment) {
            this.insertBlockComment();
        }
        pumlCodeArea.requestFocus();
    }

    private void insertUmlTag() {
        pumlCodeArea.applyTargetReplacement("""
                @startuml
                title "%s"
                ⨁
                right footer "%s"
                @enduml""".formatted(i18n.get("plantuml.snippet.title.default"), i18n.get("plantuml.template.generated.by")));
    }

    private void insertEntityTag() {
        pumlCodeArea.applyTargetReplacement("""
                entity "%s" as my_entity {
                ⨁
                }""".formatted(i18n.get("plantuml.snippet.entity.default")));
    }

    private void insertActorTag() {
        pumlCodeArea.applyTargetReplacement("""
                actor "%s" as my_actor
                ⨁
                """.formatted(i18n.get("plantuml.snippet.actor.default")));
    }

    private void insertRectangleTag() {
        pumlCodeArea.applyTargetReplacement("""
                rectangle "%s" as my_rect {
                ⨁
                }""".formatted(i18n.get("plantuml.snippet.rectangle.default")));
    }

    private void insertOutlineTag(int level) {
        String levelChars = StringUtils.repeat('*', level);
        String snippet = "' %s ⨁ %s".formatted(levelChars, levelChars);
        pumlCodeArea.applyTargetReplacement(snippet);
    }

    private void insertLineComment() {
        pumlCodeArea.addOrTrimHeadToParagraphsIfAdded(new ExtCodeArea.Replacement("' "));
    }

    private void insertBlockComment() {
        pumlCodeArea.applyTargetReplacement("""
                /'
                ⨁
                '/""");
    }

}
