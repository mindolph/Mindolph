package com.mindolph.base.control;

import com.mindolph.base.plugin.*;
import com.mindolph.core.constant.SupportFileTypes;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class ExtCodeAreaDemo implements Initializable {

    @FXML
    private SmartCodeArea extCodeArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // register plugins TODO
        PluginManager.getIns().registerPlugin(new TestPlugin());
    }

    private static class TestPlugin extends BasePlugin {

        @Override
        public Integer getOrder() {
            return 100;
        }

        @Override
        public Collection<String> supportedFileTypes() {
            return SupportFileTypes.EDITABLE_TYPES;
        }

        @Override
        public Optional<InputHelper> getInputHelper() {
            return Optional.of(new InputHelper() {
                @Override
                public List<String> getHelpWords(Object editorId) {
                    return null;
                }

                @Override
                public void updateContextText(Object editorId, String text) {

                }
            });
        }

        @Override
        public Optional<Generator> getGenerator() {
            // TODO
            return Optional.empty();
        }
    }

}
