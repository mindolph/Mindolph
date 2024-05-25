package com.mindolph.base.genai.llm;

import com.mindolph.core.constant.GenAiConstants.OutputAdjust;
import com.mindolph.core.constant.GenAiConstants.OutputFormat;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public record OutputParams(OutputAdjust outputAdjust, OutputFormat outputFormat) {

}
