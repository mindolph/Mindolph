package com.mindolph.base.genai;

import com.mindolph.base.genai.llm.LlmService;
import com.mindolph.base.genai.llm.OutputParams;
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

    private final GenAiEvents.Input testInput = new GenAiEvents.Input("讲个笑话", 0.5f, OutputAdjust.SHORTER, false, false);

    @BeforeAll
    public static void setup() {
        FxPreferences.getInstance().init(LlmServiceTest.class);
    }

    @Test
    public void predict() {
        String input = "input";
        float temperature = 0.5f;
        String result1 = LlmService.getIns().predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(result1);
        String result2 = LlmService.getIns().predict(testInput, new OutputParams(OutputAdjust.LONGER, OutputFormat.TEXT));
        System.out.println(result2);
    }

    @Test
    public void stream() {
        LlmService.getIns().stream(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT), streamToken -> {
            System.out.println(streamToken.text());
        });
    }
}
