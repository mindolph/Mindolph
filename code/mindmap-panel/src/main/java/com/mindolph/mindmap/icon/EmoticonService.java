package com.mindolph.mindmap.icon;

import javafx.scene.image.Image;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public class EmoticonService {

    private static final Logger log = LoggerFactory.getLogger(EmoticonService.class);

    private static EmoticonService ins;

    private List<String> ICON_NAMES;

    public static synchronized EmoticonService getInstance() {
        if (ins == null) {
            ins = new EmoticonService();
        }
        return ins;
    }

    private EmoticonService() {
        try (InputStream isIconNames = EmoticonService.class.getResourceAsStream("/icon/emotion/icon.lst")) {
            if (isIconNames == null) {
                return; // TODO
            }
            ICON_NAMES = IOUtils.readLines(isIconNames, "UTF-8");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<String> getIconNames() {
        return ICON_NAMES;
    }

    public Image getIcon(String name) {
        try (InputStream in = EmoticonService.class.getResourceAsStream("/icon/emotion/%s.png".formatted(name))) {
            if (in == null) {
                return null;
            }
            return new Image(in);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
