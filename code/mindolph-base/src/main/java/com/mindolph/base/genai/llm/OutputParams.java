package com.mindolph.base.genai.llm;

import com.mindolph.core.constant.GenAiConstants.OutputAdjust;
import com.mindolph.core.constant.GenAiConstants.OutputFormat;
import org.apache.commons.lang3.StringUtils;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public record OutputParams(OutputAdjust outputAdjust, OutputFormat outputFormat, String outputLanguage) {

    public OutputParams(OutputAdjust outputAdjust, OutputFormat outputFormat) {
        this(outputAdjust, outputFormat, StringUtils.EMPTY);
    }

}
