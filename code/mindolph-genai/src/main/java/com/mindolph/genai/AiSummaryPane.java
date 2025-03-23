package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.InputBuilder;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.llm.LlmService;
import com.mindolph.base.genai.llm.OutputParams;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.constant.GenAiConstants.ActionType;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.ClipBoardUtils;
import com.mindolph.mfx.util.ControlUtils;
import com.mindolph.mfx.util.PaneUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindolph.core.constant.GenAiConstants.FILE_OUTPUT_MAPPING;

/**
 * @since 1.11
 */
public class AiSummaryPane extends BaseAiPane {

    private static final Logger log = LoggerFactory.getLogger(AiSummaryPane.class);

    @FXML
    private TextArea taOutput;
    @FXML
    private ProgressBar pbWaiting;
    @FXML
    private Button btnCopy;
    @FXML
    private Button btnSummarize;
    @FXML
    private Button btnStop;
    @FXML
    private HBox hbDone;
    @FXML
    private HBox hbGenerating;

    // workaround bond to the editor to control it's state.
    private final Node bondEditor;

    public AiSummaryPane(Object editorId, String fileType, String inputText, Node bondEditor) {
        super("/genai/ai_summary_pane.fxml", editorId, fileType);
        this.bondEditor = bondEditor;

        this.toggleComponents(false);
        NodeUtils.disable(btnCopy); // disable copy button for the first time.

        lbTitle.setText("Summarize selected content by %s".formatted(LlmConfig.getIns().getActiveAiProvider()));
        btnCopy.setGraphic(FontIconManager.getIns().getIcon(IconKey.COPY));
        btnSummarize.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEND));
        btnClose.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.CANCEL);
        });
        btnStop.setOnAction(event -> {
            this.toggleComponents(false);
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.STOP);
        });
        btnSummarize.setOnAction(event -> {
            starToSummarize(inputText);
        });
        btnCopy.setOnAction(event -> {
            if (StringUtils.isNotBlank(taOutput.getText())) {
                ClipBoardUtils.textToClipboard(taOutput.getText());
                lbMsg.setText("%d bytes copied to Clipboard".formatted(taOutput.getText().length()));
            }
        });

        ControlUtils.escapableControls(() -> GenAiEvents.getIns().emitActionEvent(editorId, ActionType.CANCEL), taOutput);

        PaneUtils.escapablePanes(() -> GenAiEvents.getIns().emitActionEvent(editorId, ActionType.CANCEL),
                this, hbDone, hbGenerating);

        this.requestFocus();
    }

    private void starToSummarize(String inputText) {
//        ModelMeta modelMeta = LlmConfig.getIns().preferredModelForActiveLlmProvider();
        if (cbModel.getValue() == null) {
            DialogFactory.warnDialog("Please select a model to summarize your selected content.");
            return;
        }
        ModelMeta modelMeta = cbModel.getValue().getValue();
        this.toggleComponents(true);
        taOutput.clear();
        log.info("Start to summarize with model: '%s'".formatted(modelMeta.name()));
        lbMsg.setText(StringUtils.EMPTY);
        bondEditor.setDisable(true);
        Input input = new InputBuilder().model(modelMeta.name()).text(inputText).temperature(0.5f)
                .outputLanguage(cbLanguage.getValue().getKey())
                .maxTokens(modelMeta.maxTokens()).outputAdjust(null).isRetry(false).isStreaming(true)
                .createInput();
        LlmService.getIns().summarize(input, new OutputParams(input.outputAdjust(), FILE_OUTPUT_MAPPING.get(fileType), input.outputLanguage()),
                streamToken -> {
                    if (streamToken.isError()) {
                        log.warn("error from streaming: {}", streamToken);
                        Platform.runLater(() -> onStop(streamToken.text()));
                    }
                    else {
                        // accept streaming output (even with `stop` one).
                        if (streamToken.isStop()) {
                            Platform.runLater(() -> {
                                onStop("Done with %d tokens.".formatted(streamToken.outputTokens()));
                            });
                        }
                        else {
                            if (log.isTraceEnabled()) log.trace("append text: {}", streamToken.text());
                            Platform.runLater(() -> taOutput.appendText(streamToken.text()));
                        }
                    }
                });

    }

    public void onStop(String reason) {
        btnSummarize.setText("Re-summarize");
        lbMsg.setText(reason);
        toggleComponents(false);
        bondEditor.setDisable(false);
    }

    @Override
    protected void toggleComponents(boolean isGenerating) {
        super.toggleComponents(isGenerating);
        pbWaiting.setVisible(isGenerating);
        if (isGenerating)
            NodeUtils.disable(btnCopy, btnSummarize);
        else {
            NodeUtils.enable(btnCopy, btnSummarize);
        }
        hbDone.setVisible(!isGenerating);
        hbDone.setManaged(!isGenerating);
        hbGenerating.setVisible(isGenerating);
        hbGenerating.setManaged(isGenerating);
    }
}
