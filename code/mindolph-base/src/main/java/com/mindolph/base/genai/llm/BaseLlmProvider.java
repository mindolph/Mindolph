package com.mindolph.base.genai.llm;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindolph.base.constant.PrefConstants;
import static com.mindolph.base.constant.PrefConstants.GEN_AI_TIMEOUT;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.mfx.preference.FxPreferences;
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

    /**
     * In Seconds
     */
    protected final int timeout;
    protected final String apiKey;
    protected final String aiModel;
    protected final boolean useProxy;
    protected final static String PROMPT_FORMAT_TEMPLATE = """

            {{input}}.
            {{format}}.
            {{length}}.
            {{language}}
            """;

    /**
     * Proxy settings.
     */
    protected boolean proxyEnabled = false;
    protected String proxyUrl;
    protected String proxyType;
    protected String proxyHost;
    protected int proxyPort;
    protected String proxyUser;
    protected String proxyPassword;

    public BaseLlmProvider(String apiKey, String aiModel, boolean useProxy) {
        this.apiKey = apiKey;
        this.aiModel = aiModel;
        this.useProxy = useProxy;
        FxPreferences fxPreferences = FxPreferences.getInstance();
        this.timeout = fxPreferences.getPreference(GEN_AI_TIMEOUT, 60);
        // Proxy settings
        proxyEnabled = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_ENABLE, false);
        if (proxyEnabled) {
            proxyType = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_TYPE, "HTTP");
            proxyHost = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_HOST, "");
            proxyPort = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_PORT, 0);
            proxyUrl = "%s://%s:%s".formatted(StringUtils.lowerCase(proxyType), proxyHost, proxyPort).trim();
            proxyUser = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_USERNAME, "");
            proxyPassword = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_PASSWORD, "");
        }
    }

    //
    protected String determineModel(Input input) {
        if (StringUtils.isBlank(input.model())) {
            return aiModel;
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
