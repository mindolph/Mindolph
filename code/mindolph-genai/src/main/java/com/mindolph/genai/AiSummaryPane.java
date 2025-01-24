package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.llm.LlmService;
import com.mindolph.base.genai.llm.OutputParams;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.constant.GenAiConstants.ActionType;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.mfx.util.ClipBoardUtils;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindolph.core.constant.GenAiConstants.FILE_OUTPUT_MAPPING;

/**
 * @since 1.11
 */
public class AiSummaryPane extends StackPane {

    private static final Logger log = LoggerFactory.getLogger(AiSummaryPane.class);

    @FXML
    private Label lbTitle;
    @FXML
    private TextArea taOutput;
    @FXML
    private ProgressBar pbWaiting;
    @FXML
    private Label lbIcon;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnCopy;
    @FXML
    private Button btnReSummarize;
    @FXML
    private Button btnStop;
    @FXML
    private HBox hbDone;
    @FXML
    private HBox hbGenerating;
    @FXML
    private Label lbMsg;

    private final Object editorId;
    private final String fileType;

    public AiSummaryPane(Object editorId, String fileType, String inputText) {
        this.editorId = editorId;
        this.fileType = fileType;
        FxmlUtils.loadUri("/genai/ai_summary_pane.fxml", this);
        lbIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.MAGIC));
        btnClose.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLOSE));

        toggleButtons(true);
        btnClose.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.ABORT);
        });
        btnStop.setOnAction(event -> {
            this.toggleButtons(false);
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.STOP);
        });
        btnReSummarize.setOnAction(event -> {
            this.toggleButtons(true);
            taOutput.clear();
            starTotSummarize(inputText);
        });
        btnCopy.setOnAction(event -> {
            if (StringUtils.isNotBlank(taOutput.getText())) ClipBoardUtils.textToClipboard(taOutput.getText());
        });

//        this.setOnKeyReleased(event -> {
//            if (KeyCode.ESCAPE == event.getCode()) {
//                GenAiEvents.getIns().emitActionEvent(editorId, ActionType.CANCEL);
//            }
//        });
        this.requestFocus();

        // listeners
        GenAiEvents.getIns().subscribeSummarizeEvent(editorId, input -> {
            LlmService.getIns().summarize(input, new OutputParams(input.outputAdjust(), FILE_OUTPUT_MAPPING.get(fileType)),
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
        });

        // start to summarize
        starTotSummarize(inputText);
    }

    private void starTotSummarize(String inputText) {
        ModelMeta modelMeta = LlmConfig.getIns().preferredModelForActiveLlmProvider();
        log.info("Start to summarize with model: '%s'".formatted(modelMeta.name()));
        lbTitle.setText("Summarize selected content by %s %s".formatted(LlmConfig.getIns().getActiveAiProvider(), modelMeta.name()));
        lbMsg.setText(StringUtils.EMPTY);
        GenAiEvents.getIns().emitSummarizeEvent(editorId, new Input(modelMeta.name(), inputText,
                0.5f, modelMeta.maxTokens(),null, false, true));
    }

    public void onStop(String reason) {
        lbMsg.setText(reason);
        toggleButtons(false);
    }

    private void toggleButtons(boolean isGenerating) {
        pbWaiting.setVisible(isGenerating);
        if (isGenerating)
            NodeUtils.disable(btnCopy, btnReSummarize);
        else
            NodeUtils.enable(btnCopy, btnReSummarize);
        hbDone.setVisible(!isGenerating);
        hbDone.setManaged(!isGenerating);
        hbGenerating.setVisible(isGenerating);
        hbGenerating.setManaged(isGenerating);
    }
}
