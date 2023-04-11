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
package com.igormaznitsa.mindmap.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;

public class MindMapTest {
//  private Creatable<TestTopic> TestTopic.topicCreator = new Creatable<>() {
//  };
  @Test(expected = IllegalArgumentException.class)
  public void testMindMapParse_Error_Empty() throws Exception {
    MindMap map = new MindMap(new StringReader(""), TestTopicNode.testTopicCreator);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testMindMapParse_Error_JustTextString() throws Exception {
    MindMap map = new MindMap(new StringReader("Who is here"), TestTopicNode.testTopicCreator);
  }
  
  @Test
  public void testMindMapParse_Error_OnlyHeader() throws Exception {
    MindMap map = new MindMap(new StringReader("Who is here\n-"), TestTopicNode.testTopicCreator);
    assertNull(map.getRoot());
  }
  
  @Test
  public void testMindMapParse_Error_HeaderAndOnlyLevelChar() throws Exception {
    MindMap map = new MindMap(new StringReader("Who is here\n-\n#"), TestTopicNode.testTopicCreator);
    assertNull(map.getRoot());
  }
  
  @Test
  public void testMindMapParse_Error_HeaderAndOnlySpacesAfterLevelChar() throws Exception {
    MindMap map = new MindMap(new StringReader("Who is here\n-\n#         "), TestTopicNode.testTopicCreator);
    assertEquals("        ",map.getRoot().getText());
  }
  
  @Test
  public void testFindNext_Null() throws Exception {
    MindMap map = new MindMap(new StringReader("test\n---\n# Solar\n## Mercury\n## Venus\n## Earth\n### Moon\n## Mars\n### Phobos\n### Deimos"), TestTopicNode.testTopicCreator);
    assertEquals("Mercury",map.findNext(null, null, Pattern.compile(Pattern.quote("cury")), true, null).getText());
  }
  
  @Test
  public void testFindNext_NonNull() throws Exception {
    MindMap map = new MindMap(new StringReader("test\n---\n# Solar\n## Mercury\n## Venus\n## Earth\n### Moon\n## Mars\n### Phobos\n### Deimos"), TestTopicNode.testTopicCreator);
    Topic base = map.findNext(null, null, Pattern.compile(Pattern.quote("cury")), true, null);
    assertNull(map.findNext(null, base, Pattern.compile(Pattern.quote("cury")),true, null));
    Topic earth = map.findNext(null, base, Pattern.compile(Pattern.quote("ar")),true, null);
    assertEquals("Earth",earth.getText());
    Topic mars = map.findNext(null, earth, Pattern.compile(Pattern.quote("ar")),true, null);
    assertEquals("Mars",mars.getText());
    assertNull(map.findNext(null, mars, Pattern.compile(Pattern.quote("ar")),true, null));
  }
  
  @Test
  public void testFindPrev_Null() throws Exception {
    MindMap map = new MindMap(new StringReader("test\n---\n# Solar\n## Mercury\n## Venus\n## Earth\n### Moon\n## Mars\n### Phobos\n### Deimos"), TestTopicNode.testTopicCreator);
    assertEquals("Solar",map.findPrev(null, null, Pattern.compile(Pattern.quote("lar")),true, null).getText());
  }

  @Test
  public void testFindPrev_NonNull() throws Exception {
    MindMap map = new MindMap(new StringReader("test\n---\n# Solar\n## Mercury\n## Venus\n## Earth\n### Moon\n## Mars\n### Phobos\n### Deimos"), TestTopicNode.testTopicCreator);
    Topic base = map.findNext(null, null, Pattern.compile(Pattern.quote("Deimos")), true, null);
    Topic mars = map.findPrev(null, base, Pattern.compile(Pattern.quote("ar")),true, null);
    assertEquals("Mars", mars.getText());
    Topic earth = map.findPrev(null, mars, Pattern.compile(Pattern.quote("ar")),true, null);
    assertEquals("Earth", earth.getText());
    Topic solar = map.findPrev(null, earth, Pattern.compile(Pattern.quote("ar")),true, null);
    assertEquals("Solar", solar.getText());
    assertNull(map.findPrev(null, solar, Pattern.compile(Pattern.quote("ar")),true, null));
  }
  
  @Test
  public void testMindMapParse_NoAttributes() throws Exception {
    MindMap map = new MindMap(new StringReader("lkf\n---\n# Hello"), TestTopicNode.testTopicCreator);
    assertNull(map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  }

  @Test
  public void testMindMapParse_oneAttributeDoubleNewLine() throws Exception {
    MindMap map = new MindMap(new StringReader("lkf\n> test=`Hi`\n\n---\n# Hello"), TestTopicNode.testTopicCreator);
    assertEquals("Hi",map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  }

  @Test
  public void testMindMapParse_oneAttributeSingleNewLine() throws Exception {
    MindMap map = new MindMap(new StringReader("lkf\n> test=`Hi`\n---\n# Hello"), TestTopicNode.testTopicCreator);
    assertEquals("Hi",map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  }

  @Test
  public void testMindMapParse_overridenAttributes_SingleNewLine() throws Exception {
    MindMap map = new MindMap(new StringReader("lkf\n> test=`Hi`\n> test=`Lo`\n---\n# Hello"), TestTopicNode.testTopicCreator);
    assertEquals("Lo",map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  } 
  
  @Test
  public void testMindMapParse_overridenAttributes_SeveralNewLine() throws Exception {
    MindMap map = new MindMap(new StringReader("lkf\n> test=`Hi`\n> test=`Lo`\n---\n\n\n\n# Hello"), TestTopicNode.testTopicCreator);
    assertEquals("Lo",map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  }
  
  @Test
  public void testMindMapWrite_WithoutAttributes() throws Exception {
    MindMap map = new MindMap();
    StringWriter writer = new StringWriter();
    map.write(writer);
    assertEquals("Mind Map generated by Mindolph   \n> __version__=`1.1`\n---\n",writer.toString());
  }

  @Test
  public void testMindMapWrite_WithAttribute() throws Exception {
    MindMap map = new MindMap();
    map.setAttribute("hello", "World");
    StringWriter writer = new StringWriter();
    map.write(writer);
    assertEquals("Mind Map generated by Mindolph   \n> __version__=`1.1`,hello=`World`\n---\n",writer.toString());
  }

  @Test
  public void testIteration_TwoLevel() throws Exception {
    MindMap<TestTopicNode> map = new MindMap<>(new StringReader("---\n# root\n## child1\n### child1.1\n### child1.2\n## child2\n### child2.1\n### child2.2\n"), TestTopicNode.testTopicCreator);
    List<String> list = new ArrayList<>();
    for(Topic t : map){
      list.add(t.getText());
    }
    assertArrayEquals(new String[]{"root", "child1", "child1.1", "child1.2", "child2", "child2.1", "child2.2"},list.toArray(new String[0]));
  }

  @Test
  public void testIteration_Empty() throws Exception {
    MindMap map = new MindMap(new StringReader("---\n"), TestTopicNode.testTopicCreator);
    assertFalse(map.iterator().hasNext());
  }

  @Test
  public void testIteration_OnlyRoot() throws Exception {
    MindMap<TestTopicNode> map = new MindMap<>(new StringReader("---\n# root\n"), TestTopicNode.testTopicCreator);
    List<String> list = new ArrayList<>();
    for (Topic t : map) {
      list.add(t.getText());
    }
    assertArrayEquals(new String[]{"root"}, list.toArray(new String[list.size()]));
  }

  @Test
  public void testIteration_OnlyRoot_WithCodeSnippetsJavaAndShell() throws Exception {
    MindMap map = new MindMap(new StringReader("---\n# root\n```Java\nSystem.exit(0);\n```\n```Shell\nexit\n```"), TestTopicNode.testTopicCreator);
    assertEquals(2,map.getRoot().getCodeSnippets().size());
    assertEquals("System.exit(0);\n",map.getRoot().getCodeSnippet("Java"));
    assertEquals("exit\n",map.getRoot().getCodeSnippet("Shell"));
  }

  @Test
  public void testSerializableDeserializable_NoErrors() throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    ObjectOutputStream stream = new ObjectOutputStream(buffer);
    MindMap<TestTopicNode> map = new MindMap();
    map.setRoot(new TestTopicNode(map, null, ""));
    stream.writeObject(map);
    
    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
    assertTrue(in.readObject() instanceof MindMap);
  }
}
