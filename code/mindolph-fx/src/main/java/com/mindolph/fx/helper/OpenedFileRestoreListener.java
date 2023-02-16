package com.mindolph.fx.helper;

import java.io.File;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface OpenedFileRestoreListener {

    void onOpenedFileRestore(List<File> files);

}
