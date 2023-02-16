package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.DesktopUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.apache.commons.lang3.SystemUtils;


/**
 * @author mindolph.com@gmail.com
 */
public class AboutDialog extends BaseDialogController<Void> {

    @FXML
    private Label lbJvm;

    public AboutDialog() {
        dialog = new CustomDialogBuilder<Void>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("About Mindolph")
                .fxmlUri("dialog/about_dialog.fxml")
                .buttons(ButtonType.CLOSE)
                .icon(ButtonType.CLOSE, FontIconManager.getIns().getIcon(IconKey.CLOSE))
                .controller(this)
                .build();

        lbJvm.setText("%s %s\nby %s".formatted(SystemUtils.JAVA_VM_NAME, SystemUtils.JAVA_VM_VERSION, SystemUtils.JAVA_VM_VENDOR));
    }

    @FXML
    public void onLink(ActionEvent event){
        DesktopUtils.openURL("https://github.com/mindolph/Mindolph");
    }
}
