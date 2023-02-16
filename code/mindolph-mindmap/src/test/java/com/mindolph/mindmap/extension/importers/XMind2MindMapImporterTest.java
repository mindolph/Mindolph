package com.mindolph.mindmap.extension.importers;

import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mindmap.model.TopicNode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.zip.ZipFile;


public class XMind2MindMapImporterTest {

    private static final XMind2MindMapImporter INSTANCE = new XMind2MindMapImporter();

    private ZipFile findZip(String resource) throws Exception {
        return new ZipFile(new File(this.getClass().getResource(resource).toURI()));
    }

    @Test
    public void testXMindOld() throws Exception {
        MindMap<TopicNode> parsed = INSTANCE.parseZipFile(findZip("xmindOld.xmind"));
        Assertions.assertEquals("Avoid \nFocus-Stealing \nTraps", parsed.getRoot().getText());
        Assertions.assertEquals(6, parsed.getRoot().getChildren().size());
    }

    @Test
    public void testXMind2020() throws Exception {
        MindMap<TopicNode> parsed = INSTANCE.parseZipFile(findZip("xmind2020.xmind"));
        Assertions.assertEquals("Central Topic", parsed.getRoot().getText());
        Assertions.assertEquals(4, parsed.getRoot().getChildren().size());
    }

}