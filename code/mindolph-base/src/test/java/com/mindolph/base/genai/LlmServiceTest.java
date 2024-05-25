package com.mindolph.base.genai;

import com.mindolph.base.genai.llm.LlmService;
import com.mindolph.base.genai.llm.OutputParams;
import org.junit.jupiter.api.Test;

import static com.mindolph.core.constant.GenAiConstants.OutputAdjust;
import static com.mindolph.core.constant.GenAiConstants.OutputFormat;

/**
 * @author mindolph.com@gmail.com
 */
public class LlmServiceTest {

    @Test
    public void predict() {
        String input = "input";
        float temperature = 0.5f;
        String result1 = LlmService.getIns().predict(input, temperature, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(result1);
        String result2 = LlmService.getIns().predict(input, temperature, new OutputParams(OutputAdjust.LONGER, OutputFormat.TEXT));
        System.out.println(result2);
    }
}
