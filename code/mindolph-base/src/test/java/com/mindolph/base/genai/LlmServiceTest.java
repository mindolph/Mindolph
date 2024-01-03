package com.mindolph.base.genai;

import com.mindolph.base.genai.llm.LlmService;
import org.junit.jupiter.api.Test;

/**
 * @author allen
 */
public class LlmServiceTest {

    @Test
    public void predict() {
        String input = "input";
        float temperature = 0.5f;
        String result1 = LlmService.getIns().predict(input, temperature, GenAiEvents.OutputAdjust.SHORTER);
        System.out.println(result1);
        String result2 = LlmService.getIns().predict(input, temperature, GenAiEvents.OutputAdjust.LONGER);
        System.out.println(result2);
    }
}
