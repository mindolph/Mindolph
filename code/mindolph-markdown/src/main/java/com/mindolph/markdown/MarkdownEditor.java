package com.mindolph.markdown;


import com.mindolph.base.EditorContext;
import com.mindolph.base.Env;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.ShortcutManager;
import com.mindolph.base.constant.FontConstants;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.ExtCodeArea.Replacement;
import com.mindolph.base.control.SearchableCodeArea;
import com.mindolph.base.editor.BasePreviewEditor;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.NotificationType;
import com.mindolph.base.event.OpenFileEvent;
import com.mindolph.base.event.StatusMsg;
import com.mindolph.base.print.PrinterManager;
import com.mindolph.base.util.FxImageUtils;
import com.mindolph.base.util.GeometryConvertUtils;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.template.HtmlBuilder;
import com.mindolph.core.util.FileNameUtils;
import com.mindolph.markdown.constant.ShortcutConstants;
import com.mindolph.markdown.dialog.TableDialog;
import com.mindolph.markdown.dialog.TableOptions;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextBlockDialog;
import com.mindolph.mfx.util.BoundsUtils;
import com.mindolph.mfx.util.ClipBoardUtils;
import com.mindolph.mfx.util.DesktopUtils;
import com.mindolph.mfx.util.UrlUtils;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sun.javafx.webkit.WebConsoleListener;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.KeepType;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.print.PrinterJob;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.ClasspathResourceUtils;
import org.swiftboot.util.IoUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.base.constant.PrefConstants.PREF_KEY_MD_FONT_FILE_PDF;
import static com.mindolph.base.control.ExtCodeArea.FEATURE.*;
import static com.mindolph.markdown.constant.MarkdownConstants.*;

/**
 * @author mindolph.com@gmail.com
 */
public class MarkdownEditor extends BasePreviewEditor implements Initializable, EventHandler<ActionEvent> {

    private final Logger log = LoggerFactory.getLogger(MarkdownEditor.class);

    public static final String URL_MARKUP = "[%s](%s)";
    public static final String IMG_MARKUP = "![%s](%s)";


    private static final String initScrollScript = """
            function initScrollPos(){
                window.scrollTo(${xPos}, ${yPos})
            }
            """;

    private final Pattern pattern;

    @FXML
    private AnchorPane panePreview;

    @FXML
    private VirtualizedScrollPane<SearchableCodeArea> codeScrollPane;

    @FXML
    private Button btnBullet;
    @FXML
    private Button btnBold;
    @FXML
    private Button btnItalic;
    //    @FXML
//    private Button btnNumber;
    @FXML
    private Button btnLink;
    @FXML
    private Button btnQuote;
    @FXML
    private Button btnCode;
    @FXML
    private Button btnTable;
    @FXML
    private Button btnHeader1;
    @FXML
    private Button btnHeader2;
    @FXML
    private Button btnHeader3;
    @FXML
    private Button btnHeader4;
    @FXML
    private Button btnHeader5;
    @FXML
    private Button btnHeader6;

    @FXML
    private WebView webView;
    private WebEngine webEngine;
    private String html;
    private ContextMenu contextMenu;

    // markdown parser and renderer
    private final Parser parser;
    private final HtmlRenderer renderer;

    // used to force refresh resource in page like images.
    private String timestamp;


    public MarkdownEditor(EditorContext editorContext) {
        super("/editor/markdown_editor.fxml", editorContext, true);
        super.fileType = SupportFileTypes.TYPE_MARKDOWN;
        pattern = Pattern.compile(
                "(?<HEADING>" + HEADING_PATTERN + ")"
                        + "|(?<CODEBLOCK>" + CODE_BLOCK_PATTERN + ")"
                        + "|(?<BOLDITALIC>" + BOLD_ITALIC_PATTERN + ")"
                        + "|(?<BOLD>" + BOLD_PATTERN + ")"
                        + "|(?<ITALIC>" + ITALIC_PATTERN + ")"
                        + "|(?<LIST>" + LIST_PATTERN + ")"
                        + "|(?<TABLE>" + TABLE_PATTERN + ")"
                        + "|(?<CODE>" + CODE_PATTERN + ")"
                        + "|(?<QUOTE>" + QUOTE_PATTERN + ")"
                        + "|(?<URL>" + URL_PATTERN + ")"
        );
        timestamp = String.valueOf(System.currentTimeMillis());

        codeArea.addFeatures(TAB_INDENT, QUOTE, DOUBLE_QUOTE, BACK_QUOTE, AUTO_INDENT);
        InputMap<KeyEvent> comment = InputMap.consume(EventPattern.keyPressed(ShortcutManager.getIns().getKeyCombination(ShortcutConstants.KEY_MD_COMMENT)), keyEvent -> {
            codeArea.addOrTrimHeadToParagraphsIfAdded(new Replacement("> ")); // TODO add tail
        });
        Nodes.addInputMap(this, comment);

        EventBus.getIns().subscribe(notificationType -> {
            if (notificationType == NotificationType.FILE_LOADED) {
                // scroll to top when file loaded.
                Platform.runLater(() -> codeScrollPane.estimatedScrollYProperty().setValue(0.0));
            }
        });

        FontIconManager fim = FontIconManager.getIns();
        btnBold.setGraphic(fim.getIcon(IconKey.BOLD));
        btnItalic.setGraphic(fim.getIcon(IconKey.ITALIC));
        btnBullet.setGraphic(fim.getIcon(IconKey.BULLET_LIST));
//        btnNumber.setGraphic(fim.getIcon(IconKey.NUMBER_LIST));
        btnLink.setGraphic(fim.getIcon(IconKey.URI));
        btnQuote.setGraphic(fim.getIcon(IconKey.QUOTE));
        btnCode.setGraphic(fim.getIcon(IconKey.CODE_TAG));
        btnTable.setGraphic(fim.getIcon(IconKey.TABLE));
        btnHeader1.setGraphic(fim.getIcon(IconKey.H1));
        btnHeader2.setGraphic(fim.getIcon(IconKey.H2));
        btnHeader3.setGraphic(fim.getIcon(IconKey.H3));
        btnHeader4.setGraphic(fim.getIcon(IconKey.H4));
        btnHeader5.setGraphic(fim.getIcon(IconKey.H5));
        btnHeader6.setGraphic(fim.getIcon(IconKey.H6));

        btnBold.setOnAction(this);
        btnItalic.setOnAction(this);
        btnBullet.setOnAction(this);
//        btnNumber.setOnAction(this);
        btnLink.setOnAction(this);
        btnQuote.setOnAction(this);
        btnCode.setOnAction(this);
        btnTable.setOnAction(this);
        btnHeader1.setOnAction(this);
        btnHeader2.setOnAction(this);
        btnHeader3.setOnAction(this);
        btnHeader4.setOnAction(this);
        btnHeader5.setOnAction(this);
        btnHeader6.setOnAction(this);

        webEngine = webView.getEngine();
        webView.setContextMenuEnabled(false);
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnError(event -> log.warn("WebView error: " + event.getMessage(), event.getException()));
        webEngine.setOnAlert(event -> {
            String[] split = StringUtils.split(event.getData(), ",");
            if (ArrayUtils.isNotEmpty(split) && split.length == 2) {
                currentScrollH = Double.parseDouble(split[0].trim());
                currentScrollV = Double.parseDouble(split[1].trim());
            }
        });
        URL cssUri = getCssResourceURI();
        log.debug("Set webview with css: " + cssUri);
        webEngine.setUserStyleSheetLocation(cssUri.toString());
        webEngine.getLoadWorker().progressProperty().addListener((observable, oldValue, newValue) ->
                log.trace("Loaded %s%%".formatted(BigDecimal.valueOf(newValue.doubleValue()).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))));
        webEngine.getLoadWorker().exceptionProperty().addListener((observableValue, throwable, t1) -> {
            t1.printStackTrace();
        });

        webEngine.documentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) interceptLinks(newValue);
        });

        contextMenu = createContextMenu();
        webView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(webView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
            else {
                contextMenu.hide();
            }
        });

        // see https://github.com/vsch/flexmark-java/wiki/Extensions
        MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create(),
                        AutolinkExtension.create(),
                        AnchorLinkExtension.create()
                ))
                .set(Parser.REFERENCES_KEEP, KeepType.LAST)
                .set(Parser.LISTS_ITEM_MARKER_SPACE, true)
                .set(Parser.LISTS_NEW_ITEM_CODE_INDENT, 2)
                .set(HtmlRenderer.INDENT_SIZE, 2)
                .set(HtmlRenderer.PERCENT_ENCODE_URLS, true);

        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
        this.refresh();// to set up the font
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeScrollPane.estimatedScrollYProperty().addListener((ov, oldY, newY) -> {
//            System.out.printf("A: %d-%s%n", Thread.currentThread().getId(), Thread.currentThread().getName());
            if (!super.getIsAutoScroll() || oldY.equals(newY) || viewMode != ViewMode.BOTH) {
                return;
            }
            scrollSwitch.scrollFirst(() -> {
                try {
                    Integer previewVpHeight = (Integer) webView.getEngine().executeScript("getViewportHeight();");
                    Integer previewTotalHeight = (Integer) webView.getEngine().executeScript("getTotalHeight();");
                    if (previewVpHeight != null && previewTotalHeight != null) {
                        currentScrollV = convertScrollPosition(newY, codeArea.getViewportHeight(), codeArea.getTotalHeightEstimate(), previewVpHeight, previewTotalHeight);
                        log.debug("auto scroll preview to: " + currentScrollV);
                        webView.getEngine().executeScript("setScrollPos(%s, %s);".formatted(currentScrollH, currentScrollV));
                    }
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                }
            });
        });

        // method onWebviewScroll() will be called when scrolling webview.
        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, state, newState) -> {
            JSObject window = (JSObject) webView.getEngine().executeScript("window");
            if (window == null) {
                log.warn("web window is null");
            }
            else {
                window.setMember("scrollListener", this);
                window.setMember("hoverListener", this);
                window.setMember("clickListener", this);
            }
        });

        // listen for console output for debugging.
        if (Env.isDevelopment) {
            WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> log.trace("%d: %s".formatted(lineNumber, message)));
        }
    }

    // this method will be called from javascript inside the webview.
    public void onWebviewScroll(double x, double y) {
//        System.out.printf("B: %d-%s%n", Thread.currentThread().getId(), Thread.currentThread().getName());
        if (!super.getIsAutoScroll() || viewMode != ViewMode.BOTH) {
            return;
        }
        scrollSwitch.scrollSecond(() -> {
            try {
                Integer previewVpHeight = (Integer) webView.getEngine().executeScript("getViewportHeight();");
                Integer previewTotalHeight = (Integer) webView.getEngine().executeScript("getTotalHeight();");
                Integer scrollPos = (Integer) webView.getEngine().executeScript("getScrollPosY();");
                double codeScrollTo = convertScrollPosition(scrollPos, previewVpHeight, previewTotalHeight, codeArea.getViewportHeight(), codeArea.getTotalHeightEstimate());
                log.debug("auto scroll code editor to: " + currentScrollV);
                codeScrollPane.estimatedScrollYProperty().setValue(codeScrollTo);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        });
    }

    @Override
    protected void onFilesDropped(CharacterHit hit, File file, String filePath) {
        String markup = FileNameUtils.isImageFile(file) ? IMG_MARKUP : URL_MARKUP;
        String mdFileMarkup = markup.formatted(file.getName(), filePath);
        codeArea.insertText(hit.getInsertionIndex(), mdFileMarkup);
    }

    // this method will be called from javascript inside the webview.
    public void onHover(String content) {
        log.debug("Hover content: %s".formatted(content));
        EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(), new StatusMsg(content));
    }

    // this method will be called from javascript inside the webview.
    public void onFileLinkClicked(String url) {
        if (!UrlUtils.isValid(url)) {
            File f;
            if (FileNameUtils.isAbsolutePath(url)) {
                f = new File(url);
            }
            else {
                f = new File(editorContext.getFileData().getFile().getParentFile(), url);
            }
            log.debug("Try to open file: " + f.getPath());
            EventBus.getIns().notifyOpenFile(new OpenFileEvent(f, true));
        }
    }

    private void interceptLinks(Document document) {
        NodeList linkNodeList = document.getElementsByTagName("a");
        for (int i = 0; i < linkNodeList.getLength(); i++) {
            org.w3c.dom.Node item = linkNodeList.item(i);
            EventTarget eventTarget = (EventTarget) item;
            eventTarget.addEventListener("click", evt -> {
                HTMLAnchorElement anchorElement = (HTMLAnchorElement) evt.getCurrentTarget();
                String href = anchorElement.getHref();
                if (UrlUtils.isValid(href)) {
                    // handle opening URL outside JavaFX WebView
                    log.debug(href);
                    DesktopUtils.openURL(href);
                    evt.preventDefault();
                }
            }, false);
        }
    }

    @Override
    public String getFontPrefKey() {
        return FontConstants.KEY_MD_EDITOR;
    }

    private URL getCssResourceURI() {
        return ClasspathResourceUtils.getResourceURI("style/markdown_preview_github.css");
    }

    private String getCss() {
        URL cssUri = getCssResourceURI();
        String css = null;
        try {
            InputStream inputStream = cssUri.openStream();
            css = IoUtils.readAllToString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return css;
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = pattern.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("HEADING") != null ? "heading" :
                            matcher.group("CODEBLOCK") != null ? "code-block" :
                                    matcher.group("LIST") != null ? "list" :
                                            matcher.group("TABLE") != null ? "table" :
                                                    matcher.group("BOLD") != null ? "bold" :
                                                            matcher.group("ITALIC") != null ? "italic" :
                                                                    matcher.group("BOLDITALIC") != null ? "bold-italic" :
                                                                            matcher.group("CODE") != null ? "code" :
                                                                                    matcher.group("QUOTE") != null ? "md-quote" :
                                                                                            matcher.group("URL") != null ? "url" :
                                                                                                    null; /* never happens */
            assert styleClass != null;
            System.out.printf("%s(%d-%d)%n", styleClass, matcher.start(), matcher.end());
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @Override
    protected void refresh(String text) {
        codeArea.setStyleSpans(0, computeHighlighting(text));
        super.refresh(text);
    }

    @Override
    public void refreshPreview(String text, Callback<Object, Void> callback) {
        if (codeArea.getLength() == 0) {
            log.debug("No text to preview");
            return;
        }

//        Executors.newSingleThreadExecutor().submit(() -> {
        // MutableDataSet options = new MutableDataSet();
        // uncomment to set optional extensions
        // options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));

        // uncomment to convert soft-breaks to hard breaks
        // options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

        Node document = parser.parse(codeArea.getText());
        String html = renderer.render(document);
        callback.call(html);
//        });
    }

    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem miViewSource = new MenuItem("View Source");
        MenuItem miRefresh = new MenuItem("Refresh", FontIconManager.getIns().getIcon(IconKey.REFRESH));
        MenuItem miExportHtml = new MenuItem("Export to HTML file", FontIconManager.getIns().getIcon(IconKey.BROWSE));
        MenuItem miExportImage = new MenuItem("Export to Image file", FontIconManager.getIns().getIcon(IconKey.IMAGE));
        MenuItem miExportPdf = new MenuItem("Export to PDF file", FontIconManager.getIns().getIcon(IconKey.PDF));


        miViewSource.setOnAction(e -> {
            Node document = parser.parse(codeArea.getText());
            String html = renderer.render(document);
            String finalHtml = new HtmlBuilder(html)
                    .title(editorContext.getFileData().getName())
                    .css("style/markdown_preview_github.css")
                    .script(initScrollScript, "initScrollPos")
                    .markdown("markdown-body")
                    .build(timestamp);
//            html = this.wrap(this.fixTheImageUrl(html));
            new TextBlockDialog(getScene().getWindow(), "Source", finalHtml, true).showAndWait();
        });
        miRefresh.setOnAction(e -> {
            timestamp = String.valueOf(System.currentTimeMillis());
            webEngine.load("about:blank");
            refresh(codeArea.getText());
        });
        miExportHtml.setOnAction(e -> {
            Node document = parser.parse(codeArea.getText());
            String html = renderer.render(document);

            HtmlBuilder builder = new HtmlBuilder(html)
                    .title(editorContext.getFileData().getName())
                    .relativeUri(editorContext.getFileData().getFile().getParentFile())
                    .css("style/markdown_preview_github.css")
                    .markdown("markdown-body");
            String finalHtml = builder.build(timestamp);

            log.trace("Export to html: " + StringUtils.abbreviate(finalHtml, 50));
            File file = editorContext.getFileData().getFile();
            File htmlFile = DialogFactory.openSaveFileDialog(getScene().getWindow(), file.getParentFile(),
                    FilenameUtils.getBaseName(file.getName()) + ".html",
                    new FileChooser.ExtensionFilter("HTML file", "*.html"));
            if (htmlFile != null) {
                try {
                    FileUtils.writeStringToFile(htmlFile, finalHtml, StandardCharsets.UTF_8);
                    if (builder.getImages() != null) {
                        for (File oriImageFile : builder.getImages()) {
                            File destImgFile = new File(htmlFile.getParentFile(), "images/" + oriImageFile.getName());
                            log.debug("copy file to " + destImgFile);
                            FileUtils.copyFile(oriImageFile, destImgFile);
                        }
                    }
                    EventBus.getIns().notifyStatusMsg(htmlFile, new StatusMsg("HTML file exported to: %s".formatted(htmlFile.getPath())));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        miExportImage.setOnAction(event -> {
            // this doesn't work TODO
            WebView canvasWebView = new WebView();
            canvasWebView.setPrefWidth(1000);
            canvasWebView.setPrefHeight(1000);
            WebEngine engine = canvasWebView.getEngine();
            engine.getLoadWorker().workDoneProperty().addListener((observable, oldValue, newValue) -> {
                WritableImage snapshot = canvasWebView.snapshot(null, null);
                FxImageUtils.dumpImage(snapshot);
            });
            canvasWebView.getEngine().loadContent(html);
        });
        miExportPdf.setOnAction(event -> {
            File file = editorContext.getFileData().getFile();
            File pdfFile = DialogFactory.openSaveFileDialog(getScene().getWindow(), file.getParentFile(),
                    FilenameUtils.getBaseName(file.getName()) + ".pdf",
                    new FileChooser.ExtensionFilter("PDF file", "*.pdf"));
            if (pdfFile != null && pdfFile.getParentFile().exists()) {
                log.debug("Export to pdf file: " + pdfFile);
                new Thread(() -> {
                    try {
                        String fontFilePath = fxPreferences.getPreference(PREF_KEY_MD_FONT_FILE_PDF, String.class);
                        String finalHtml = new HtmlBuilder(html)
                                .title(editorContext.getFileData().getName())
                                .absoluteUri(editorContext.getFileData().getFile().getParentFile())
                                .css("style/markdown_preview_github.css")
                                .markdown("markdown-body")
                                .pdf(new File(fontFilePath))
                                .build(timestamp);

                        PdfRendererBuilder builder = new PdfRendererBuilder();
                        builder.useFastMode();
                        builder.withHtmlContent(finalHtml, editorContext.getFileData().getFile().toString());
                        builder.toStream(new FileOutputStream(pdfFile));
                        builder.run();
//                        DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
//                                        Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)
//                                        , TocExtension.create()).toMutable()
//                                .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
//                                .toImmutable();
//                        PdfConverterExtension.exportToPdf(pdfFile.getPath(), finalHtml, "", OPTIONS);
                        File debugFile = new File(SystemUtils.getJavaIoTmpDir(), pdfFile.getName() + ".html");
                        log.debug("write to file: " + debugFile);
                        try {
                            FileUtils.writeStringToFile(debugFile, finalHtml, Charset.defaultCharset());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        EventBus.getIns().notifyStatusMsg(file, new StatusMsg("Failed to export to PDF: " + e.getLocalizedMessage()));
                    }
                    EventBus.getIns().notifyStatusMsg(file, new StatusMsg("PDF file exported to: %s".formatted(pdfFile.getPath())));
                }, "Markdown Export Thread").start();
            }
        });
        if (Env.isDevelopment) {
            contextMenu.getItems().addAll(miRefresh, miViewSource, miExportHtml, miExportPdf);
        }
        else {
            contextMenu.getItems().addAll(miRefresh, miExportHtml, miExportPdf);
        }
        return contextMenu;
    }


    @Override
    protected void render(Object renderObject) {
        log.info("Load markdown html to web view");
        html = (String) renderObject;

        String finalScript = RegExUtils.replaceAll(initScrollScript, "\\$\\{xPos\\}", String.valueOf(currentScrollH));
        finalScript = RegExUtils.replaceAll(finalScript, "\\$\\{yPos\\}", String.valueOf(currentScrollV));

        String finalHtml = new HtmlBuilder(html)
                .title(editorContext.getFileData().getName())
                .absoluteUri(editorContext.getFileData().getFile().getParentFile())
                .css("style/markdown_preview_github.css")
                .script(finalScript, "initScrollPos")
                .markdown("markdown-body")
                .build(timestamp);

//        System.out.println(finalHtml);

        log.info("markdown rendered as html done with length: " + finalHtml.length());
        if (webEngine != null) {
            webEngine.loadContent(finalHtml);
        }
        log.debug("after calling webengine loading html");
    }

    @Override
    protected void afterRender() {
    }

    /**
     * @return
     * @deprecated
     */
    public Image getImage() {
        SnapshotParameters params = new SnapshotParameters();
        Bounds boundsInLocal = webView.getBoundsInLocal();
        log.debug(BoundsUtils.boundsInString(boundsInLocal));
        params.setViewport(GeometryConvertUtils.boundsToRectangle2D(boundsInLocal));
        // this only export what's in viewport
        return webView.snapshot(params, null);
    }

    public void print() {
        File file = this.editorContext.getFileData().getFile();
        log.info("Print %s".formatted(file));
        PrinterJob printerJob = PrinterManager.getInstance().createPrinterJob();
        printerJob.getJobSettings().setJobName(file.getName());
        boolean proceed = printerJob.showPrintDialog(getScene().getWindow());
        if (proceed) {
            webEngine.print(printerJob);
            printerJob.endJob();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        webEngine.load(null);
        webEngine = null;
        webView = null;
    }

    @Override
    public void handle(ActionEvent event) {
        Object node = event.getSource();
        if (node == btnHeader1) {
            this.addHeader(1);
        }
        else if (node == btnHeader2) {
            this.addHeader(2);
        }
        else if (node == btnHeader3) {
            this.addHeader(3);
        }
        else if (node == btnHeader4) {
            this.addHeader(4);
        }
        else if (node == btnHeader5) {
            this.addHeader(5);
        }
        else if (node == btnHeader6) {
            this.addHeader(6);
        }
        else if (node == btnBold) {
            codeArea.addToSelectionHeadAndTail("**", true);
        }
        else if (node == btnItalic) {
            codeArea.addToSelectionHeadAndTail("*", true);
        }
        else if (node == btnBullet) {
            codeArea.addOrTrimHeadToParagraphsIfAdded(new Replacement("* "));
        }
//        else if (node == btnNumber) {
//            codeArea.addOrTrimHeadToParagraphs(new Replacement(""), s -> {
//                return null; // TODO
//            });
//        }
        else if (node == btnQuote) {
            codeArea.addOrTrimHeadToParagraphsIfAdded(new Replacement("> ", "  "));
        }
        else if (node == btnCode) {
            IndexRange selection = codeArea.getSelection();
            codeArea.addToSelectionHeadAndTail("```", false);
            codeArea.moveTo(selection.getStart() + 3);
        }
        else if (node == btnLink) {
            String text = ClipBoardUtils.textFromClipboard();
            boolean isUrl = UrlUtils.isValid(text);
            String link = (isUrl ? "[](%s)" : "[%s]()").formatted(text);
            IndexRange selection = codeArea.getSelection();
            codeArea.replaceSelection(link);
            codeArea.moveTo(selection.getStart() + (isUrl ? 1 : link.length() - 1));
        }
        else if (node == btnTable) {
            TableOptions to = new TableOptions();
            to.setRows(3);
            to.setCols(3);
            new TableDialog(to).show(tableOptions -> {
                if (tableOptions != null) {
                    String[] emptyRow = new String[tableOptions.getCols()];
                    String[] separatorRow = new String[tableOptions.getCols()];
                    Arrays.fill(emptyRow, "    ");
                    Arrays.fill(separatorRow, "----");
                    String content = String.join("\n",
                            "|" + StringUtils.join(emptyRow, "|") + "|",
                            "|" + StringUtils.join(separatorRow, "|") + "|"
                    );
                    content = "\n" + content + StringUtils.repeat("\n|" + StringUtils.join(emptyRow, "|") + "|", tableOptions.getRows());
                    IndexRange selection = codeArea.getSelection();
                    codeArea.replaceSelection(content);
                    codeArea.moveTo(selection.getStart() + 2);
                }
                codeArea.requestFocus();
            });
        }
        codeArea.requestFocus();
    }

    private void addHeader(int number) {
        String newHead = StringUtils.repeat('#', number) + " ";
        codeArea.addOrTrimHeadToParagraphs(new Replacement(newHead), original -> {
            return newHead + RegExUtils.replaceFirst(original, "(?<head>#+ ?)", StringUtils.EMPTY);
        });
        codeArea.requestFocus();
    }

}
