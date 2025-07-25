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
import javafx.scene.layout.VBox;
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

    @FXML
    private VBox vbToolbar;
    private PlantUmlToolbar plantUmlToolbar;

    private final ContextMenu contextMenu = new ContextMenu();

    private final AtomicLong scrollStartTime = new AtomicLong(0);
    private final double SCROLL_SPEED_THRESHOLD = 1.75; // the threshold of scroll speed between scroll and swipe.

    private final Indicator indicator = new Indicator();

    // used to extract outline title from comment.
    private final Pattern extractingPattern = Pattern.compile("[\\*]+(.+?)[\\*]+");

    private Image image;

    private boolean isAutoSwitch = true;

    public PlantUmlEditor(EditorContext editorContext) {
        super("/editor/plant_uml_editor.fxml", editorContext, false);
        super.fileType = SupportFileTypes.TYPE_PLANTUML;
        log.info("initialize plantuml editor");

        threadPoolService = Executors.newSingleThreadExecutor();

        vbToolbar.getChildren().add(new PlantUmlToolbar((PlantUmlCodeArea) super.codeArea));

        // auto switch preview page
        super.codeArea.currentParagraphProperty().addListener((observable, oldValue, newValue) -> {
            int currentRow = newValue;
            if (isAutoSwitch) {
                if (indicator.toPageByRow(currentRow)) {
                    log.info("Change page to " + indicator.page);
                    refresh(codeArea.getText());
                }
            }
        });

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
            RadioMenuItem miPageX = new RadioMenuItem("Page %d: %s".formatted(i + 1, indicator.pages.get(i).title));
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
                    if (image.isError()) {
                        log.error("Image contains error: " + image.getException());
                        return;
                    }
                    log.debug("Export image: %sx%s".formatted(image.getWidth(), image.getHeight()));
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "jpg", snapshotFile);
                    log.info("Exported image to file: %s".formatted(snapshotFile));
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        });
        contextMenu.getItems().addAll(new SeparatorMenuItem(), miExport, new SeparatorMenuItem(), miCopyImage, miCopyAscii, miCopyScript);
        if (indicator.isEmpty()) contextMenu.getItems().forEach(mi -> mi.setDisable(true));
        log.debug("Context menu created with %d menu items".formatted(contextMenu.getItems().size()));
    }

    private String convertPageToString(FileFormat fileFormat) {
        String theText = codeArea.getText();
        SourceStringReader reader = new SourceStringReader(theText, StandardCharsets.UTF_8);
        try (ByteArrayOutputStream utfBuffer = new ByteArrayOutputStream()) {
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
            indicator.pages.clear();
            for (BlockUml block : reader.getBlocks()) {
                int startRow = block.getData().getFirst().getLocation().getPosition();
                int endRow = block.getData().getLast().getLocation().getPosition();
                log.debug("Page starts from row %d to row %d".formatted(startRow, endRow));
                // Show error image if error occurs, but continue next page
                String errMsg = StringUtils.trim(block.getDiagram().getWarningOrError());
                if (StringUtils.contains(errMsg, "(Error)")) {
                    log.debug("encounter error in this plantuml");
                    indicator.addPage(new Page(errMsg, startRow, endRow));
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
                        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(os.toByteArray())) {
                            image = new Image(byteArrayInputStream);
                        } catch (Exception e) {
                            log.error(e.getLocalizedMessage(), e);
                        }
                        Platform.runLater(() -> {
                            EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(),
                                    new StatusMsg("Something wrong with your code in page %d".formatted(curPage + 1),
                                            "See the description", image));
                        });
                    } catch (IOException e) {
                        log.error(e.getLocalizedMessage(), e);
                    }
                }
                else {
                    indicator.addPage(new Page(this.extractDiagramTitle(block.getDefinition(false)), startRow, endRow));
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
                    String title = indicator.page < indicator.pages.size() ? indicator.pages.get(indicator.page).title : StringUtils.EMPTY;
                    EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(),
                            new StatusMsg("Page %d/%d: %s".formatted(indicator.page + 1, indicator.totalPages, title)));
                });
                log.trace(String.valueOf(reader.generateDiagramDescription()));
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    DiagramDescription diagramDescription = reader.outputImage(os, indicator.page);
                    if (diagramDescription != null) {
                        log.debug(diagramDescription.getDescription());
                        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(os.toByteArray())) {
                            image = new Image(byteArrayInputStream);
                        } catch (IOException e) {
                            log.error(e.getLocalizedMessage(), e);
                        }
                        previewConsumer.call(image);
                    }
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
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
        return "(@|'[\\s]*)(%s|[\\*]+.+[\\*]+?)".formatted(String.join("|", DIAGRAM_KEYWORDS_START));
    }

    @Override
    protected String getHeadingLevelTag() {
        return null;// no level for now
    }

    @Override
    protected int determineOutlineLevel(String heading) {
        return ArrayUtils.contains(DIAGRAM_KEYWORDS_START, heading.trim()) ? 1
                : com.mindolph.mfx.util.TextUtils.countInStarting(heading.trim(), "*") + 1;
    }

    @Override
    protected String extractOutlineTitle(String heading, TextLocation location, TextLocation nextBlockLocation) {
        log.debug("extract outline title for heading: '%s'".formatted(heading));
        // extract title by cutting the diagram code block.
        if (ArrayUtils.contains(DIAGRAM_KEYWORDS_START, heading.trim())) {
            int startPos = codeArea.getAbsolutePosition(location.getEndRow(), location.getEndCol());
            int endPos = nextBlockLocation == null ? codeArea.getText().length() : codeArea.getAbsolutePosition(nextBlockLocation.getStartRow(), nextBlockLocation.getStartCol());
            String block = StringUtils.substring(codeArea.getText(), startPos, endPos);
            try (ByteArrayInputStream bains = new ByteArrayInputStream(block.getBytes(StandardCharsets.UTF_8))) {
                List<String> lines = IoUtils.readToStringList(bains);
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
            Matcher matcher = extractingPattern.matcher(heading);
            return matcher.find() ? matcher.group(1) : heading;
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

    public void setAutoSwitch(boolean autoSwitch) {
        this.isAutoSwitch = autoSwitch;
    }

    private static class Indicator {
        int totalPages = 0;
        int page = 0;
        List<Integer> errPages;
        List<Page> pages;

        public void reset() {
            totalPages = 0;
            errPages = new ArrayList<>();
            pages = new ArrayList<>();
            // page = 0; // page is for global indication, DO NOT reset
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

        public boolean toPageByRow(int row) {
            if (pages == null) return false;
            for (Page p : pages) {
                if (row >= p.startRow && row <= p.endRow) {
                    int newPage = pages.indexOf(p);
                    // only different page will return true.
                    if (newPage != page) {
                        page = newPage;
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }

        public void addPage(Page page) {
            this.pages.add(page);
        }
    }

    private record Page(String title, int startRow, int endRow) {
    }
}
