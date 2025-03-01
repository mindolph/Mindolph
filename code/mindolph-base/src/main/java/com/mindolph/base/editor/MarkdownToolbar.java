package com.mindolph.base.editor;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.ExtCodeArea.Replacement;
import com.mindolph.base.dialog.TableDialog;
import com.mindolph.base.dialog.TableOptions;
import com.mindolph.mfx.util.ClipBoardUtils;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.swiftboot.util.UrlUtils;

import java.io.File;
import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 * @see MarkdownCodeArea
 */
public class MarkdownToolbar extends HBox implements EventHandler<ActionEvent> {

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
    private Button btnSeparator;
    @FXML
    private Button btnImage;
    @FXML
    private Button btnComment;

    private final MarkdownCodeArea markdownCodeArea;

    public MarkdownToolbar(MarkdownCodeArea markdownCodeArea) {
        this.markdownCodeArea = markdownCodeArea;
        FxmlUtils.loadUri("/editor/markdown_toolbar.fxml", this);

        FontIconManager fim = FontIconManager.getIns();
        btnBold.setGraphic(fim.getIcon(IconKey.BOLD));
        btnItalic.setGraphic(fim.getIcon(IconKey.ITALIC));
        btnBullet.setGraphic(fim.getIcon(IconKey.BULLET_LIST));
//        btnNumber.setGraphic(fim.getIcon(IconKey.NUMBER_LIST));
        btnLink.setGraphic(fim.getIcon(IconKey.URI));
        btnImage.setGraphic(fim.getIcon(IconKey.PREVIEW));
        btnQuote.setGraphic(fim.getIcon(IconKey.QUOTE));
        btnCode.setGraphic(fim.getIcon(IconKey.CODE_TAG));
        btnTable.setGraphic(fim.getIcon(IconKey.TABLE));
        btnHeader1.setGraphic(fim.getIcon(IconKey.H1));
        btnHeader2.setGraphic(fim.getIcon(IconKey.H2));
        btnHeader3.setGraphic(fim.getIcon(IconKey.H3));
        btnHeader4.setGraphic(fim.getIcon(IconKey.H4));
        btnHeader5.setGraphic(fim.getIcon(IconKey.H5));
        btnHeader6.setGraphic(fim.getIcon(IconKey.H6));
        btnSeparator.setGraphic(fim.getIcon(IconKey.SEPARATOR));
        btnComment.setGraphic(fim.getIcon(IconKey.COMMENT));

        btnBold.setOnAction(this);
        btnItalic.setOnAction(this);
        btnBullet.setOnAction(this);
//        btnNumber.setOnAction(this);
        btnLink.setOnAction(this);
        btnImage.setOnAction(this);
        btnQuote.setOnAction(this);
        btnCode.setOnAction(this);
        btnTable.setOnAction(this);
        btnHeader1.setOnAction(this);
        btnHeader2.setOnAction(this);
        btnHeader3.setOnAction(this);
        btnHeader4.setOnAction(this);
        btnHeader5.setOnAction(this);
        btnHeader6.setOnAction(this);
        btnSeparator.setOnAction(this);
        btnComment.setOnAction(this);
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
            markdownCodeArea.addToSelectionHeadAndTail("**", true);
        }
        else if (node == btnItalic) {
            markdownCodeArea.addToSelectionHeadAndTail("*", true);
        }
        else if (node == btnBullet) {
            markdownCodeArea.addOrTrimHeadToParagraphsIfAdded(new Replacement("* ", "  "));
        }
//        else if (node == btnNumber) {
//            markdownCodeArea.addOrTrimHeadToParagraphs(new Replacement(""), s -> {
//                return null; // TODO
//            });
//        }
        else if (node == btnQuote) {
            markdownCodeArea.addOrTrimHeadToParagraphsIfAdded(new Replacement("> ", "  "));
        }
        else if (node == btnCode) {
            markdownCodeArea.addToSelectionHeadAndTail("```\n", "\n```", true);
        }
        else if (node == btnLink) {
            String text = ClipBoardUtils.textFromClipboard();
            boolean isUrl = UrlUtils.isValid(text);
            IndexRange selection = markdownCodeArea.getSelection();
            String url = isUrl ? text : StringUtils.EMPTY;
            String link = "[%s](%s)".formatted(selection.getLength() > 0 ? markdownCodeArea.getSelectedText() : StringUtils.EMPTY, url);
            markdownCodeArea.replaceSelection(link);
            markdownCodeArea.moveTo(selection.getStart() + (isUrl ? 1 : link.length() - 1));
        }
        else if (node == btnImage) {
            String text = ClipBoardUtils.textFromClipboard();
            File file = new File(text);
            boolean isFilePath = file.exists();
            IndexRange selection = markdownCodeArea.getSelection();
            String path = isFilePath ? text : StringUtils.EMPTY;
            String link = "![%s](%s)".formatted(selection.getLength() > 0 ? markdownCodeArea.getSelectedText() : StringUtils.EMPTY, path);
            markdownCodeArea.replaceSelection(link);
            markdownCodeArea.moveTo(selection.getStart() + (isFilePath ? 1 : link.length() - 1));
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
                    content = content + "\n";
                    IndexRange selection = markdownCodeArea.getSelection();
                    markdownCodeArea.replaceSelection(content);
                    markdownCodeArea.moveTo(selection.getStart() + 2);
                }
                markdownCodeArea.requestFocus();
            });
        }
        else if (node == btnSeparator) {
            markdownCodeArea.insertText("\r---\r");
        }
        else if (node == btnComment) {
            String newHead = "[comment]:";
            markdownCodeArea.addOrTrimHeadToParagraphs(new Replacement(newHead), true);
        }
        markdownCodeArea.requestFocus();
    }

    /**
     * @param number
     */
    private void addHeader(int number) {
        String newHead = StringUtils.repeat('#', number) + " ";
        markdownCodeArea.addOrTrimHeadToParagraphs(new Replacement(newHead), true, original -> {
            return newHead + RegExUtils.replaceFirst(original, "(?<head>#+ ?)|(?<bullet>[\\*-] ?)", StringUtils.EMPTY);
        });
        markdownCodeArea.requestFocus();
    }
}
