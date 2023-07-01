package com.mindolph.core.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
public class ReleaseUtilsTest {

    @Test
    public void getLatestReleaseVersion() {
        ReleaseUtils.ReleaseInfo latestReleaseVersion = ReleaseUtils.getLatestReleaseVersion();
        System.out.println(latestReleaseVersion);
        Assertions.assertNotNull(latestReleaseVersion);
        Assertions.assertTrue(StringUtils.isNotBlank(latestReleaseVersion.getVersion()));
    }

}
