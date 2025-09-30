package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A customized selector for AI agents.
 * call reloadAgents() to load agents from user preferences.
 *
 * @since 1.13.1
 */
public class AgentSelector extends ComboBox<Pair<String, AgentMeta>> {

    private static final Logger log = LoggerFactory.getLogger(AgentSelector.class);

    public AgentSelector() {
        super.setPromptText("Choose an Agent");
        super.setCellFactory(comboBox -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<String, AgentMeta> item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    FXMLLoader fxmlLoader = FxmlUtils.loadUri("/genai/agent_item.fxml", new ItemController(item));
                    Node root = fxmlLoader.getRoot();
                    if (StringUtils.isNotBlank(item.getValue().getDescription())) {
                        this.setTooltip(new Tooltip(item.getValue().getDescription()));
                    }
                    setGraphic(root);
                }
                else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });
    }

    public void reloadAgents() {
        Pair<String, AgentMeta> selectedItem = this.getSelectionModel().getSelectedItem();
        String selectedAgentId = null;
        if (selectedItem != null) {
            selectedAgentId = selectedItem.getKey();
        }
        this.getItems().clear();
        Map<String, AgentMeta> agentMap = LlmConfig.getIns().loadAgents();
        this.getItems().addAll(agentMap.values().stream().map(agentMeta -> new Pair<>(agentMeta.getId(), agentMeta)).toList());
        if (StringUtils.isNotBlank(selectedAgentId)) {
            AgentMeta agentMeta = agentMap.get(selectedAgentId);
            if (agentMeta != null) {
                log.debug("Reload agent: %s".formatted(agentMeta.getName()));
                this.getSelectionModel().select(new Pair<>(selectedAgentId, agentMeta));
            }
        }
    }

    private static class ItemController extends HBox implements Initializable {

        private final Pair<String, AgentMeta> item;

        @FXML
        private Label lblIcon;

        @FXML
        private Label lblName;

        public ItemController(Pair<String, AgentMeta> item) {
            this.item = item;
        }

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            lblIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEN_AI));
            AgentMeta am = item.getValue();
            lblName.setText("%s (%s : %s)".formatted(am.getName(), am.getChatProvider().getDisplayName(), am.getChatModel()));
        }
    }

}
