package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.llm.Constants.OutputFormat;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

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

    String templateMmd = """
            %s(%.1f)
                %s
                    Hi, I'm AI assistant,
                    ask me anything you want.
                    I can do more.
            """;

    Map<OutputFormat, String> map = new HashMap<>() {
        {
            put(OutputFormat.TEXT, templateText);
            put(OutputFormat.MARKDOWN, templateMarkdown);
            put(OutputFormat.MINDMAP, templateMmd);
        }
    };

    @Override
    public String predict(String input, float temperature, OutputParams outputParams) {
        try {
            Thread.sleep(RandomUtils.nextInt(500, 3000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (System.currentTimeMillis() % 3 == 0) {
            throw new RuntimeException("Mock LLM exception");
        }
        String chatId = RandomStringUtils.randomAlphabetic(10);

        String template = map.get(outputParams.outputFormat());

        String generated = template.formatted(input, temperature, chatId);
        if (outputParams.outputAdjust() == Constants.OutputAdjust.SHORTER) {
            return StringUtils.substring(generated, 0, StringUtils.lastIndexOf(generated, '\n') + 1);
        }
        else if (outputParams.outputAdjust() == Constants.OutputAdjust.LONGER) {
            switch (outputParams.outputFormat()) {
                case TEXT:
                    return generated + "\nI can do more.";
                case MARKDOWN:
                    return generated + "\n* I can do more.";
                case MINDMAP:
                    return generated + "\n\t\tI can do more.";
            }
            return generated;
        }
        else {
            return generated;
        }
    }
}
