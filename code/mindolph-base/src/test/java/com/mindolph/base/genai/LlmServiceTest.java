package com.mindolph.base.genai;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.llm.LlmService;
import com.mindolph.base.genai.llm.OutputParams;
import com.mindolph.base.genai.llm.StreamToken;
import com.mindolph.mfx.preference.FxPreferences;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.mindolph.core.constant.GenAiConstants.OutputAdjust;
import static com.mindolph.core.constant.GenAiConstants.OutputFormat;

/**
 * TODO doesn't work.
 *
 * @author mindolph.com@gmail.com
 */
public class LlmServiceTest {

    private final Input testInput = new InputBuilder().text("讲个笑话").temperature(0.5f).outputAdjust(OutputAdjust.SHORTER).isRetry(false).isStreaming(false).createInput();

    @BeforeAll
    public static void setup() {
        FxPreferences.getInstance().init(LlmServiceTest.class);
    }

    @Test
    public void predict() {
        StreamToken predict1 = LlmService.getIns().predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict1);
        StreamToken predict2 = LlmService.getIns().predict(testInput, new OutputParams(OutputAdjust.LONGER, OutputFormat.TEXT));
        System.out.println(predict2);
    }

    @Test
    public void stream() {
        LlmService.getIns().stream(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT), streamToken -> {
            System.out.println(streamToken.text());
        });
    }
}
