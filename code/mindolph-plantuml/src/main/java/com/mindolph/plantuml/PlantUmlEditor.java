package com.mindolph.plantuml;

import com.mindolph.base.EditorContext;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.ImageScrollPane;
import com.mindolph.base.editor.BasePreviewEditor;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.StatusMsg;
import com.mindolph.base.util.CssUtils;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.constant.TextConstants;
import com.mindolph.core.search.TextLocation;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IoUtils;
import org.swiftboot.util.TextUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mindolph.base.constant.FontConstants.KEY_PUML_EDITOR;
import static com.mindolph.base.constant.FontConstants.KEY_PUML_EDITOR_MONO;
import static com.mindolph.plantuml.constant.PlantUmlConstants.DIAGRAM_KEYWORDS_START;

/**
 * @author mindolph.com@gmail.com
 */
public class PlantUmlEditor extends BasePreviewEditor implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(PlantUmlEditor.class);

    @FXML
    private ImageScrollPane previewPane;

    private final ContextMenu contextMenu = new ContextMenu();

    private final AtomicLong scrollStartTime = new AtomicLong(0);
    private final double SCROLL_SPEED_THRESHOLD = 1.75; // the threshold of scroll speed between scroll and swipe.

    private Image image;

    private final Indicator indicator = new Indicator();

    public PlantUmlEditor(EditorContext editorContext) {
        super("/editor/plant_uml_editor.fxml", editorContext, false);
        super.fileType = SupportFileTypes.TYPE_PLANTUML;
        log.info("initialize plantuml editor");

        threadPoolService = Executors.newSingleThreadExecutor();

        this.previewPane.setOnContextMenuRequested(event -> {
            log.debug("context menu requested");
            this.createContextMenu();
            contextMenu.show(previewPane, event.getScreenX(), event.getScreenY());
        });
        this.previewPane.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
        });
        this.previewPane.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) contextMenu.hide();
        });
        this.previewPane.getScalableView().scaleProperty().addListener((observable, oldValue, newValue) -> {
            EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(), new StatusMsg("%.0f%%".formatted(newValue.doubleValue() * 100)));
        });

        super.enablePageSwipe();

        this.refresh();// to set up the font
    }

    protected void createContextMenu() {
        contextMenu.getItems().clear();
        log.info("Total pages: " + indicator.totalPages);
        ToggleGroup toggleGroup = new ToggleGroup();
        for (int i = 0; i < indicator.totalPages; i++) {
            RadioMenuItem miPageX = new RadioMenuItem("Page %d: %s".formatted(i + 1, indicator.pageTitles.get(i)));
            miPageX.setToggleGroup(toggleGroup);
            miPageX.setUserData(i);
            miPageX.setGraphic(FontIconManager.getIns().getIcon(IconKey.PAGE));
            if (indicator.page == i) {
                miPageX.setSelected(true);
            }
            miPageX.setOnAction(event -> {
                indicator.page = (int) miPageX.getUserData();
                refresh(codeArea.getText());
            });
            contextMenu.getItems().add(miPageX);
        }
        MenuItem miExport = new MenuItem("Export Image as File...", FontIconManager.getIns().getIcon(IconKey.IMAGE));
        MenuItem miCopyImage = new MenuItem("Copy Image to Clipboard");
        miCopyImage.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putImage(image);
            Clipboard.getSystemClipboard().setContent(content);
        });
        MenuItem miCopyAscii = new MenuItem("Copy ASCII Image to Clipboard");
        miCopyAscii.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(this.convertPageToString(FileFormat.ATXT));
            Clipboard.getSystemClipboard().setContent(content);
        });
        MenuItem miCopyScript = new MenuItem("Copy Script to Clipboard");
        miCopyScript.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(this.convertPageToString(null));
            Clipboard.getSystemClipboard().setContent(content);
        });
        miExport.setOnAction(event -> {
            // TODO figures out why there are 2 implementations exists
//            File snapshotFile = new FileDialogBuilder().fileDialogType(FileDialogBuilder.FileDialogType.SAVE_FILE)
//                    .title("Save to").initDir(SystemUtils.getUserHome())
//                    .extensionFilters(new FileChooser.ExtensionFilter("JPG", "*.jpg"))
//                    .buildAndShow();
            File file = editorContext.getFileData().getFile();
            File snapshotFile = DialogFactory.openSaveFileDialog(getScene().getWindow(), file.getParentFile(),
                    FilenameUtils.getBaseName(file.getName()) + ".jpg",
                    new FileChooser.ExtensionFilter("Image file(jpg)", "*.jpg"));
            Image image = previewPane.getImage();
            try {
                if (snapshotFile != null) {
                    if (image == null) {
                        log.error("Image is null");
                        return;
                    }
                    log.debug("Export image: %sx%s".formatted(image.getWidth(), image.getHeight()));
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "jpg", snapshotFile);
                    log.info("Exported image to file: %s".formatted(snapshotFile));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        contextMenu.getItems().addAll(new SeparatorMenuItem(), miExport, new SeparatorMenuItem(), miCopyImage, miCopyAscii, miCopyScript);
        if (indicator.isEmpty()) contextMenu.getItems().forEach(mi -> mi.setDisable(true));
        log.debug("Context menu created with %d menu items".formatted(contextMenu.getItems().size()));
    }

    private String convertPageToString(FileFormat fileFormat) {
        String theText = codeArea.getText();
        SourceStringReader reader = new SourceStringReader(theText, "UTF-8");
        ByteArrayOutputStream utfBuffer = new ByteArrayOutputStream();
        try {
            if (fileFormat == null) {
                // return original plantuml code if no target
                BlockUml blockUml = reader.getBlocks().get(indicator.page);
                return StringUtils.join(blockUml.getDefinition(true), TextConstants.LINE_SEPARATOR);
            }
            else {
                DiagramDescription description = reader
                        .outputImage(utfBuffer, indicator.page, new FileFormatOption(fileFormat, false));
                String result = utfBuffer.toString(StandardCharsets.UTF_8);
                Pattern pattern = Pattern.compile("java\\.lang\\.\\S+?Exception");
                Matcher matcher = pattern.matcher(result);
                if (matcher.find()) {
                    throw new RuntimeException("Failed to convert page: " + matcher.group());
                }
                return result;
            }
        } catch (Exception ex) {
            log.error("Can't export image to " + fileFormat, ex);
            DialogFactory.errDialog(ex.getLocalizedMessage());
            return null;
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    protected void nextPage() {
        if (indicator.nextPage()) {
            refresh(codeArea.getText());
        }
    }

    @Override
    protected void prevPage() {
        if (indicator.prevPage()) {
            refresh(codeArea.getText());
        }
    }

    @Override
    public void applyStyles() {
        CssUtils.applyFontCss(codeArea, "/style/plantuml_syntax_template.css", KEY_PUML_EDITOR, KEY_PUML_EDITOR_MONO);
    }

    @Override
    protected void refresh(String text) {
        codeArea.refresh();
        super.refresh(text);
        this.refresh();
    }

    @Override
    public void refreshPreview(String text, Callback<Object, Void> previewConsumer) {
        indicator.reset();
        log.debug("Current page: %d".formatted(indicator.page));

        // The FileFormat uses AWT resources, so it should be run in Swing thread, otherwise it will be blocked.
        SwingUtilities.invokeLater(() -> {
            SourceStringReader reader = new SourceStringReader(codeArea.getText());

            // retrieve title info from all pages.
            indicator.pageTitles.clear();
            for (BlockUml block : reader.getBlocks()) {
                // Show error image if error occurs, but continue next page
                String errMsg = StringUtils.trim(block.getDiagram().getWarningOrError());
                if (StringUtils.contains(errMsg, "(Error)")) {
                    log.debug("encounter error in this plantuml");
                    indicator.addPageTitle(errMsg);
                    int curPage = reader.getBlocks().indexOf(block);
                    indicator.errPages.add(curPage);
                    log.debug("Found error for page: %d".formatted(curPage));
                    if (curPage != indicator.page) {
                        log.debug("skip because not current page");
                        continue; // only one error be handled
                    }
                    log.debug("Generate error image for page: %d".formatted(curPage));
                    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        DiagramDescription diagramDescription = reader.outputImage(os, curPage);
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(os.toByteArray());
                        image = new Image(byteArrayInputStream);
                        byteArrayInputStream.close();
                        Platform.runLater(() -> {
                            EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(),
                                    new StatusMsg("Something wrong with your code in page %d".formatted(curPage + 1),
                                            "See the description", image));
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    indicator.addPageTitle(this.extractDiagramTitle(block.getDefinition(false)));
                }
            }
            indicator.totalPages = reader.getBlocks().size();
            indicator.fitPage();
            log.debug("total pages %d, current page %d, error page count %d".formatted(indicator.totalPages, indicator.page, indicator.errPages.size()));
            if (indicator.isEmpty()) {
                return;// this is en empty file
            }

            if (!indicator.isCurrentPageError()) {
                Platform.runLater(() -> {
                    String title = indicator.page < indicator.pageTitles.size() ? indicator.pageTitles.get(indicator.page) : StringUtils.EMPTY;
                    EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(),
                            new StatusMsg("Page %d/%d: %s".formatted(indicator.page + 1, indicator.totalPages, title)));
                });
                log.trace(String.valueOf(reader.generateDiagramDescription()));
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    DiagramDescription diagramDescription = reader.outputImage(os, indicator.page);
                    if (diagramDescription != null) {
                        log.debug(diagramDescription.getDescription());
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(os.toByteArray());
                        image = new Image(byteArrayInputStream);
                        byteArrayInputStream.close();
                        previewConsumer.call(image);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                previewConsumer.call(null);
            }
        });
    }

    private String extractDiagramTitle(List<String> lines) {
        // TODO to be refactored for multiline title.
        List<String> prediction = lines.stream()
                .filter(string -> string.trim().startsWith("title") || string.trim().startsWith("caption")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(prediction)) {
            String title = prediction.get(0);
            if (title.startsWith("title")) {
                title = StringUtils.substringAfter(title, "title");
                if (StringUtils.isBlank(title)) {
                    // TODO
                }
                title = TextUtils.removeQuotes(title);

            }
            else {
                title = StringUtils.substringAfter(title, "caption");
            }
            return title;
        }
        else {
            return "[Unnamed]";
        }
    }

    @Override
    protected void render(Object renderObject) {
        Image image = (Image) renderObject;
        if (image != null) {
            log.debug("Render plantuml image: %s x %s".formatted(image.getWidth(), image.getHeight()));
            previewPane.setImage(image);
        }
    }

    @Override
    protected String getOutlinePattern() {
        return "(@|' )(%s|\\*\\*.+\\*\\*)".formatted(String.join("|", DIAGRAM_KEYWORDS_START));
    }

    @Override
    protected String getHeadingLevelTag() {
        return null;// no level for now
    }

    @Override
    protected int determineOutlineLevel(String heading) {
        return ArrayUtils.contains(DIAGRAM_KEYWORDS_START, heading.trim()) ? 1 : 2;
    }

    @Override
    protected String extractOutlineTitle(String heading, TextLocation location, TextLocation nextBlockLocation) {
        log.debug("extract outline title for heading: '%s'".formatted(heading));
        // extract title by cutting the diagram code block.
        if (ArrayUtils.contains(DIAGRAM_KEYWORDS_START, heading.trim())) {
            int startPos = codeArea.getAbsolutePosition(location.getEndRow(), location.getEndCol());
            int endPos = nextBlockLocation == null ? codeArea.getText().length() : codeArea.getAbsolutePosition(nextBlockLocation.getStartRow(), nextBlockLocation.getStartCol());
            String block = StringUtils.substring(codeArea.getText(), startPos, endPos);
            try {
                List<String> lines = IoUtils.readToStringList(new ByteArrayInputStream(block.getBytes(StandardCharsets.UTF_8)));
                String title = this.extractDiagramTitle(lines);
//            if ("[Unnamed]".equals(title)){
//                return heading;
//            }
                return title;
            } catch (IOException e) {
                return heading;
            }
        }
        else {
            return StringUtils.substringBetween(heading, "**").trim();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        image = null;
        previewPane.setImage(null);
    }

    public Image getImage() {
        return image;
    }

    private static class Indicator {
        int totalPages = 0;
        int page = 0;
        List<Integer> errPages;
        List<String> pageTitles;

        public void reset() {
            totalPages = 0;
//            page = 0; // page is for global indication, no reset
            errPages = new ArrayList<>();
            pageTitles = new ArrayList<>();
        }

        public void fitPage() {
            page = Math.min(page, Math.max(0, totalPages - 1));
        }

        public boolean isCurrentPageError() {
            return errPages.contains(page);
        }

        public boolean isEmpty() {
            return totalPages <= 0;
        }

        public boolean nextPage() {
            return ++page < totalPages;
        }

        public boolean prevPage() {
            return --page >= 0;
        }

        public void addPageTitle(String pageTitle) {
            if (this.pageTitles == null) this.pageTitles = new ArrayList<>();
            this.pageTitles.add(pageTitle);
        }
    }
}
