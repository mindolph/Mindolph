package com.mindolph.mindmap.view;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.mindolph.base.BaseView;
import com.mindolph.mindmap.MindMapView;
import com.mindolph.mindmap.event.MindmapEvents;
import com.mindolph.mindmap.model.NoteEditorData;
import com.mindolph.mindmap.model.TopicNode;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.time.Duration;

/**
 * View to show the attributes of a topic, including note, link, etc.
 *
 * @author
 * @since 1.14
 */
public class AttributesView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(AttributesView.class);

    @FXML
    private NotePanel notePanel;
    @FXML
    private TextField tfUrl;
    //    @FXML
//    private Button btnEmoticon;
//    @FXML
//    private TextField tfFile;

    private TopicNode topic;

    private MindMapView mindMapView; // used to notify the mind map view to save file when note changes.

    // used to control the merging of editing history.
    private final EventSource<String> inputtingSource = new EventSource<>();

    public AttributesView() {
        super("/view/attributes_view.fxml", false);
        // reduce the frequency of history source to avoid too many history records when editing note.
        inputtingSource.reduceSuccessions((s, s2) -> s2, Duration.ofMillis(200))
                .subscribe(s -> {
                    mindMapView.onMindMapModelChanged(true);
                    mindMapView.updateStatusBarForTopic(this.topic);
                });

        tfUrl.focusedProperty().addListener((observable, oldValue, isFocused) -> {
            // lost focus
            if (isFocused != oldValue && !isFocused) {
                Extra<?> link = topic.getExtras().get(Extra.ExtraType.LINK);
                boolean noChanges = (link == null && StringUtils.isEmpty(tfUrl.getText())) ||
                        (link != null && Strings.CS.equals(tfUrl.getText(), link.getAsString()));
                if (noChanges) {
                    return;
                }
                else {
                    try {
                        if (StringUtils.isBlank(tfUrl.getText())) {
                            log.debug("Remove file link attribute");
                            topic.removeExtra(Extra.ExtraType.LINK);
                            MindmapEvents.notifyAttributesChangeEvent(this.topic);
                        }
                        else {
                            topic.setExtra(new ExtraLink(tfUrl.getText()));
                            MindmapEvents.notifyAttributesChangeEvent(this.topic);

                        }
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

//        tfFile.focusedProperty().addListener((observable, oldValue, newValue) -> {
//            // lost focus
//            if (oldValue!=newValue && !newValue) {
//                try {
//                    this.topic.setExtra(new ExtraFile(tfFile.getText()));
//                    MindmapEvents.notifyAttributesChange(this.topic);
//                } catch (URISyntaxException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
    }

    public void loadAttributes(TopicNode topic) {
        super.loading = true;
        this.topic = topic;
        MindmapEvents.subscribeAttributesChangeEvent(topic, v -> {
            inputtingSource.push(null);
        });
        // listen for the original topic changes.
        MindmapEvents.subscribeTopicChangeEvent(topic, t -> {
            this.topic = topic;
            this.loadAttributes();
        });
        // no need since it's unable to save note directly in the panel for now.
        // only one listener exists even call this subscribe method multiple times.
//        MindmapEvents.subscribeNoteSaveEvent(topic, newNoteData -> {
//            log.debug(newNoteData.getText());
//            log.debug("Notify mmd editor to save file");
//            MindmapEvents.notifyMmdSaveEvent(this.mindMapView);
//            mindMapView.updateStatusBarForTopic(topic);
//        });
        this.loadAttributes();
        super.loading = false;
    }

    private void loadAttributes() {
        if (topic != null) {
            // NOTE attribute
            ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
            if (note != null) {
                notePanel.loadData(topic, new NoteEditorData(note.getValue(), note.isEncrypted(), null, note.getHint()), false);
            }
            else {
                notePanel.loadData(topic, new NoteEditorData("", false, null, null), false);
            }
            // URL attribute
            tfUrl.setDisable(false);
            Extra<?> link = topic.getExtras().get(Extra.ExtraType.LINK);
            if (link != null) {
                tfUrl.setText(link.getValue().toString());
            }
            else {
                tfUrl.setText("");
            }
            // File attribute
//            Extra<?> file = topic.getExtras().get(Extra.ExtraType.FILE);
//            if (file != null) {
//                tfFile.setText(file.getValue().toString());
//            }
//            else {
//                tfFile.setText("");
//            }
            // Emoticon attribute
//            String iconName = AttributeUtils.getIconAttribute(topic);
//            if (StringUtils.isNotBlank(iconName)) {
//                Image icon = EmoticonService.getInstance().getIcon(iconName);
//                ImageView iv = new ImageView(icon);
//                btnEmoticon.setGraphic(iv);
//            }
        }
        else {
            notePanel.loadData(null, null, false);
            tfUrl.setDisable(true);
            tfUrl.setText(StringUtils.EMPTY);
//            tfFile.setText(StringUtils.EMPTY);
        }
    }


    public NotePanel getNotePanel() {
        return notePanel;
    }

    public void setMindMapView(MindMapView mindMapView) {
        this.mindMapView = mindMapView;
    }
}
