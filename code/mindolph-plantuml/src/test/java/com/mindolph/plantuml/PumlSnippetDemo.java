package com.mindolph.plantuml;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.CustomSnippetGroup;
import com.mindolph.base.control.snippet.SnippetView;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.plantuml.snippet.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class PumlSnippetDemo extends Application implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(PumlSnippetDemo.class);

    @FXML
    private SnippetView snippetView;
    @FXML
    private TextArea textArea;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/puml_snippet_demo.fxml"));
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setTitle("Hello Snippet Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PluginManager.getIns().registerPlugin(new PlantUmlPlugin());
        snippetView.setActive(true);
        List<BaseSnippetGroup> groups = Arrays.asList(new GeneralSnippetGroup(),
                new DiagramSnippetGroup(),
                new SkinparamSnippetGroup(),
                new ColorSnippetGroup(),
                new ThemeSnippetGroup(),
                new CreoleSnippetGroup(),
                new ProcessingSnippetGroup(),
                new BuiltinFunctionsSnippetGroup(),
                new C4SnippetGroup(),
                new CustomSnippetGroup());
        snippetView.reload(groups, SupportFileTypes.TYPE_PLANTUML);
        EventBus.getIns().subscribeSnippetApply(snippet -> {
            String code = snippet.getCode();
            if (StringUtils.contains(code, "⨁")) {
                String old = textArea.getSelectedText();
                String replaced = StringUtils.replace(code, "⨁", old);
                textArea.replaceSelection(replaced);
            }
            else {
                textArea.setText(code);
            }
        });
    }

    public static class PrintDemoLauncher {
        public static void main(String[] args) {
            Application.launch(PumlSnippetDemo.class, args);
        }
    }
}
