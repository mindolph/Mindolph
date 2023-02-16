package com.mindolph.mindmap.icon;

import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @deprecated Probably can be used for other devices like Android.
 */
public final class ImageIconServiceProvider {
    private static final ImageIconService IMAGEICON_SERVICE;

    static {
        final ServiceLoader<ImageIconService> service = ServiceLoader.load(ImageIconService.class, ImageIconService.class.getClassLoader());
        service.reload();
        final Iterator<ImageIconService> iterator = service.iterator();
        IMAGEICON_SERVICE = iterator.hasNext() ? iterator.next() : new DefaultImageIconService();
        LoggerFactory.getLogger(ImageIconServiceProvider.class).info("Image Icon Service factory : " + IMAGEICON_SERVICE.getClass().getName());
    }

    public static ImageIconService getInstance() {
        return IMAGEICON_SERVICE;
    }
}
