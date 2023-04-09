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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import com.igormaznitsa.mindmap.model.parser.MindMapLexer;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class TopicTest {

//  private Creatable<TestTopic> TestTopic.topicCreator = new Creatable<>() {
//  };

  private MindMapLexer makeLexer(String text) {
    MindMapLexer lexer = new MindMapLexer();
    lexer.start(text, 0, text.length(), MindMapLexer.TokenType.WHITESPACE);
    return lexer;
  }

  private MindMap<TestTopicNode> createMindMapWithRoot() {
    MindMap<TestTopicNode> map = new MindMap<>();
    map.setRoot(new TestTopicNode(map, null, ""));
    return map;
  }
  
  @Test
  public void isAncestor() {
    MindMap<TestTopicNode> map = createMindMapWithRoot();
    Topic son = new Topic(map, map.getRoot(), "son");
    Topic grandson = new Topic(map, son, "grandson");
    Assert.assertTrue(son.isAncestor(map.getRoot()));
    Assert.assertTrue(grandson.isAncestor(map.getRoot()));
    Assert.assertTrue(grandson.isAncestor(son));
  }

  @Test
  public void testFindMaxChildPathLength() {
    MindMap<TestTopicNode> map = createMindMapWithRoot();
    Topic t1 = new Topic(map, map.getRoot(), "t1");

    Topic t2 = new Topic(map, map.getRoot(), "t2");
    Topic t21 = new Topic(map, t2, "t21");
    Topic t22 = new Topic(map, t21, "t22");

    Topic t3 = new Topic(map, map.getRoot(), "t3");
    Topic t31 = new Topic(map, t3, "t31");
    Topic t32 = new Topic(map, t31, "t32");
    Topic t33 = new Topic(map, t32, "t33");

    assertEquals(0, t1.findMaxChildPathLength());
    assertEquals(2, t2.findMaxChildPathLength());
    assertEquals(4, map.getRoot().findMaxChildPathLength());
  }

  @Test
  public void test() {
    MindMap mm = createMindMapWithRoot();
    Topic topic = new Topic(mm, null, "авы аыва вы Что то");
    assertTrue(topic
        .containsPattern(null, Pattern.compile(Pattern.quote("Что"), Pattern.CASE_INSENSITIVE),
            true, null));
    assertTrue(topic.containsPattern(null,
        Pattern.compile(Pattern.quote("что"), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
        true, null));
  }

  @Test
  public void testParse_OnlyTopic() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic topic = Topic.parse(mm, makeLexer("# Topic"), TestTopicNode.testTopicCreator);
    assertEquals("Topic", topic.getText());
    assertTrue(topic.getChildren().isEmpty());
  }

  @Test
  public void testParse_OnlyTopicWithExtras() throws Exception {
    MindMap<TestTopicNode> mm = createMindMapWithRoot();
    TestTopicNode topic = Topic.parse(mm, makeLexer(
        "# Topic\n- NOTE\n<pre>Some\ntext</pre>\n- LINK\n<pre>http://www.google.com</pre>\n## Topic2"), TestTopicNode.testTopicCreator);
    assertEquals("Topic", topic.getText());
    assertEquals(1, topic.getChildren().size());
    assertEquals(2, topic.getExtras().size());
    assertEquals("Some\ntext", topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),
        ((MMapURI) topic.getExtras().get(Extra.ExtraType.LINK).getValue()).asURI());
  }

  @Test
  public void testParse_PairTopicsFirstContainsMultilineTextNoteAndMiscEOL() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer(
        "# Topic\r\n- NOTE\r\n<pre>   Some   \r\n    text     \n    line  \r\n  end \r\n   </pre>\r\n- LINK\n<pre>http://www.google.com</pre>\n## Topic2"), TestTopicNode.testTopicCreator);
    assertEquals("Topic", topic.getText());
    assertEquals(1, topic.getChildren().size());
    assertEquals(2, topic.getExtras().size());
    assertEquals("   Some   \r\n    text     \n    line  \r\n  end \r\n   ",
            topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),
        ((MMapURI) topic.getExtras().get(Extra.ExtraType.LINK).getValue()).asURI());
    Topic second = topic.getFirst();
    assertEquals("Topic2", second.getText());
  }

  @Test
  public void testParse_TopicWithURLContainingSpaces() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer(
        "# Topic\n- NOTE\n<pre>Some\ntext</pre>\n- LINK\n<pre>  http://www.google.com </pre>\n## Topic2"), TestTopicNode.testTopicCreator);
    assertEquals("Topic", topic.getText());
    assertEquals(1, topic.getChildren().size());
    assertEquals(2, topic.getExtras().size());
    assertEquals("Some\ntext", topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),
        ((MMapURI) topic.getExtras().get(Extra.ExtraType.LINK).getValue()).asURI());
  }

  @Test
  public void testParse_OnlyTopicWithExtrasAndAttributes() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer(
        "# Topic\n- NOTE\n<pre>Some\ntext</pre>\n- LINK\n<pre>http://www.google.com</pre>\n> attr1=`hello`,attr2=``wor`ld``"), TestTopicNode.testTopicCreator);
    assertEquals("Topic", topic.getText());
    assertTrue(topic.getChildren().isEmpty());
    assertEquals(2, topic.getExtras().size());
    assertEquals("Some\ntext", topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),
        ((MMapURI) topic.getExtras().get(Extra.ExtraType.LINK).getValue()).asURI());
    assertEquals(2, topic.getAttributes().size());
    assertEquals("hello", topic.getAttribute("attr1"));
    assertEquals("wor`ld", topic.getAttribute("attr2"));
  }

  @Test
  public void testParse_TopicAndChild() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Topic<br>Root\n ## Child<br/>Topic"), TestTopicNode.testTopicCreator);
    assertEquals("Topic\nRoot", topic.getText());
    assertEquals(1, topic.getChildren().size());

    Topic child = topic.getChildren().get(0);
    assertEquals("Child\nTopic", child.getText());
    assertTrue(child.getChildren().isEmpty());
  }

  @Test
  public void testParseRoot_CodeSnippetContainsThreeBackticksLine_withSpacesBefore()
      throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Root\n```Java\nSystem.exit(0);\n ```\n```"), TestTopicNode.testTopicCreator);
    assertEquals("System.exit(0);\n ```\n", topic.getCodeSnippet("Java"));
  }

  @Test
  public void testParseRoot_CodeSnippetContainsThreeBackticksLine_withOneMoreBacktickAfterSpace()
      throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Root\n```Java\nSystem.exit(0);\n``` `\n```"), TestTopicNode.testTopicCreator);
    assertEquals("System.exit(0);\n``` `\n", topic.getCodeSnippet("Java"));
  }

  @Test
  public void testParseRoot_CodeSnippetContainsThreeBackticksLine_manyBackticks() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Root\n```Java\nSystem.exit(0);\n`````\n```"), TestTopicNode.testTopicCreator);
    assertEquals("System.exit(0);\n`````\n", topic.getCodeSnippet("Java"));
  }

  @Test
  public void testParseRoot_CodeSnippetContainsThreeBackticksLine_threeBacktickAndChar()
      throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Root\n```Java\nSystem.exit(0);\n```a\n```"), TestTopicNode.testTopicCreator);
    assertEquals("System.exit(0);\n```a\n", topic.getCodeSnippet("Java"));
  }

  @Test
  public void testParseRoot_CodeSnippetContainsThreeBackticksLine_charAndThreeBacktick()
      throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Root\n```Java\nSystem.exit(0);\na```\n```"), TestTopicNode.testTopicCreator);
    assertEquals("System.exit(0);\na```\n", topic.getCodeSnippet("Java"));
  }

  @Test
  public void testParseRoot_emptyCodeSnippet() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Root\n```Java\n```"), TestTopicNode.testTopicCreator);
    assertEquals("", topic.getCodeSnippet("Java"));
  }

  @Test
  public void testParseRoot_notClosedCodeSnippet_OnlyHeader() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Root\n```Java\n"), TestTopicNode.testTopicCreator);
    assertNull(topic.getCodeSnippet("Java"));
  }

  @Test
  public void testParseRoot_notClosedCodeSnippet_NotClosed() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Root\n```Java\nSystem.out.println();\n"), TestTopicNode.testTopicCreator);
    assertNull(topic.getCodeSnippet("Java"));
  }

  @Test
  public void testParse_TopicAndTwoChildren() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# Topic\n ## Child1\n ## Child2\n"), TestTopicNode.testTopicCreator);
    assertEquals("Topic", topic.getText());
    assertEquals(2, topic.getChildren().size());

    Topic child1 = topic.getChildren().get(0);
    assertEquals("Child1", child1.getText());
    assertTrue(child1.getChildren().isEmpty());

    Topic child2 = topic.getChildren().get(1);
    assertEquals("Child2", child2.getText());
    assertTrue(child2.getChildren().isEmpty());
  }

  @Test
  public void testParse_MultiLevels() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode root = TestTopicNode.parse(mm, makeLexer(
        "# Level1\n## Level2.1\n### Level3.1\n## Level2.2\n### Level3.2\n#### Level4.2\n## Level2.3"), TestTopicNode.testTopicCreator);
    assertEquals("Level1", root.getText());
    assertEquals(3, root.getChildren().size());
    assertEquals("Level2.1", root.getChildren().get(0).getText());
    assertEquals("Level2.2", root.getChildren().get(1).getText());
    assertEquals("Level2.3", root.getChildren().get(2).getText());

    TestTopicNode level32 = root.getChildren().get(1).getChildren().get(0);

    assertEquals("Level3.2", level32.getText());
    assertEquals("Level4.2", level32.getChildren().get(0).getText());
  }

  @Test
  public void testParse_WriteOneLevel() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "Level1");
    StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# Level1\n", writer.toString());
  }

  @Test
  public void testParse_WriteOneLevelWithExtra() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "Level1");
    root.setExtra(new ExtraLink("http://wwww.igormaznitsa.com"));
    StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# Level1\n- LINK\n<pre>http://wwww.igormaznitsa.com</pre>\n",
        writer.toString());
  }

  @Test
  public void testParse_WriteOneLevelWithExtraAndTwoCodeSnippets() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "Level1");
    root.setCodeSnippet("Java", "System.exit();");
    root.setCodeSnippet("Shell", "exit");
    root.setExtra(new ExtraLink("http://wwww.igormaznitsa.com"));
    StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals(
        "\n# Level1\n- LINK\n<pre>http://wwww.igormaznitsa.com</pre>\n```Java\nSystem.exit();\n```\n```Shell\nexit\n```\n",
        writer.toString());
  }

  @Test
  public void testParse_WriteOneLevelWithExtraAndAttribute() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "Level1");
    root.setAttribute("hello", "wor`ld");
    root.setExtra(new ExtraLink("http://wwww.igormaznitsa.com"));
    StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals(
        "\n# Level1\n> hello=``wor`ld``\n\n- LINK\n<pre>http://wwww.igormaznitsa.com</pre>\n",
        writer.toString());
  }

  @Test
  public void testParse_WriteOneLevelWithSpecialChars() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "<Level1>\nNextText");
    StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# \\<Level1\\><br/>NextText\n", writer.toString());
  }

  @Test
  public void testParse_WriteTwoLevel() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "Level1");
    new Topic(mm, root, "Level2");
    StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# Level1\n\n## Level2\n", writer.toString());
  }

  @Test
  public void testParse_WriteThreeLevel() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "Level1");
    Topic level2 = new Topic(mm, root, "Level2");
    new Topic(mm, level2, "Level3");
    new Topic(mm, root, "Level2.1");
    StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# Level1\n\n## Level2\n\n### Level3\n\n## Level2\\.1\n", writer.toString());
  }

  @Test
  public void testParse_EmptyTextAtMiddleLevel() throws Exception {
    MindMap mm = createMindMapWithRoot();
    TestTopicNode topic = TestTopicNode.parse(mm, makeLexer("# \n## Child\n"), TestTopicNode.testTopicCreator);
    assertEquals("", topic.getText());
    assertEquals(1, topic.getChildren().size());

    Topic child2 = topic.getChildren().get(0);
    assertEquals("Child", child2.getText());
    assertTrue(child2.getChildren().isEmpty());
  }

  @Test
  public void testParse_topicWithTextStartsWithHash() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "");
    mm.setRoot(root);
    new Topic(mm, root, "#NewTopic");
    String packedMap = mm.packToString();
    MindMap parsed = new MindMap(new StringReader(packedMap), TestTopicNode.testTopicCreator);
    Topic rootParsed = parsed.getRoot();

    assertEquals(1, rootParsed.getChildren().size());
    Topic theTopic = rootParsed.getFirst();
    assertEquals("#NewTopic", theTopic.getText());
    assertTrue(theTopic.getExtras().isEmpty());
  }

  @Test
  public void testWriteParse_encryptedNote() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "");
    mm.setRoot(root);
    root.setText("`Root\ntopic`");
    root.setExtra(new ExtraNote("Encrypted world", true, "tip"));

    StringWriter writer = new StringWriter();
    String written = mm.write(writer).toString();

    assertTrue(written.contains("> extras.note.encrypted=`true`,extras.note.encrypted.hint=`tip`"));

    MindMap parsed = new MindMap(new StringReader(written), TestTopicNode.testTopicCreator);
    Topic parsedRoot = parsed.getRoot();
    ExtraNote parsedNote = (ExtraNote) parsedRoot.getExtras().get(Extra.ExtraType.NOTE);
    assertTrue(parsedNote.isEncrypted());
    assertEquals("tip", parsedNote.getHint());
  }

  @Test
  public void testParse_noteContainsTicks() throws Exception {
    MindMap mm = createMindMapWithRoot();
    Topic root = new Topic(mm, null, "");
    mm.setRoot(root);
    root.setText("`Root\ntopic`");
    root.setExtra(new ExtraNote("Hello world \n <br>```Some```"));

    StringWriter writer = new StringWriter();
    mm.write(writer);
    String text = writer.toString();

    assertEquals("Mind Map generated by Mindolph   \n"
        + "> __version__=`1.1`\n"
        + "---\n"
        + "\n# \\`Root<br/>topic\\`\n"
        + "- NOTE\n"
        + "<pre>Hello world \n"
        + " &lt;br&gt;```Some```</pre>\n", text);

    MindMap parsed = new MindMap(new StringReader(text), TestTopicNode.testTopicCreator);

    assertEquals("`Root\ntopic`", parsed.getRoot().getText());
    assertEquals("Hello world \n <br>```Some```",
        ((ExtraNote) parsed.getRoot().getExtras().get(Extra.ExtraType.NOTE)).getValue());
  }

}
