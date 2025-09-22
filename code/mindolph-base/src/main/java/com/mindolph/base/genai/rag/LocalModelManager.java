package com.mindolph.base.genai.rag;

import com.mindolph.base.util.NetworkUtils;
import com.mindolph.core.Downloader;
import com.mindolph.core.config.ProxyMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.util.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @since 1.13.0
 */
public class LocalModelManager {

    private static final Logger log = LoggerFactory.getLogger(LocalModelManager.class);

    private static final LocalModelManager instance = new LocalModelManager();
    private static final String MODEL_PATH_TEMPLATE = "models/%s/%s/model.onnx";
    private static final String TOKENIZER_PATH_TEMPLATE = "models/%s/%s/tokenizer.json";

    public static LocalModelManager getIns() {
        return instance;
    }

    private LocalModelManager() {
    }

    public boolean doesModelExists(String langCode, String modelName) {
        File modelFile = getModelFile(langCode, modelName);
        File tokenizerFile = getTokenizerFile(langCode, modelName);
        log.debug("Check model file: %s".formatted(modelFile));
        log.debug("Check tokenizer file: %s".formatted(tokenizerFile));
        return modelFile.exists() && tokenizerFile.exists();
    }

    public File getModelFile(String langCode, String modelName) {
        File baseDir = AppUtils.getAppBaseDir();
        String pathToModel = MODEL_PATH_TEMPLATE.formatted(langCode, modelName);
        return new File(baseDir, pathToModel);
    }

    public File getTokenizerFile(String langCode, String modelName) {
        File baseDir = AppUtils.getAppBaseDir();
        String pathToTokenizer = TOKENIZER_PATH_TEMPLATE.formatted(langCode, modelName);
        return new File(baseDir, pathToTokenizer);
    }

    public boolean downloadModel(String langCode, ModelMeta modelMeta) {
        String modelUrl = "%s/onnx/model.onnx".formatted(modelMeta.getDownloadUrl());
        String tokenizerUrl = "%s/tokenizer.json".formatted(modelMeta.getDownloadUrl());
        File modelFile = getModelFile(langCode, modelMeta.getName());
        File tokenizerFile = getTokenizerFile(langCode, modelMeta.getName());

        ProxyMeta proxyMeta = NetworkUtils.getProxyMeta();
        Downloader downloader = new Downloader();

        try {
            log.info("Download model: %s".formatted(modelUrl));
            boolean s1 = downloader.downloadWithProxyMeta(modelUrl, modelFile, proxyMeta);
            log.info("Saved model to: %s".formatted(modelFile));
            log.info("Download tokenizer: %s".formatted(tokenizerUrl));
            boolean s2 = downloader.downloadWithProxyMeta(tokenizerUrl, tokenizerFile, proxyMeta);
            log.info("Saved tokenizer to: %s".formatted(tokenizerFile));
            return s1 && s2;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     *
     * @param langCode
     * @param modelName
     * @return true if all files are deleted.
     */
    public boolean clearModel(String langCode, String modelName) {
        File modelFile = getModelFile(langCode, modelName);
        File tokenizerFile = getTokenizerFile(langCode, modelName);
        log.debug("Delete model file: %s".formatted(modelFile));
        log.debug("Delete tokenizer file: %s".formatted(tokenizerFile));
        return modelFile.delete() && tokenizerFile.delete();
    }
}
