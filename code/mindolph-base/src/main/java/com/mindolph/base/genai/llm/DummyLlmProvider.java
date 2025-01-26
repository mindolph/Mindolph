package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiConstants.OutputFormat;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class DummyLlmProvider implements LlmProvider {


    String templateText = """
            [%s](%.1f)
            [%s]
            Hi, I'm AI assistant,
            ask me anything you want.
            I can do more.""";

    String templateMarkdown = """
            ### %s(%.1f)
            > %s
            * Hi, I'm AI assistant,
            * ask me anything you want.
            * I can do more.""";

    String templateMmdNormal = """
            %s(%.1f)
                %s
                    Hi, I'm AI assistant,
                    ask me anything you want.
                    I can do more.
            """;

    String templateMmdLong = """
            %s(%.1f)
                %s
                    Hi, I'm AI assistant,
                    ask me anything you want.
                    I can do more.
                    aaaa
                    bbbb
                    cccc
                    dddd
                    eeee
                    ffff
                    gggg
                    hhhh
                    iiii
                    jjjj
                    kkkk
                    llll
                    mmmm
                    nnnn
                    oooo
                    pppp
                    qqqq
                    rrrr
                    ssss
                    tttt
                    uuuu
                    vvvv
                    wwww
                    xxxx
                    yyyy
                    zzzz
            """;

    String LENGTH_NORMAL = "normal";
    String LENGTH_LONG = "long";

    MultiKeyMap<String, String> mkMap = new MultiKeyMap<>() {
        {
            put(OutputFormat.TEXT.name(), LENGTH_NORMAL, templateText);
            put(OutputFormat.MARKDOWN.name(), LENGTH_NORMAL, templateMarkdown);
            put(OutputFormat.MINDMAP.name(), LENGTH_NORMAL, templateMmdNormal);
            put(OutputFormat.MINDMAP.name(), LENGTH_LONG, templateMmdLong);
        }
    };

    @Override
    public StreamToken predict(Input input, OutputParams outputParams) {
        try {
            Thread.sleep(RandomUtils.nextInt(500, 3000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (System.currentTimeMillis() % 3 == 0) {
            throw new RuntimeException("Mock LLM exception");
        }
        String chatId = RandomStringUtils.randomAlphabetic(10);

        String length = LENGTH_NORMAL;
        if (input.text().contains(LENGTH_LONG)) {
            length = LENGTH_LONG;
        }

        String template = mkMap.get(outputParams.outputFormat().name(), length);

        String generated = template.formatted(input.text(), input.temperature(), chatId);
        if (outputParams.outputAdjust() == GenAiConstants.OutputAdjust.SHORTER) {
            generated = StringUtils.substring(generated, 0, StringUtils.lastIndexOf(generated, '\n') + 1);
        }
        else if (outputParams.outputAdjust() == GenAiConstants.OutputAdjust.LONGER) {
            switch (outputParams.outputFormat()) {
                case TEXT:
                    generated = generated + "\nI can do more.";
                case MARKDOWN:
                    generated = generated + "\n* I can do more.";
                case MINDMAP:
                    generated = generated + "\n\t\tI can do more.";
                case PLANTUML:
                    generated = generated + "I can do more.";
            }
        }
        return new StreamToken(generated, 999, true, false);
    }

    @Override
    public void stream(Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
        // TODO

    }
}
