package com.mindolph.base.control;

import javafx.scene.control.TreeItem;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mindolph.base.control.DirBreadCrumb.Crumb;

/**
 * @since 1.8
 */
public class DirBreadCrumb extends BreadCrumbBar<Crumb> {

    /**
     * Init the bread crumb bar with base workspace dir and target dir.
     *
     * @param workspaceDir
     * @param displayDir
     */
    public void init(File workspaceDir, File displayDir) {
        try {
            Path workspacePath = Path.of(workspaceDir.toURI());
            Path path = Path.of(displayDir.toURI());
            List<Crumb> ret = new ArrayList<>();
            Path cur = path;
            do {
                if (!cur.startsWith(workspacePath)){
                    break;
                }
                ret.add(new Crumb(cur));
                cur = cur.getParent();
            } while(cur !=null);
            Collections.reverse(ret);
            TreeItem<Crumb> pathTreeItem = BreadCrumbBar.buildTreeModel(ret.toArray(new Crumb[]{}));
            super.setSelectedCrumb(pathTreeItem);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record Crumb(Path path){
        @Override
        public String toString() {
            return path.toFile().getName();
        }
    }
}
