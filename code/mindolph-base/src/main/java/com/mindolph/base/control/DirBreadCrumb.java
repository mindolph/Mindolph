package com.mindolph.base.control;

import javafx.scene.control.TreeItem;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mindolph.base.control.DirBreadCrumb.Crumb;

public class DirBreadCrumb extends BreadCrumbBar<Crumb> {


    public void init(File workspaceDir, File displayDir) {
        Path path = null;
        try {
            Path workspacePath = Path.of(workspaceDir.toURI());
            path = Path.of(displayDir.toURI());
            List<Crumb> ret = new ArrayList<>();
            Path cur = path;
            do {
                if (!cur.startsWith(workspacePath)){
                    break;
                }
                ret.add(new Crumb(cur));
                cur = cur.getParent();
                System.out.println(cur);
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
