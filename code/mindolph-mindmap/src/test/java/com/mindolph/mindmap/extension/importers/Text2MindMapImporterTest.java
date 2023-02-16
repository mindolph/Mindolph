/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mindolph.mindmap.extension.importers;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mindmap.model.TopicNode;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class Text2MindMapImporterTest {
    private static final Text2MindMapImporter INSTANCE = new Text2MindMapImporter();

    @Test
    public void testDoImport_Empty() throws Exception {
        final MindMap<TopicNode> result = INSTANCE.makeFromLines(Collections.singletonList("          "));
        Assertions.assertNull(result.getRoot());
    }

    @Test
    public void testDoImport_OnlyRoot() throws Exception {
        final MindMap<TopicNode> result = INSTANCE.makeFromLines(Collections.singletonList("\tSolar system   "));
        Assertions.assertEquals("Solar system", result.getRoot().getText());
    }

    @Test
    public void testDoImport_Multilevel() throws Exception {
        final MindMap<TopicNode> result = INSTANCE.makeFromLines(asList("Solar system", "\tMercury", "\tVenus", "\tEarth", "\t\tMoon", "\tMars", "\t\tFobos", "\t\tDemos", "Jupiter"));
        Assertions.assertEquals("Solar system", result.getRoot().getText());
        Assertions.assertEquals(5, result.getRoot().getChildren().size());
        final TopicNode mars = result.getRoot().getChildren().get(3);
        final TopicNode jupiter = result.getRoot().getChildren().get(4);
        Assertions.assertEquals("Mars", mars.getText());
        Assertions.assertEquals(2, mars.getChildren().size());
        Assertions.assertEquals("Jupiter", jupiter.getText());
    }


    @Test
    public void testDoImport_Multilevel2() throws Exception {
        final MindMap<TopicNode> result = INSTANCE.makeFromLines(asList("solar system", "\tjupiter", "\tmars", " \t\tfobos", "\t\tdeimos", "\tpluto", "\tsaturn"));
        Assertions.assertEquals("solar system", result.getRoot().getText());
        Assertions.assertEquals(4, result.getRoot().getChildren().size());
        final TopicNode root = result.getRoot();
        Assertions.assertEquals("jupiter", root.getChildren().get(0).getText());
        Assertions.assertEquals("mars", root.getChildren().get(1).getText());
        Assertions.assertEquals("pluto", root.getChildren().get(2).getText());
        Assertions.assertEquals("saturn", root.getChildren().get(3).getText());
        final TopicNode mars = result.getRoot().getChildren().get(1);
        Assertions.assertEquals(2, mars.getChildren().size());
        Assertions.assertEquals("fobos", mars.getChildren().get(0).getText());
        Assertions.assertEquals("deimos", mars.getChildren().get(1).getText());
    }

    @Test
    public void testImportFromFile() throws Exception {
        final File file = new File(Text2MindMapImporter.class.getResource("tabbedtext.txt").getFile());
        Assertions.assertTrue(file.isFile());
        final List<String> lines = FileUtils.readLines(file, "UTF-8");
        final MindMap<TopicNode> result = INSTANCE.makeFromLines(lines);
        Assertions.assertEquals(5, result.getRoot().getChildren().size());
    }

}
