package com.mindolph.base.control;

import com.mindolph.base.plugin.InputHelper;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.core.constant.SupportFileTypes;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class ExtCodeAreaDemo implements Initializable {

    @FXML
    private ExtCodeArea extCodeArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // register plugins TODO
        PluginManager.getIns().registerPlugin(new TestPlugin());
    }

    private static class TestPlugin implements Plugin {

        @Override
        public Collection<String> supportedFileTypes() {
            return SupportFileTypes.EDITABLE_TYPES;
        }

        @Override
        public InputHelper getInputHelper() {
            return new InputHelper() {
                @Override
                public List<String> getHelpWords() {
                    return null;
                }

                @Override
                public void updateContextText(String text) {

                }
            };
        }
    }

}
