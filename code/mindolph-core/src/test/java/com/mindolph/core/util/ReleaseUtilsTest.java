package com.mindolph.core.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author mindolph.com@gmail.com
 */
public class ReleaseUtilsTest {

    @Test
    public void getLatestReleaseVersion() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ReleaseUtils.getLatestReleaseVersion(latestReleaseVersion -> {
            System.out.println(latestReleaseVersion);
            Assertions.assertNotNull(latestReleaseVersion);
            Assertions.assertTrue(StringUtils.isNotBlank(latestReleaseVersion.getVersion()));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            latch.countDown();
        });
        latch.await();

    }

}
