package com.mindolph.base.genai.rag;

import com.mindolph.base.util.NetworkUtils;
import com.mindolph.core.Downloader;
import com.mindolph.core.config.ProxyMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.util.AppUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.mindolph.core.constant.GenAiConstants.ALL_LANGUAGE_CODE;

/**
 * @since 1.13.0
 */
public class LocalModelManager {

    private static final Logger log = LoggerFactory.getLogger(LocalModelManager.class);

    private static final LocalModelManager instance = new LocalModelManager();
    private static final String MODEL_PATH_TEMPLATE = "models/%s/%s/%s"; // lang code, model name, model file name
    private static final String TOKENIZER_PATH_TEMPLATE = "models/%s/%s/tokenizer.json"; // lang code, model name
    private static final String TOKENIZER_CONFIG_PATH_TEMPLATE = "models/%s/%s/tokenizer_config.json"; // lang code, model name

    public static LocalModelManager getIns() {
        return instance;
    }

    private LocalModelManager() {
    }

    public boolean doesModelExists(String langCode, ModelMeta modelMeta) {
        String language = determineLangCode(langCode, modelMeta);
        File modelFile = getModelFile(language, modelMeta);
        File tokenizerFile = getTokenizerFile(language, modelMeta);
        log.debug("Check model file: %s".formatted(modelFile));
        log.debug("Check tokenizer file: %s".formatted(tokenizerFile));
        return modelFile.exists() && tokenizerFile.exists();
    }

    /**
     * Get model file, if ModelMeta is null, lookup from the default definitions.
     *
     * @param langCode
     * @param modelMeta
     * @return
     */
    public File getModelFile(String langCode, ModelMeta modelMeta) {
        if (modelMeta == null) {
            throw new RuntimeException("model meta is null");
//            modelMeta = lookupModelMeta(GenAiModelProvider.INTERNAL.name(), modelMeta.getName());
        }
        String language = determineLangCode(langCode, modelMeta);
        log.debug("Supported language of this model: %s".formatted(language));
        String modelFileName = FilenameUtils.getName(modelMeta.getDownloadModelPath());
        return getModelFile(language, modelMeta.getName(), modelFileName);
    }

    private File getModelFile(String langCode, String modelName, String fileName) {
        File baseDir = AppUtils.getAppBaseDir();
        fileName = StringUtils.isBlank(fileName) ? "model.onnx" : fileName;
        String pathToModel = MODEL_PATH_TEMPLATE.formatted(langCode, modelName, fileName);
        return new File(baseDir, pathToModel);
    }

    public File getTokenizerFile(String langCode,  ModelMeta modelMeta) {
        File baseDir = AppUtils.getAppBaseDir();
        String language = determineLangCode(langCode, modelMeta);
        String pathToTokenizer = TOKENIZER_PATH_TEMPLATE.formatted(language, modelMeta.getName());
        return new File(baseDir, pathToTokenizer);
    }

    public File getTokenizerConfigFile(String langCode,  ModelMeta modelMeta) {
        File baseDir = AppUtils.getAppBaseDir();
        String language = determineLangCode(langCode, modelMeta);
        String pathToTokenizer = TOKENIZER_CONFIG_PATH_TEMPLATE.formatted(language, modelMeta.getName());
        return new File(baseDir, pathToTokenizer);
    }

    public boolean downloadModel(String langCode, ModelMeta modelMeta) {
        // urls
        String modelUrl;
        if (StringUtils.isBlank(modelMeta.getDownloadModelPath())) {
            // use default model file name if not provided.
            modelUrl = "%s/onnx/model.onnx".formatted(modelMeta.getDownloadUrl());
        }
        else {
            modelUrl = "%s/%s".formatted(modelMeta.getDownloadUrl(), modelMeta.getDownloadModelPath());
        }
        String tokenizerUrl = "%s/tokenizer.json".formatted(modelMeta.getDownloadUrl());
        String tokenizerConfigUrl = "%s/tokenizer_config.json".formatted(modelMeta.getDownloadUrl());

        // files
        String fileName = FilenameUtils.getName(modelMeta.getDownloadModelPath());
        File modelFile = getModelFile(langCode, modelMeta);
        File tokenizerFile = getTokenizerFile(langCode, modelMeta);
        File tokenizerConfigFile = getTokenizerConfigFile(langCode, modelMeta);

        ProxyMeta proxyMeta = NetworkUtils.getProxyMeta();
        Downloader downloader = new Downloader();

        try {
            log.info("Download tokenizerConfig: %s".formatted(tokenizerConfigUrl));
            downloader.downloadWithProxyMeta(tokenizerConfigUrl, tokenizerConfigFile, proxyMeta);
            log.info("Saved tokenizer config file: %s".formatted(tokenizerConfigFile));
            log.info("Download tokenizer: %s".formatted(tokenizerUrl));
            downloader.downloadWithProxyMeta(tokenizerUrl, tokenizerFile, proxyMeta);
            log.info("Saved tokenizer to: %s".formatted(tokenizerFile));
            log.info("Download model: %s".formatted(modelUrl));
            downloader.downloadWithProxyMeta(modelUrl, modelFile, proxyMeta);
            log.info("Saved model to: %s".formatted(modelFile));
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
//            return false;
        }
    }

    /**
     *
     * @param langCode
     * @param modelMeta
     * @return true if all files are deleted.
     */
    public boolean clearModel(String langCode, ModelMeta modelMeta) {
        String language = determineLangCode(langCode, modelMeta);
        File modelFile = getModelFile(language, modelMeta);
        File tokenizerFile = getTokenizerFile(language, modelMeta);
        File tokenizerConfigFile = getTokenizerConfigFile(language, modelMeta);
        log.debug("Delete model file: %s".formatted(modelFile));
        log.debug("Delete tokenizer file: %s".formatted(tokenizerFile));
        return modelFile.delete() && tokenizerFile.delete() && tokenizerConfigFile.delete();
    }


    // use provided lang code if it is not ALL_LANGUAGE_CODE in the ModelMeta,
    // in other words, prefer lang code in ModelMeta if it is for all language.
    private static String determineLangCode(String langCode, ModelMeta modelMeta) {
        return ALL_LANGUAGE_CODE.equals(modelMeta.getLangCode()) ? ALL_LANGUAGE_CODE : langCode;
    }

}
