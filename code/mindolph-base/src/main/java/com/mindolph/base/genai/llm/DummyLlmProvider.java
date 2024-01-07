package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class DummyLlmProvider implements LlmProvider {

    @Override
    public String predict(String input, float temperature, GenAiEvents.OutputAdjust outputAdjust) {
        try {
            Thread.sleep(RandomUtils.nextInt(500, 3000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (RandomUtils.nextInt() % 3 == 0) {
            throw new RuntimeException("Mock LLM exception");
        }
        String chatId = RandomStringUtils.randomAlphabetic(10);
        String generated = """
                [%s](%.1f)
                [%s]
                Hi, I'm AI assistant,
                ask me anything you want.
                I can do more.""".formatted(chatId, temperature, input);
        if (outputAdjust == GenAiEvents.OutputAdjust.SHORTER) {
            return StringUtils.substring(generated, 0, StringUtils.lastIndexOf(generated, '\n') + 1);
        }
        else if (outputAdjust == GenAiEvents.OutputAdjust.LONGER) {
            return generated + "\nI can do more.";
        }
        else {
            return generated;
        }
    }
}
