package com.mindolph.base.genai.llm;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.util.NetworkUtils;
import com.mindolph.core.config.ProxyMeta;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_OUTPUT_LANGUAGE;
import static com.mindolph.base.constant.PrefConstants.GEN_AI_TIMEOUT;
import static com.mindolph.core.constant.GenAiConstants.lookupLanguage;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public abstract class BaseLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(BaseLlmProvider.class);

    protected ProviderMeta providerMeta;
    protected ModelMeta modelMeta;
    protected ProxyMeta proxyMeta;
    /**
     * Whether proxy is enabled globally.
     */
    protected boolean proxyEnabled = false;
    /**
     * In Seconds
     */
    protected final int timeout;

    protected final static String PROMPT_FORMAT_TEMPLATE = """
            
            {{input}}.
            {{format}}.
            {{length}}.
            {{language}}
            """;

    public BaseLlmProvider(ProviderMeta providerMeta, ModelMeta modelMeta) {
        FxPreferences fxPreferences = FxPreferences.getInstance();
        this.providerMeta = providerMeta;
        this.modelMeta = modelMeta;
        this.timeout = fxPreferences.getPreference(GEN_AI_TIMEOUT, 60);
        // Proxy settings
        this.proxyEnabled = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_ENABLE, false);
        if (this.proxyEnabled) {
            this.proxyMeta = NetworkUtils.getProxyMeta();
        }
    }

    //
    protected String determineModel(Input input) {
        if (StringUtils.isBlank(input.model())) {
            return modelMeta.getName();
        }
        else {
            return input.model();
        }
    }

    protected String determineLanguage(OutputParams outParams) {
        if (StringUtils.isBlank(outParams.outputLanguage())) {
            String langCode = FxPreferences.getInstance().getPreference(GEN_AI_OUTPUT_LANGUAGE, String.class);
            log.debug("Language code: {}", langCode);
            return lookupLanguage(langCode);
        }
        else {
            log.debug("Language: {}", outParams.outputLanguage());
            return lookupLanguage(outParams.outputLanguage());
        }
    }

    protected Map<String, Object> formatParams(String text, OutputParams outputParams) {
        HashMap<String, Object> params = new HashMap<>() {
            {
                put("input", text);
                put("format", outputParams.outputFormat() != null && StringUtils.isNotBlank(outputParams.outputFormat().getName())
                        ? "your output must be in format: %s".formatted(outputParams.outputFormat().getName())
                        : StringUtils.EMPTY);
                put("length", outputParams.outputAdjust() == null ? StringUtils.EMPTY :
                        "output : " + (outputParams.outputAdjust() == GenAiConstants.OutputAdjust.SHORTER ? "concisely" : "detailed"));
                put("language", "the output language must be " + determineLanguage(outputParams));
            }
        };
        return params;
    }

}
