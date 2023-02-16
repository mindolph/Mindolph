package com.mindolph.core.collection;

import org.junit.jupiter.api.Test;
import org.swiftboot.util.ClasspathResourceUtils;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public class ComparatorsTest {

    @Test
    public void testNavigationComparator() {
        URL testUri = ClasspathResourceUtils.getResourceURI("collection");
        File dir = new File(testUri.getPath());
        File[] subFiles = dir.listFiles();
        List<File> files = Arrays.asList(subFiles);
        Collections.swap(files, 0, files.size() - 1);
        for (File file : files) {
            System.out.println(file.getPath());
        }

        System.out.println("Sorted:");
        files.sort(Comparators.NAVIGATION_DEFAULT_COMPARATOR);
        for (File file : files) {
            System.out.println(file.getPath());
        }

    }
}
