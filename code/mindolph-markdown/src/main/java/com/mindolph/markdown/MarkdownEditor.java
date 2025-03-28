package com.mindolph.markdown;


import com.mindolph.base.EditorContext;
import com.mindolph.core.Env;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.SearchableCodeArea;
import com.mindolph.base.editor.BasePreviewEditor;
import com.mindolph.base.editor.MarkdownCodeArea;
import com.mindolph.base.editor.MarkdownToolbar;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.OpenFileEvent;
import com.mindolph.base.event.StatusMsg;
import com.mindolph.base.print.PrinterManager;
import com.mindolph.base.util.CssUtils;
import com.mindolph.base.util.FxImageUtils;
import com.mindolph.base.util.GeometryConvertUtils;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.search.TextLocation;
import com.mindolph.core.template.HtmlBuilder;
import com.mindolph.core.util.FileNameUtils;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextBlockDialog;
import com.mindolph.mfx.util.BoundsUtils;
import com.mindolph.mfx.util.DesktopUtils;
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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.print.PrinterJob;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.ClasspathResourceUtils;
import org.swiftboot.util.IoUtils;
import org.swiftboot.util.PathUtils;
import org.swiftboot.util.UrlUtils;
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
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

import static com.mindolph.base.constant.FontConstants.KEY_MD_EDITOR;
import static com.mindolph.base.constant.FontConstants.KEY_MD_EDITOR_MONO;
import static com.mindolph.base.constant.MarkdownConstants.HEADING_PATTERN;
import static com.mindolph.base.constant.PrefConstants.*;
import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;

/**
 * @author mindolph.com@gmail.com
 */
public class MarkdownEditor extends BasePreviewEditor implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(MarkdownEditor.class);

    public static final String URL_MARKUP = "[%s](%s)";
    public static final String IMG_MARKUP = "![%s](%s)";


    private static final String initScrollScript = """
            function initScrollPos(){
                window.scrollTo(${xPos}, ${yPos})
            }
            """;


    @FXML
    private AnchorPane panePreview;

    @FXML
    private VirtualizedScrollPane<SearchableCodeArea> codeScrollPane;

    @FXML
    private VBox vbToolbar;
    private MarkdownToolbar markdownToolbar;

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

    private final EventSource<Double> scrollEventCode = new EventSource<>();
    private final EventSource<Double> scrollEventPreview = new EventSource<>();

    public MarkdownEditor(EditorContext editorContext) {
        super("/editor/markdown_editor.fxml", editorContext, true);
        super.fileType = SupportFileTypes.TYPE_MARKDOWN;

        threadPoolService = Executors.newSingleThreadExecutor();

        timestamp = String.valueOf(System.currentTimeMillis());

        // comment because not sure if this is necessary
//        EventBus.getIns().subscribeFileLoaded(editorContext.getFileData(), fileData -> {
//            Platform.runLater(() -> codeScrollPane.estimatedScrollYProperty().setValue(0.0));
//        });


        // init the toolbar
        markdownToolbar = new MarkdownToolbar((MarkdownCodeArea) codeArea);
        vbToolbar.getChildren().add(markdownToolbar);

        webEngine = webView.getEngine();
        log.debug(webEngine.getUserAgent());
        webView.setContextMenuEnabled(false);
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnError(event -> log.warn("WebView error: %s".formatted(event.getMessage()), event.getException()));
        webEngine.setOnAlert(event -> {
            String[] split = StringUtils.split(event.getData(), ",");
            if (ArrayUtils.isNotEmpty(split) && split.length == 2) {
                currentScrollH = Double.parseDouble(split[0].trim());
                currentScrollV = Double.parseDouble(split[1].trim());
            }
        });
        URL cssUri = getCssResourceURI();
        log.debug("Set webview with css: %s".formatted(cssUri));
        webEngine.setUserStyleSheetLocation(cssUri.toString());
        webEngine.getLoadWorker().progressProperty().addListener((observable, oldValue, newValue) ->
                log.trace("Loaded %s%%".formatted(BigDecimal.valueOf(newValue.doubleValue()).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))));
        webEngine.getLoadWorker().exceptionProperty().addListener((observableValue, throwable, t1) -> {
            if (t1 != null) log.error("Markdown Preview Error", t1);
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
        // this.refresh();// to set up the font

        scrollEventCode.reduceSuccessions((s1, s2) -> s2, Duration.ofMillis(100))
                .subscribe(newY -> {
                    scrollEventPreview.suspenderOf(scrollEventCode.suppressible().suppressible());
                    scrollSwitch.scrollFirst(() -> {
                        try {
                            Integer previewVpHeight = (Integer) webView.getEngine().executeScript("getViewportHeight();");
                            Integer previewTotalHeight = (Integer) webView.getEngine().executeScript("getTotalHeight();");
                            if (previewVpHeight != null && previewTotalHeight != null) {
                                currentScrollV = convertScrollPosition(newY, codeArea.getViewportHeight(), codeArea.getTotalHeightEstimate(), previewVpHeight, previewTotalHeight);
                                if (log.isTraceEnabled())
                                    log.trace("auto scroll preview to: %s".formatted(currentScrollV));
                                webView.getEngine().executeScript("setScrollPos(%s, %s);".formatted(currentScrollH, currentScrollV));
                            }
                        } catch (Exception e) {
                            log.error(e.getLocalizedMessage());
                        }
                    });
                });
        scrollEventPreview.reduceSuccessions((s1, s2) -> s2, Duration.ofMillis(100))
                .subscribe(newY -> {
                    scrollSwitch.scrollSecond(() -> {
                        try {
                            Integer previewVpHeight = (Integer) webView.getEngine().executeScript("getViewportHeight();");
                            Integer previewTotalHeight = (Integer) webView.getEngine().executeScript("getTotalHeight();");
                            Integer scrollPos = (Integer) webView.getEngine().executeScript("getScrollPosY();");
                            double codeScrollTo = convertScrollPosition(scrollPos, previewVpHeight, previewTotalHeight, codeArea.getViewportHeight(), codeArea.getTotalHeightEstimate());
                            if (log.isTraceEnabled())
                                log.trace("auto scroll code editor to: %s".formatted(currentScrollV));
                            codeScrollPane.estimatedScrollYProperty().setValue(codeScrollTo);
                        } catch (Exception e) {
                            log.error(e.getLocalizedMessage());
                        }
                    });
                });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        codeScrollPane.estimatedScrollYProperty().addListener((ov, oldY, newY) -> {
//            System.out.printf("A: %d-%s%n", Thread.currentThread().getId(), Thread.currentThread().getName());
            if (!super.getIsAutoScroll() || oldY.equals(newY) || viewMode != ViewMode.BOTH) {
                return;
            }
            scrollEventCode.push(newY);
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
        // for remembering the scroll position to keep the webview where it is during editing.
        super.currentScrollH = x;
        super.currentScrollV = y;

        if (!super.getIsAutoScroll() || viewMode != ViewMode.BOTH) {
            return;
        }
        scrollEventPreview.push(y);
    }

    @Override
    protected void onFileDropped(CharacterHit hit, File file, String filePath) {
        String markup = FileNameUtils.isImageFile(file) ? IMG_MARKUP : URL_MARKUP;
        String mdFileMarkup = markup.formatted(file.getName(), filePath);
        codeArea.insertText(hit.getInsertionIndex(), mdFileMarkup);
    }

    @Override
    protected void onFilesDropped(CharacterHit hit, List<File> files) {
        List<String> paths = files.stream().map(
                file -> {
                    String markup = FileNameUtils.isImageFile(file) ? IMG_MARKUP : URL_MARKUP;
                    String filePath = super.getRelatedPathInCurrentWorkspace(file).orElseGet(file::getPath);
                    String mdFileMarkup = markup.formatted(file.getName(), filePath);
                    return "%s  ".formatted(mdFileMarkup); // add 2 blanks for Markdown
                }).toList();
        codeArea.insertText(hit.getInsertionIndex(), StringUtils.join(paths, LINE_SEPARATOR));
    }

    // NOTE: this method will be called from javascript inside the webview.
    public void onHover(String content) {
        log.debug("Hover content: %s".formatted(content));
        EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(), new StatusMsg(content));
    }

    // NOTE: this method will be called from javascript inside the webview.
    public void onFileLinkClicked(String url) {
        if (!UrlUtils.isValid(url)) {
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
            File f;
            if (PathUtils.isAbsolutePath(decodedUrl)) {
                f = new File(decodedUrl);
            }
            else {
                f = new File(editorContext.getFileData().getFile().getParentFile(), decodedUrl);
            }
            log.debug("Try to open file: %s".formatted(f.getPath()));
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

    @Override
    public void applyStyles() {
        CssUtils.applyFontCss(codeArea, "/style/markdown_syntax_template.css", KEY_MD_EDITOR, KEY_MD_EDITOR_MONO);
    }

    @Override
    protected void refresh(String text) {
        codeArea.refresh();
        super.refresh(text);
        this.refresh();
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
        MenuItem miViewSource = new MenuItem("View Source...");
        MenuItem miRefresh = new MenuItem("Refresh", FontIconManager.getIns().getIcon(IconKey.REFRESH));
        MenuItem miExportHtml = new MenuItem("Export to HTML file...", FontIconManager.getIns().getIcon(IconKey.BROWSE));
        MenuItem miExportImage = new MenuItem("Export to Image file...", FontIconManager.getIns().getIcon(IconKey.IMAGE));
        MenuItem miExportPdf = new MenuItem("Export to PDF file...", FontIconManager.getIns().getIcon(IconKey.PDF));


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

            log.trace("Export to html: %s".formatted(StringUtils.abbreviate(finalHtml, 50)));
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
                            log.debug("copy file to %s".formatted(destImgFile));
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
                log.info("Export to pdf file: %s".formatted(pdfFile));
                new Thread(() -> {
                    try {
                        String sansFontFilePath = fxPreferences.getPreferenceAlias(PREF_KEY_MD_SANS_FONT_FILE, PREF_KEY_MD_FONT_FILE_PDF, String.class);
                        String monoFontFilePath = fxPreferences.getPreference(PREF_KEY_MD_MONO_FONT_FILE, String.class);
                        String finalHtml = new HtmlBuilder(html)
                                .title(editorContext.getFileData().getName())
                                .absoluteUri(editorContext.getFileData().getFile().getParentFile())
                                .css("style/markdown_export_pdf.css")
                                .markdown("markdown-body")
                                .pdf(new File(sansFontFilePath), new File(monoFontFilePath))
                                .build(timestamp);

                        if (log.isTraceEnabled()) {
                            System.out.println(finalHtml);
                        }

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
                    } catch (Throwable e) {
                        log.error("Failed to export to PDF", e);
                        EventBus.getIns().notifyStatusMsg(file, new StatusMsg("Failed to export to PDF: " + e.getLocalizedMessage()));
                    }
                    String success = "PDF file exported to: %s".formatted(pdfFile.getPath());
                    log.info(success);
                    EventBus.getIns().notifyStatusMsg(file, new StatusMsg(success));
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
        if (log.isTraceEnabled()) log.trace("Load markdown html to web view");
        html = (String) renderObject;

        if (log.isTraceEnabled()) log.trace("Init the web view position to: %.1f, %.1f".formatted(currentScrollH, currentScrollV));
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

        log.info("markdown rendered as html done with length: %d".formatted(finalHtml.length()));
        if (webEngine != null) {
            webEngine.loadContent(finalHtml);
        }
        log.debug("after calling webengine loading html");
    }

    @Override
    protected void afterRender() {
        // NO NEED TO DO ANYTHING
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
        webEngine.getLoadWorker().cancel();
        webEngine.load(null);
        webEngine = null;
        webView = null;
    }

    @Override
    protected String getOutlinePattern() {
        return HEADING_PATTERN;
    }

    @Override
    protected String getHeadingLevelTag() {
        return "#";
    }

    @Override
    protected String extractOutlineTitle(String heading, TextLocation location, TextLocation nextBlockLocation) {
        return RegExUtils.replacePattern(heading, "#" , "");
    }

}
